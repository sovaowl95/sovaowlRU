package ru.sovaowltv.service.caravan;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.user.Achievements;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.icons.IconsUtil;
import ru.sovaowltv.service.smiles.SmilesUtil;
import ru.sovaowltv.service.styles.StyleUtil;
import ru.sovaowltv.service.unclassified.RandomUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserPremiumUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:constants.yml")
public class CaravanRewardUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final RandomUtil randomUtil;
    private final RarityUtil rarityUtil;
    private final ItemUtil itemUtil;
    private final UserPremiumUtil userPremiumUtil;
    private final SmilesUtil smilesUtil;
    private final StyleUtil styleUtil;
    private final IconsUtil iconsUtil;

    private final CaravanMessages caravanMessages;

    @Value("${caravanSmileChance}")
    private double caravanSmileChance;

    @Value("${caravanPremiumChance}")
    private double caravanPremiumChance;

    void giveReward(Set<Long> robbers, Rarity caravanRarity) {
        int qualityInc = rarityUtil.changeRarityToQualityInc(caravanRarity);
        List<Object> list = new ArrayList<>();

        robbers.forEach(userId -> {
            User user = null;
            try {
                user = usersRepositoryHandler.getUserById(userId);
                MessageStatus ms = new MessageStatus();
                ms.setInfo(generateRewardAndGetInfoString(user, caravanRarity, qualityInc));
                ms.setType("caravanReward");
                list.add(ms);
                giveCaravanAchievement(user);
            } catch (Exception e) {
                log.error("user not found! " + userId, e);
            } finally {
                usersRepositoryHandler.saveAndFree(user);
            }
        });
        robbers.clear();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType("caravanReward");
        messageStatus.setInfo(new Gson().toJson(list));

        caravanMessages.sendToAllStreamsCaravanReward(new Gson().toJson(messageStatus));
    }

    private String generateRewardAndGetInfoString(User user, Rarity caravanRarity, double qualityIncrease) {
        user.setCaravanRobbedTimes(user.getCaravanRobbedTimes() + 1);
        if (randomUtil.isChance(caravanPremiumChance)) {
            return givePremium(user, caravanRarity, qualityIncrease);
        } else if (randomUtil.isChance(caravanSmileChance)) {
            return giveSmileOrStyle(user, caravanRarity, qualityIncrease);
        } else {
            return itemUtil.getItemReward(user, caravanRarity, qualityIncrease);
        }
    }

    private String giveSmileOrStyle(User user, Rarity caravanRarity, double qualityIncrease) {
        if (randomUtil.nextInt(2) == 0) {
            return smilesUtil.getSmileReward(user, caravanRarity, qualityIncrease);
        } else {
            return styleUtil.getStyleReward(user, caravanRarity, qualityIncrease);
        }
    }

    private String givePremium(User user, Rarity caravanRarity, double qualityIncrease) {
        String premiumReward = userPremiumUtil.getPremiumReward(user);
        if (premiumReward == null) {
            log.error("CAN'T GIVE PREMIUM TO USER: " + user.getId() + " " + user.getNickname());
            return itemUtil.getItemReward(user, caravanRarity, qualityIncrease);
        }
        return premiumReward;
    }

    private void giveCaravanAchievement(User user) {
        int caravanRobbedTimes = user.getCaravanRobbedTimes();
        Set<Achievements> userAchievements = user.getAchievements();
        if (caravanRobbedTimes >= 10 && !userAchievements.contains(Achievements.CARAVAN_10)) {
            userAchievements.add(Achievements.CARAVAN_10);
            iconsUtil.addIconAchievement(user, Achievements.CARAVAN_10);
        }
        if (caravanRobbedTimes >= 100 && !userAchievements.contains(Achievements.CARAVAN_100)) {
            userAchievements.add(Achievements.CARAVAN_100);
            iconsUtil.addIconAchievement(user, Achievements.CARAVAN_100);
        }
        if (caravanRobbedTimes >= 1000 && !userAchievements.contains(Achievements.CARAVAN_1000)) {
            userAchievements.add(Achievements.CARAVAN_1000);
            iconsUtil.addIconAchievement(user, Achievements.CARAVAN_1000);
        }
    }
}