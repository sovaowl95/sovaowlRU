package ru.sovaowltv.model.user;

import lombok.Getter;
import ru.sovaowltv.model.shop.Rarity;

@Getter
public enum Achievements {
    BETA_TESTER(Rarity.ANCIENT, "/img/achievements/BETA_TESTER.png"),
    DONATOR(Rarity.COMMON, "/img/achievements/DONATOR.png"),
    KING(Rarity.RARE, "/img/achievements/KING.png"),

    CARAVAN_10(Rarity.COMMON, "/img/achievements/CARAVAN_10.png"),
    CARAVAN_100(Rarity.RARE, "/img/achievements/CARAVAN_100.png"),
    CARAVAN_1000(Rarity.EPIC, "/img/achievements/CARAVAN_1000.png"),

    HEART(Rarity.ANCIENT, "/img/achievements/HEART.png");

    private final Rarity rarity;
    private final String imgLink;

    Achievements(Rarity rarity, String imgLink) {
        this.rarity = rarity;
        this.imgLink = imgLink;
    }
}
