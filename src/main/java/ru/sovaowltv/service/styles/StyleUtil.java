
package ru.sovaowltv.service.styles;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Style;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UserSettingsRepository;
import ru.sovaowltv.repositories.website.StylesRepository;
import ru.sovaowltv.service.caravan.RarityUtil;
import ru.sovaowltv.service.unclassified.RandomUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserCoinsUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class StyleUtil {
    private final UserSettingsRepository userSettingsRepository;
    private final StylesRepository stylesRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UserUtil userUtil;
    private final RarityUtil rarityUtil;
    private final RandomUtil randomUtil;
    private final UserCoinsUtil userCoinsUtil;


    @Value("${caravanDuplicatePriceDelimiter}")
    private int caravanDuplicatePriceDelimiter;

    @Value("${caravanLowPriceCommon}")
    private int caravanLowPriceCommon;

    @Value("${caravanHighPriceCommon}")
    private int caravanHighPriceCommon;

    public String getWhiteStyle() {
        return stylesRepository.findByName("White")
                .orElseThrow(() -> new RuntimeException("Can't find 'White' style"))
                .getName();
    }

    public void buyStyle(String id) {
        User user = null;
        try {
            user = userUtil.getUser();
            Style style = stylesRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find style"));
            if (userCoinsUtil.withdrawMoney(user, style.getPrice())) {
                user.getStyles().add(style);
                user.getUserSettings().setStyleId(style.getId());
                userSettingsRepository.save(user.getUserSettings());
                return;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't buy style");
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }


    public void setStyle(String style) {
        User user = null;
        try {
            user = userUtil.getUser();
            Optional<Style> styleOptional = user.getStyles()
                    .stream()
                    .filter(v -> v.getName().equals(style))
                    .findFirst();
            if (styleOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find style");
            }
            long styleId = styleOptional.get().getId();
            if (styleId != user.getUserSettings().getStyleId()) {
                user.getUserSettings().setStyleId(styleId);
                userSettingsRepository.save(user.getUserSettings());
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public String getStyleReward(User user, Rarity caravanRarity, double chanceMultiplier) {
        Rarity rarity = rarityUtil.generateRarity(rarityUtil.getMinRarity(caravanRarity), caravanRarity, chanceMultiplier);
        List<Style> allStylesByCurrentRarity = stylesRepository.findAllByRarity(rarity);
        List<Style> targetStyles = new ArrayList<>();

        allStylesByCurrentRarity.forEach(style -> {
            if (!user.getStyles().contains(style)) targetStyles.add(style);
        });

        if (!targetStyles.isEmpty()) {
            int index = randomUtil.nextInt(targetStyles.size());
            Style style = targetStyles.get(index);
            user.getStyles().add(style);
            return convertStyleToString(user, style, false);
        } else {
            return getDuplicateStyleReward(user, allStylesByCurrentRarity);
        }
    }

    private String getDuplicateStyleReward(User user, List<Style> allStylesByCurrentRarity) {
        int index = randomUtil.nextInt(allStylesByCurrentRarity.size());
        Style style = allStylesByCurrentRarity.get(index);
        setPriceIfFreeStyle(user, style);
        int duplicateValue = style.getPrice() / caravanDuplicatePriceDelimiter;
        style.setPrice(duplicateValue);
        userCoinsUtil.addCoins(user, duplicateValue);
        return convertStyleToString(user, style, true);
    }

    private void setPriceIfFreeStyle(User user, Style style) {
        if (style.getPrice() == 0) {
            int price = randomUtil.getIntWithBounds(caravanLowPriceCommon, caravanHighPriceCommon);
            double realPrice = userCoinsUtil.calcCoinsRewardNoModificator(user, price);
            style.setPrice((int) realPrice);
        }
    }


    private String convertStyleToString(User user, Style style, boolean duplicate) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "style");
        map.put("name", style.getName());
        map.put("rarity", style.getRarity());
        map.put("price", style.getPrice());
        map.put("exp", 0);
        map.put("premiumUser", user.isPremiumUser());
        map.put("nickname", user.getNickname());
        if (duplicate) map.put("duplicate", true);
        return new Gson().toJson(map);
    }

    public void addNewStyleStub() {
        Style style;
        style = new Style();
        style.setRarity(Rarity.COMMON);
        style.setPrice(1);
        style.setName("name");
        stylesRepository.save(style);
    }
}
