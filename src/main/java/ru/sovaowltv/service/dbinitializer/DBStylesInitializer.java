package ru.sovaowltv.service.dbinitializer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Style;
import ru.sovaowltv.repositories.website.StylesRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DBStylesInitializer {
    private final StylesRepository stylesRepository;

    void initStyles() {
        List<Style> styles = stylesRepository.findAll();
        if (styles.isEmpty()) {
            saveAncientStyles();
            saveLegendaryStyles();
            saveEpicStyles();
            saveRareStyles();
            saveCommonStyles();
        }
    }

    private void saveAncientStyles() {
        saveStyle("Neon", 50000, Rarity.ANCIENT);
//        saveStyle("Rust", 50000, Rarity.ANCIENT);
    }

    private void saveLegendaryStyles() {
        saveStyle("Flower", 20000, Rarity.LEGENDARY);
        saveStyle("Flame", 20000, Rarity.LEGENDARY);
        saveStyle("Rainbow", 20000, Rarity.LEGENDARY);
    }

    private void saveEpicStyles() {
        saveStyle("BrokenRainbow", 10000, Rarity.EPIC);
        saveStyle("EightBit", 10000, Rarity.EPIC);
    }

    private void saveRareStyles() {
        saveStyle("Orange", 5000, Rarity.RARE);
        saveStyle("Yellow", 5000, Rarity.RARE);
        saveStyle("LightGreen", 5000, Rarity.RARE);
    }

    private void saveCommonStyles() {
        saveStyle("White", 0, Rarity.COMMON);
        saveStyle("Blue", 0, Rarity.COMMON);
        saveStyle("Pink", 0, Rarity.COMMON);
    }

    private void saveStyle(String neon, int i, Rarity ancient) {
        Style style = new Style();
        style.setName(neon);
        style.setPrice(i);
        style.setRarity(ancient);
        stylesRepository.save(style);
    }
}
