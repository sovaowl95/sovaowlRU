package ru.sovaowltv.service.icons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.shop.Icons;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.user.Achievements;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.IconsRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class IconsUtil {
    private final IconsRepository iconsRepository;

    public Icons addIconAchievement(User user, Achievements ach) {
        Icons icon = generateIconFromAchievements(ach);
        user.getIcons().add(icon);
        return icon;
    }

    private Icons generateIconFromAchievements(Achievements ach) {
        Icons icon = new Icons();
        icon.setLink("/achievements/" + ach.name() + ".png");
        icon.setName(ach.name());
        icon.setPrice(0);
        icon.setRarity(ach.getRarity());
        icon = iconsRepository.saveAndFlush(icon);
        return icon;
    }

    public void addNewIconToUser(User userById) {
        Set<Icons> iconsSet = userById.getIcons();
        Icons icon = generateIcons();
        iconsSet.add(icon);
    }

    private Icons generateIcons() {
        Icons icons = new Icons();
        icons.setLink("/achievements/" + ".png");
        icons.setName("name");
        icons.setPrice(0);
        icons.setRarity(Rarity.COMMON);
        iconsRepository.save(icons);
        return icons;
    }

    public boolean doesUserHaveIcon(User user, Achievements achievement) {
        List<Icons> activeIcons = user.getUserSettings().getActiveIcons();
        for (Icons activeIcon : activeIcons) {
            if (activeIcon.getName().equalsIgnoreCase(achievement.name()))
                return true;
        }
        return false;
    }
}
