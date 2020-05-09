package ru.sovaowltv.service.caravan;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.item.*;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.ItemRepository;
import ru.sovaowltv.service.unclassified.RandomUtil;
import ru.sovaowltv.service.user.params.UserCoinsUtil;
import ru.sovaowltv.service.user.params.UserExpUtil;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemUtil {
    private final ItemRepository itemRepository;

    private final RandomUtil randomUtil;
    private final RarityUtil rarityUtil;
    private final UserExpUtil userExpUtil;
    private final UserCoinsUtil userCoinsUtil;

    String getItemReward(User user, Rarity caravanRarity, double chanceMultiplier) {
        Item item = calculateItemReward(user, caravanRarity, chanceMultiplier);
        String userId = String.valueOf(user.getId());
        userExpUtil.addExp(userId, item.getExp(), user);
        userCoinsUtil.addCoins(user, item.getPrice());
        return convertItemToString(user, item);
    }

    private Item getRandomItem() {
        int i = randomUtil.nextInt(15); // last case + 1
        switch (i) {
            case 0:
                return new AdultMagazine();
            case 1:
                return new AlcoholMashine();
            case 2:
                return new BugOfPotatoes();
            case 3:
                return new Cake();
            case 4:
                return new Cookie();
            case 5:
                return new Dildo();
            case 6:
                return new GoldBar();
            case 7:
                return new GoodTeammates();
            case 8:
                return new GreenShield();
            case 9:
                return new KeyFromTheHeartOfAnOwl();
            case 10:
                return new Kitten();
            case 11:
                return new Medkit();
            case 12:
                return new RubberWoman();
            case 13:
                return new Socks();
            case 14:
                return new Trash();
            default:
                log.error("CANT FIND RANDOM ITEM:{}", i);
        }
        return new Trash();
    }


    private Item calculateItemReward(User user, Rarity caravanRarity, double qualityIncrease) {
        Item item = getRandomItem();
        item.setUser(user);
        item.setRarity(rarityUtil.generateRarity(rarityUtil.getMinRarity(caravanRarity), caravanRarity, qualityIncrease));

        int lowPrice = rarityUtil.changeRarityToPrice(item.getRarity());
        int highPrice = lowPrice * 2;
        int generatedPrice = randomUtil.getIntWithBounds(lowPrice, highPrice);
        double realPrice = userCoinsUtil.calcCoinsRewardNoModificator(user, generatedPrice);
        item.setPrice((int) realPrice);

        int generatedExp = randomUtil.getIntWithBounds(lowPrice, highPrice);
        int realExp = userExpUtil.calcExpRewardForUser(user, generatedExp);
        item.setExp(realExp);

        user.getItems().add(item);
        itemRepository.save(item);

        return item;
    }

    private String convertItemToString(User user, Item item) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "item");
        map.put("name", item.getClass().getSimpleName());
        map.put("rarity", item.getRarity());
        map.put("price", item.getPrice());
        map.put("exp", item.getExp());
        map.put("premiumUser", user.isPremiumUser());
        map.put("nickname", user.getNickname());
        return new Gson().toJson(map);
    }
}