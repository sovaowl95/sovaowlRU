package ru.sovaowltv.service.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.exceptions.chat.BadSmileServiceException;
import ru.sovaowltv.exceptions.chat.BadSmileServiceParamsException;
import ru.sovaowltv.exceptions.chat.SavedSmileNotFound;
import ru.sovaowltv.model.chat.SavedSmile;
import ru.sovaowltv.model.shop.Icons;
import ru.sovaowltv.model.user.Achievements;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.model.user.UserSettings;
import ru.sovaowltv.repositories.chat.SavedSmileRepository;
import ru.sovaowltv.repositories.user.UserSettingsRepository;
import ru.sovaowltv.service.icons.IconsUtil;
import ru.sovaowltv.service.smiles.GGSmiles;
import ru.sovaowltv.service.smiles.TwitchSmiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
@Slf4j
public class UserSettingsUtil {
    private final SavedSmileRepository savedSmileRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final IconsUtil iconsUtil;
    private final UserUtil userUtil;

    private final TwitchSmiles twitchSmiles;
    private final GGSmiles ggSmiles;

    @Value("${twitch}")
    private String twitch;

    @Value("${gg}")
    private String gg;

    public void setShowTime(boolean val) {
        User user = null;
        try {
            user = userUtil.getUser();
            user.getUserSettings().setShowTime(val);
            userSettingsRepository.save(user.getUserSettings());
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void setPremiumText(boolean premiumText) {
        User user = null;
        try {
            user = userUtil.getUser();
            if (!user.isPremiumUser()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buy premium first");
            }
            user.getUserSettings().setPremiumChat(premiumText);
            userSettingsRepository.save(user.getUserSettings());
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void setTextSize(Integer size) {
        User user = null;
        try {
            if (size <= -1 || size >= 60) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad size");
            user = userUtil.getUser();
            user.getUserSettings().setTextSize(size);
            userSettingsRepository.save(user.getUserSettings());
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void setSmilesSize(Integer size) {
        User user = null;
        try {
            if (size <= -1 || size >= 150) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad size");
            user = userUtil.getUser();
            user.getUserSettings().setSmileSize(size);
            userSettingsRepository.save(user.getUserSettings());
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void recalculateIcons() {
        User user = null;
        try {
            user = userUtil.getUser();
            for (Achievements achievement : user.getAchievements()) {
                boolean find = false;
                for (Icons icon : user.getIcons()) {
                    if (icon.getName().equalsIgnoreCase(achievement.name())) {
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    iconsUtil.addIconAchievement(user, achievement);
                }
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void clearIcon() {
        User user = null;
        try {
            user = userUtil.getUser();
            user.getUserSettings().getActiveIcons().clear();
            userSettingsRepository.save(user.getUserSettings());
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void addIconInActive(String name) {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getAchievements().contains(Achievements.valueOf(name))) {
                for (Icons icon : user.getIcons()) {
                    if (icon.getName().equalsIgnoreCase(name)) {
                        for (Icons activeIcon : user.getUserSettings().getActiveIcons()) {
                            if (activeIcon.getName().equalsIgnoreCase(name)) return;
                        }
                        user.getUserSettings().getActiveIcons().add(icon);
                        userSettingsRepository.save(user.getUserSettings());
                        return;
                    }
                }
                Icons icons = iconsUtil.addIconAchievement(user, Achievements.valueOf(name));
                user.getUserSettings().getActiveIcons().add(icons);
                userSettingsRepository.save(user.getUserSettings());
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void removeIconFromActive(String name) {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getAchievements().contains(Achievements.valueOf(name))) {
                for (Icons icon : user.getIcons()) {
                    if (icon.getName().equalsIgnoreCase(name)) {
                        user.getUserSettings().getActiveIcons().remove(icon);
                        userSettingsRepository.save(user.getUserSettings());
                        return;
                    }
                }
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public SavedSmile addSavedSmile(Map<String, Object> map) {
        User user = null;
        try {
            user = userUtil.getUser();
            if (map.get("code") == null || map.get("service") == null) {
                throw new BadSmileServiceParamsException("bad params " + map.toString());
            }
            String name = map.get("code").toString();
            String service = map.get("service").toString().toLowerCase();
            if (!service.equalsIgnoreCase(twitch) && !service.equalsIgnoreCase(gg)) {
                throw new BadSmileServiceException("unknown service " + service);
            }

            UserSettings userSettings = user.getUserSettings();
            List<SavedSmile> savedSmiles = userSettings.getSavedSmiles();
            for (SavedSmile savedSmile : savedSmiles) {
                if (savedSmile.getService().equals(service)
                        && savedSmile.getSmileName().equals(name)) {
                    return null;
                }
            }

            Optional<SavedSmile> smileOptional = savedSmileRepository.getByServiceAndSmileName(service, name);
            SavedSmile savedSmile = null;
            if (smileOptional.isPresent()) {
                userSettings.getSavedSmiles().add(smileOptional.get());
            } else {
                savedSmile = new SavedSmile();
                savedSmile.setService(service);
                savedSmile.setSmileName(name);
                savedSmile.setSmileCode(getSmileCode(name, service));

                savedSmileRepository.save(savedSmile);
                savedSmiles.add(0, savedSmile);
            }
            userSettingsRepository.save(userSettings);
            return savedSmile;
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    @NotNull
    private String getSmileCode(String name, String service) {
        String smile = null;
        if (service.equals(twitch)) {
            smile = twitchSmiles.getSmile(name);
        } else if (service.equals(gg)) {
            smile = ggSmiles.getSmile(name);
        }
        if (smile == null) throw new SavedSmileNotFound("saved smile not found " + name);
        return smile;
    }

    //todo:
//    public void removeSavedSmile(Map<String, Object> map) {
//        User user = null;
//        try {
//            user = userUtil.getUser();
//
//        } finally {
//            usersRepositoryHandler.saveAndFree(user);
//        }
//    }
}
