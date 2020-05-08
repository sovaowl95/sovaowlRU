package ru.sovaowltv.service.factorys;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Smile;
import ru.sovaowltv.model.shop.Style;
import ru.sovaowltv.model.user.Achievements;
import ru.sovaowltv.model.user.Role;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.model.user.UserSettings;
import ru.sovaowltv.repositories.user.UserSettingsRepository;
import ru.sovaowltv.repositories.website.SmilesRepository;
import ru.sovaowltv.repositories.website.StylesRepository;
import ru.sovaowltv.service.email.EmailUtil;
import ru.sovaowltv.service.unclassified.UserDataValidator;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserFactory {
    private final StylesRepository stylesRepository;
    private final SmilesRepository smilesRepository;
    private final UserSettingsRepository userSettingsRepository;

    private final UserUtil userUtil;
    private final EmailUtil emailUtil;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDataValidator userDataValidator;

    public void createUserFromMapWithNeedVerification(Map<String, Object> map) {
        createUserFromMap(map, true);
    }

    public User createUserFromMap(Map<String, Object> map, boolean needVerification) {
        String login = String.valueOf(map.get("login"));
        String password = String.valueOf(map.get("password"));
        String email = String.valueOf(map.get("email"));
        String gender = String.valueOf(map.get("gender"));
        String rules = String.valueOf(map.get("rules"));
        return createUser(login, password, email, gender, rules, needVerification);
    }


    private User createUser(String login, String password, String email, String gender, String rules, boolean needVerification) {
        userDataValidator.validateAllData(login, password, email, gender, rules, needVerification);

        User user = new User();
        user.setLogin(login.toLowerCase());
        user.setNickname(login);
        solvePassword(password, user);
        user.setEmail(email.toLowerCase().trim());

        user.setRecoveryToken(UUID.randomUUID().toString());

        user.setLevel(1);
        user.setExp(0);
        user.setCoins(500);
        user.setPremiumTimes(0);
        user.setCaravanRobbedTimes(0);

        user.setMale(Boolean.parseBoolean(gender));
        user.setUserAvatar("def.png");
        user.setEmailVerification(UUID.randomUUID().toString());
        user.setRegDate(LocalDateTime.now());

        generateRoles(user);
        generateUserSettings(user);
//        generateUserAchievements(user); beta-test
        generateStyle(user);
        generateSmiles(user);
        generateIcons(user);

        userSettingsRepository.save(user.getUserSettings());
        usersRepositoryHandler.saveUser(user);

        solveVerification(email, needVerification, user);
        userUtil.setAuthContext(user);
        return user;
    }


    private void generateRoles(User user) {
        Set<Role> rolesSet = new HashSet<>();
        rolesSet.add(Role.REGISTERED);
        user.setRoles(rolesSet);
    }

    private void solvePassword(String password, User user) {
        if (!password.equalsIgnoreCase("NULL")) user.setPassword(passwordEncoder.encode(password));
        else user.setPassword("NULL");
    }


    private void solveVerification(String email, boolean needVerification, User user) {
        if (needVerification) {
            emailUtil.sendRegEmail(email, user);
        } else {
            userUtil.commitVerification(user);
        }
    }

    private void generateIcons(User user) {
        user.setIcons(Collections.emptySet());
    }

    private void generateSmiles(User user) {
        Set<Smile> smileList = new HashSet<>(smilesRepository.findAll());
        smileList = smileList.stream().filter(v -> v.getRarity() == Rarity.COMMON).collect(Collectors.toSet());
        user.setSmiles(smileList);
    }

    private void generateStyle(User user) {
        Set<Style> styleList = new HashSet<>(stylesRepository.findAll());
        styleList = styleList.stream().filter(v -> v.getRarity() == Rarity.COMMON).collect(Collectors.toSet());
        user.setStyles(styleList);
    }

    private void generateUserAchievements(User user) {
        Set<Achievements> achievements = new HashSet<>();
        achievements.add(Achievements.BETA_TESTER);
        user.setAchievements(achievements);
    }

    private void generateUserSettings(User user) {
        UserSettings userSettings = new UserSettings();
        userSettings.setStyleId(user.isMale() ? 10 : 11);
        userSettings.setPremiumChat(false);
        userSettings.setShowTime(false);
        userSettings.setSmileSize(28);
        userSettings.setTextSize(16);
        userSettings.setUser(user);
        userSettings.setActiveIcons(new ArrayList<>());
        user.setUserSettings(userSettings);
    }
}
