package ru.sovaowltv.service.spammer;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.spammer.SpammerStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.stream.SpammerRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpammerUtil {
    private final SpammerRepository spammerRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;

    private final DataExtractor dataExtractor;
    private final AutowireCapableBeanFactory autowireCapableBeanFactory;

    final Map<Long, SpammerThread> spammerMap = new HashMap<>();

    public Spammer getSpammerByIdAndVerifyByUser(String id, User user) {
        Optional<Spammer> spammerOptional = spammerRepository.findById(Long.valueOf(id));
        if (spammerOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find spammer");
        if (!spammerOptional.get().getStream().getUser().equals(user))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It's not your spammer");
        return spammerOptional.get();
    }

    private Spammer createNewSpammer(Stream stream) {
        Spammer spammer = new Spammer();
        spammer.setStream(stream);
        spammer.setText("default spammer text");
        spammer.setTime(60);
        spammer.setDelay(0);
        spammer.setSpammerStatus(SpammerStatus.STOP);
        addSpammerToStream(stream, spammer);
        return spammer;
    }

    private void addSpammerToStream(Stream stream, Spammer spammer) {
        Set<Spammer> spammerSet = stream.getSpammerSet();
        if (spammerSet == null) spammerSet = new HashSet<>();
        spammerSet.add(spammer);
    }

    public void createSpam(Stream stream) {
        Spammer newSpammer = createNewSpammer(stream);
        spammerRepository.save(newSpammer);
        addSpammer(newSpammer);
    }

    public void deleteSpam(String json) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        String id = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id");
        User user = userUtil.getUser();
        Spammer spammer = getSpammerByIdAndVerifyByUser(id, user);
        usersRepositoryHandler.free(user);
        spammerRepository.delete(spammer);
        deleteSpammer(spammer);
    }


    public void launchSpammers(Stream stream) {
        try {
            stream.getSpammerSet().forEach(spammer -> {
                try {
                    addSpammer(spammer);
                    if (spammer.getSpammerStatus() == SpammerStatus.RUN)
                        startSpammer(spammer);
                } catch (Exception e) {
                    log.error("SpammerUtil launchSpammers -> ", e);
                }
            });
        } catch (Exception e) {
            log.error("error launch spammers for user " + stream.getUser().getNickname(), e);
        }
    }

    private void addSpammer(Spammer spammer) {
        if (!spammerMap.containsKey(spammer.getId())) {
            addSpammerInMap(spammer, spammer.getId());
        }
    }

    public void startSpammer(Spammer spammer) {
        long id = spammer.getId();
        if (spammerMap.containsKey(id)) {
            spammerMap.get(id).stopSpam();
            spammerMap.remove(id);
        }
        SpammerThread spammerThread = addSpammerInMap(spammer, id);
        spammerThread.start();
    }

    public void stopSpammer(Spammer spammer) {
        long id = spammer.getId();
        if (spammerMap.containsKey(id)) {
            spammerMap.get(id).stopSpam();
            spammerMap.remove(id);
        }
    }

    private void deleteSpammer(Spammer spammer) {
        long id = spammer.getId();
        if (!spammerMap.containsKey(id)) {
            spammerMap.get(id).stopSpam();
            spammerMap.remove(id);
        }
    }


    SpammerThread addSpammerInMap(Spammer spammer, long id) {
        SpammerThread spammerThread = new SpammerThread();
        spammerThread.setSpammer(spammer);
        autowireCapableBeanFactory.autowireBean(spammerThread);
        spammerMap.put(id, spammerThread);
        return spammerThread;
    }
}