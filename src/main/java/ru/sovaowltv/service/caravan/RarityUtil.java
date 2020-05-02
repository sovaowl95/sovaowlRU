package ru.sovaowltv.service.caravan;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.service.unclassified.RandomUtil;

import static ru.sovaowltv.model.shop.Rarity.*;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class RarityUtil {
    private final RandomUtil randomUtil;

    @Value("${randomRarityANCIENT}")
    private double randomRarityANCIENT;

    @Value("${randomRarityLEGENDARY}")
    private double randomRarityLEGENDARY;

    @Value("${randomRarityEPIC}")
    private double randomRarityEPIC;

    @Value("${randomRarityRARE}")
    private double randomRarityRARE;

    public Rarity generateRarity(Rarity minRarity, Rarity maxRarity, double chanceMultiplier) {
        double randomValue = randomUtil.getDoubleWithBounds(0, 100) / chanceMultiplier;
        switch (maxRarity) {
            case ANCIENT:
                if (randomValue < randomRarityANCIENT) return ANCIENT;
                if (minRarity == ANCIENT) return ANCIENT;
            case LEGENDARY:
                if (randomValue < randomRarityLEGENDARY) return LEGENDARY;
                if (minRarity == LEGENDARY) return LEGENDARY;
            case EPIC:
                if (randomValue < randomRarityEPIC) return EPIC;
                if (minRarity == EPIC) return EPIC;
            case RARE:
                if (randomValue < randomRarityRARE) return Rarity.RARE;
                if (minRarity == Rarity.RARE) return Rarity.RARE;
            case COMMON:
            default:
                return Rarity.COMMON;
        }
    }

    public Rarity getMinRarity(Rarity caravanRarity) {
        switch (caravanRarity) {
            case ANCIENT:
                return Rarity.LEGENDARY;
            case LEGENDARY:
                return Rarity.EPIC;
            case EPIC:
                return Rarity.RARE;
            case RARE:
            case COMMON:
            default:
                return Rarity.COMMON;
        }
    }

    int changeRarityToPrice(Rarity rarity) {
        switch (rarity) {
            case ANCIENT:
                return 250;
            case LEGENDARY:
                return 200;
            case EPIC:
                return 150;
            case RARE:
                return 100;
            case COMMON:
                return 50;
            default:
                return 0;
        }
    }

    public int changeRarityToQualityInc(Rarity caravanRarity) {
        switch (caravanRarity) {
            case ANCIENT:
                return 6;
            case LEGENDARY:
                return 5;
            case EPIC:
                return 4;
            case RARE:
                return 3;
            case COMMON:
            default:
                return 2;
        }
    }
}