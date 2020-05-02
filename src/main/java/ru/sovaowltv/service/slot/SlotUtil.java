package ru.sovaowltv.service.slot;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.caravan.RarityUtil;
import ru.sovaowltv.service.smiles.SmilesUtil;
import ru.sovaowltv.service.styles.StyleUtil;
import ru.sovaowltv.service.unclassified.RandomUtil;
import ru.sovaowltv.service.user.params.UserCoinsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotUtil {
    private final RandomUtil randomUtil;
    private final UserCoinsUtil userCoinsUtil;
    private final RarityUtil rarityUtil;
    private final StyleUtil styleUtil;
    private final SmilesUtil smilesUtil;

    public String startSlotGame(User user, String text) {
        int betValue = extractBetValue(text);

        if (userCoinsUtil.withdrawMoney(user, betValue)) {
            int num1 = randomUtil.getIntWithBounds(1, 7);
            int num2 = randomUtil.getIntWithBounds(1, 7);
            int num3 = randomUtil.getIntWithBounds(1, 7);

            if (all777(num1, num2, num3, betValue)) {
                return solveWinAce(user);
            } else if (allEquals(num1, num2, num3)) {
                return solveWin(user, betValue, num1, num2, num3, 10, "win10");
            } else if (twoEquals(num1, num2, num3)) {
                return solveWin(user, betValue, num1, num2, num3, 2, "win2");
            } else {
                return solveLose(user, betValue, num1, num2, num3);
            }
        } else {
            return "NOT ENOUGH MONEY";
        }
    }

    private String solveWinAce(User user) {
        double v = userCoinsUtil.calcCoinsRewardNoModificator(user, 777);
        double coins = 10 * v;
        userCoinsUtil.addCoins(user, coins);
        Map<String, Object> ace = getStringObjectMap(user, "ace", coins, 777, 7, 7, 7);

        List<String> smiles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            smiles.add(smilesUtil.getSmileReward(user, Rarity.ANCIENT, rarityUtil.changeRarityToQualityInc(Rarity.ANCIENT)));
        }

        List<String> styles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            styles.add(styleUtil.getStyleReward(user, Rarity.ANCIENT, rarityUtil.changeRarityToQualityInc(Rarity.ANCIENT)));
        }

        ace.put("smiles", smiles);
        ace.put("styles", styles);

        return new Gson().toJson(ace);
    }

    private String solveLose(User user, int betValue, int num1, int num2, int num3) {
        return getJsonStr(user, "lose", betValue, betValue, num1, num2, num3);
    }

    private String solveWin(User user, int betValue, int num1, int num2, int num3, int multiplier, String type) {
        double v = userCoinsUtil.calcCoinsRewardNoModificator(user, betValue);
        double coins = multiplier * v;
        userCoinsUtil.addCoins(user, coins);
        return getJsonStr(user, type, coins, betValue, num1, num2, num3);
    }

    private int extractBetValue(String text) {
        try {
            String[] split = text.split(" ");
            if (split.length >= 2) {
                int i = Integer.parseInt(split[1]);
                if (i <= 0) return 100;
                return i;
            } else {
                return 100;
            }
        } catch (Exception e) {
            return 100;
        }
    }

    private String getJsonStr(User user, String type, double reward, double bet, int el1, int el2, int el3) {
        Map<String, Object> map = getStringObjectMap(user, type, reward, bet, el1, el2, el3);
        return new Gson().toJson(map);
    }

    @NotNull
    private Map<String, Object> getStringObjectMap(User user, String type, double reward, double bet, int el1, int el2, int el3) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("premium", user.isPremiumUser());
        map.put("nickname", user.getNickname());
        map.put("coins", reward);
        map.put("bet", bet);
        map.put("el1", el1);
        map.put("el2", el2);
        map.put("el3", el3);
        return map;
    }

    private boolean all777(int el1, int el2, int el3, int betValue) {
        return betValue == 777 && el1 == 7 && el2 == 7 && el3 == 7;
    }

    private boolean allEquals(int el1, int el2, int el3) {
        return el1 == el2 && el2 == el3;
    }

    private boolean twoEquals(int el1, int el2, int el3) {
        return el1 == el2 || el1 == el3 || el2 == el3;
    }
}
