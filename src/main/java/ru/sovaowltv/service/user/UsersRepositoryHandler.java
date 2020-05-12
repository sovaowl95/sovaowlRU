package ru.sovaowltv.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersRepository;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersRepositoryHandler {
    private final UsersRepository usersRepository;
    private final UsersTwitchRepository usersTwitchRepository;

    private final Map<Long, User> idToUserMap = new HashMap<>();
    private final Map<Long, AtomicLong> counterToUser = new HashMap<>();

    private final Map<Long, Long> usersForRemove = new HashMap<>();

    @Scheduled(cron = "*/10 * * * * *") //every 10 sec
    private synchronized void removeUnused() {
        Iterator<Map.Entry<Long, Long>> iterator = usersForRemove.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            Long key = entry.getKey();
            Long timeValue = entry.getValue();
            User user = idToUserMap.get(key);
            if (user == null) return;
            if (counterToUser.get(user.getId()).get() == 0) {
                usersRepository.save(user);

                idToUserMap.remove(user.getId());
                counterToUser.remove(user.getId());
                iterator.remove();

                log.debug("user saved and removed from map. id: {}", user.getId());
            } else if (timeValue + TimeUnit.MINUTES.toMillis(1) < System.currentTimeMillis()) {
                log.error("bug? {} now: {} {} \n{}\n{}", timeValue, System.currentTimeMillis(), idToUserMap, counterToUser, usersForRemove);
                usersRepository.save(user);
                iterator.remove();
            } else {
                log.debug("user used by someone.");
                iterator.remove();
            }
        }
    }

    private synchronized User addUserToLocalDB(User user) {
        long id = user.getId();
        if (idToUserMap.containsKey(id)) {
            counterToUser.get(id).incrementAndGet();
            return idToUserMap.get(id);
        }
        idToUserMap.put(id, user);
        counterToUser.put(id, new AtomicLong(1));
        return user;
    }

    public synchronized void saveUser(User user) {
        usersRepository.save(user);
    }

    public synchronized void free(User user) {
        if (user == null) return;
        long counter = counterToUser.get(user.getId()).decrementAndGet();
        if (counter == 0) usersForRemove.put(user.getId(), System.currentTimeMillis());
        log.debug("free user without changes. {} {}", user.getId(), counter);
    }

    public synchronized void saveAndFree(User user) {
        if (user == null) {
            log.debug("TRYING TO SAVE&FREE EMPTY USER");
            return;
        }
        log.debug("saveAndFree");
        long counter = counterToUser.get(user.getId()).decrementAndGet();
        if (counter <= 0) {
            usersForRemove.put(user.getId(), System.currentTimeMillis());
            log.debug("user counter == 0.  put in FOR REMOVED LIST. id: {}", user.getId());
        } else {
            log.debug("user not ready for save. counter: {} id: {}", counter, user.getId());
        }
    }

    /*
     * EXTRA
     */

    /**
     * ADMIN
     */
    public synchronized void saveUserMap() {
        idToUserMap.forEach((k, v) -> usersRepository.save(v));
    }

    public synchronized void saveAllAndClear() {
        log.debug("clearing user map...");
        idToUserMap.forEach((k, v) -> saveAndFree(v));
        idToUserMap.clear();
        counterToUser.clear();
        usersForRemove.clear();
        log.debug("clearing user map done!");
    }

    @Scheduled(cron = "*/10 * * * * *") //every 10 sec
    private synchronized void showMapsStatus() {
        if (!idToUserMap.isEmpty()) {
            log.debug("idToUserMap size {}", idToUserMap.size());
            idToUserMap.forEach((k, v) -> {
                log.debug("idToUserMap");
                log.debug("k = {}", k);
                log.debug("v = {}", v.getLogin());
            });
            log.debug("");
        }

        if (!counterToUser.isEmpty()) {
            log.debug("counterToUser size {}", counterToUser.size());
            counterToUser.forEach((k, v) -> {
                log.debug("counterToUser");
                log.debug("k = {}", k);
                log.debug("v = {}", v);
            });
            log.debug("");
        }

        if (!usersForRemove.isEmpty()) {
            log.debug("usersForRemove size {}", usersForRemove.size());
            usersForRemove.forEach((k, v) -> {
                log.debug("usersForRemove");
                log.debug("k = {}", k);
                log.debug("t = {}", v);
            });
            log.debug("");
        }
    }
    /*
     *
     * FIND METHODS
     *
     */

    /**
     * BY ID
     */
    public synchronized User getUserById(String userId) {
        return getUserById(Long.parseLong(userId));
    }

    public synchronized User getUserById(Long id) {
        if (idToUserMap.containsKey(id)) {
            long counter = counterToUser.get(id).incrementAndGet();
            log.debug("user already in map. counter: {} id: {}", counter, id);
            return idToUserMap.get(id);
        } else {
            Optional<User> userOptional = usersRepository.findById(id);
            if (userOptional.isEmpty())
                throw new UserNotFoundException("Can't find user by id: {}" + id.toString());
            User user = userOptional.get();
            user = addUserToLocalDB(user);
            log.debug("user was loaded in map. counter: 1 id: {}", id);
            return user;
        }
    }


    /**
     * BY LOGIN
     */
    public synchronized Optional<User> getUserOptionalByLogin(String login) {
        try {
            return Optional.of(getUserByLogin(login));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized User getUserByLogin(String login) {
        login = login.toLowerCase();
        for (Map.Entry<Long, User> entry : idToUserMap.entrySet()) {
            User v = entry.getValue();
            if (v.getLogin().equals(login)) {
                return getUserById(v.getId());
            }
        }
        Optional<User> userOptional = usersRepository.findUserByLogin(login);
        if (userOptional.isEmpty())
            throw new UserNotFoundException("Can't find user by login: " + login);

        return getUserById(userOptional.get().getId());
    }

    public synchronized User getUserByLoginOrEmail(String login, String email) {
        Optional<User> userOptional = usersRepository.findUserByLoginOrEmail(login, email);
        if (userOptional.isEmpty()) {
            log.debug("User not found by username or email : {} {}", login, email);
            throw new UsernameNotFoundException("User not found with username or email : " + login + " " + email);
        }
        return addUserToLocalDB(userOptional.get());
    }

    /**
     * BY NICKNAME
     */
    public synchronized Optional<User> getUserByNicknameOptional(String nickname) {
        try {
            return Optional.of(getUserByNickname(nickname));
        } catch (Exception e) {
            log.debug("can't find user by nick optional {}", nickname);
            return Optional.empty();
        }
    }

    public synchronized User getUserByNickname(String nickname) {
        for (Map.Entry<Long, User> entry : idToUserMap.entrySet()) {
            User v = entry.getValue();
            if (v.getNickname().equals(nickname)) {
                return getUserById(v.getId());
            }
        }
        Optional<User> userOptional = usersRepository.findUserByNickname(nickname);
        if (userOptional.isEmpty())
            throw new UserNotFoundException("Can't find user by nickname: " + nickname);
        return getUserById(userOptional.get().getId());
    }

    /**
     * BY EMAIL
     */
    public synchronized Optional<User> getUserOptionalByEmail(String email) {
        try {
            return Optional.of(getUserByEmail(email));
        } catch (Exception e) {
            log.debug("user by email not found: {} returning optional.empty", email);
            return Optional.empty();
        }
    }

    public synchronized User getUserByEmail(String email) {
        for (Map.Entry<Long, User> entry : idToUserMap.entrySet()) {
            User user = entry.getValue();
            if (user.getEmail().equalsIgnoreCase(email)) {
                return getUserById(user.getId());
            }
        }
        Optional<User> userOptional = usersRepository.findUserByEmail(email);
        if (userOptional.isEmpty())
            throw new UserNotFoundException("Can't find user by email: " + email);

        return getUserById(userOptional.get().getId());
    }

    /**
     * ADDITIONAL
     */
    public synchronized List<User> findAllByPremiumExpiredBeforeAndPremiumUserTrue() {
        List<User> userList = usersRepository.findAllByPremiumExpiredBeforeAndPremiumUserTrue(LocalDate.now().plusDays(1));
        userList.forEach(this::addUserToLocalDB);
        return userList;
    }

    public synchronized List<User> findAllByUserTwitchNotNull() {
        List<UserTwitch> list = usersTwitchRepository.findAll();
        return list.stream()
                .map(user -> addUserToLocalDB(user.getUser()))
                .collect(Collectors.toList());
    }

    public synchronized List<User> findAll() {
        List<User> userList = usersRepository.findAll();
        userList.forEach(this::addUserToLocalDB);
        return userList;
    }
}