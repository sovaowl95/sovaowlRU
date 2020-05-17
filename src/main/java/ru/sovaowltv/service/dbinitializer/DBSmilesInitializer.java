package ru.sovaowltv.service.dbinitializer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Smile;
import ru.sovaowltv.repositories.website.SmilesRepository;
import ru.sovaowltv.service.smiles.GGSmiles;
import ru.sovaowltv.service.smiles.TwitchSmiles;
import ru.sovaowltv.service.smiles.WebSiteSmileAbstract;
import ru.sovaowltv.service.smiles.YTSmiles;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DBSmilesInitializer {
    private final SmilesRepository smilesRepository;

    private final TwitchSmiles twitchSmiles;
    private final GGSmiles ggSmilesUtil;
    private final YTSmiles ytSmiles;
    private final WebSiteSmileAbstract webSiteSmilesUtil;

    public void initSmiles() {
        List<Smile> smiles = smilesRepository.findAll();
        if (smiles.isEmpty()) {
            saveAncientSmiles();
            saveLegendarySmiles();
            saveEpicSmiles();
            saveRareSmiles();
            saveCommonSmiles();
        }
        webSiteSmilesUtil.initSiteSmiles();
        twitchSmiles.initSmiles();
        ggSmilesUtil.initSmiles();
        ytSmiles.initSmiles();
    }

    private void saveAncientSmiles() {
        saveSmile("AyaHeart1.png", "AyaHeart1", 20000, Rarity.ANCIENT);
        saveSmile("AyaHeart2.png", "AyaHeart2", 20000, Rarity.ANCIENT);
    }

    private void saveLegendarySmiles() {
        saveSmile("AyaW76.png", "AyaW76", 15000, Rarity.LEGENDARY);
        saveSmile("KarGame.png", "KarGame", 15000, Rarity.LEGENDARY);
        saveSmile("Slug.gif", "Slug", 15000, Rarity.LEGENDARY);
    }

    private void saveEpicSmiles() {
        saveSmile("Dance.gif", "Dance", 10000, Rarity.EPIC);
        saveSmile("Sex.png", "Sex", 10000, Rarity.EPIC);
    }

    private void saveRareSmiles() {
        saveSmile("AyaCry.png", "AyaCry", 5000, Rarity.RARE);
        saveSmile("Grumpy.png", "Grumpy", 5000, Rarity.RARE);
        saveSmile("KarHi.png", "KarHi", 5000, Rarity.RARE);
        saveSmile("KarLaugh.png", "KarLaugh", 5000, Rarity.RARE);
        saveSmile("KarWidow.png", "KarWidow", 5000, Rarity.RARE);
        saveSmile("Peka.png", "Peka", 5000, Rarity.RARE);
        saveSmile("Pled.png", "Pled", 5000, Rarity.RARE);
    }

    private void saveCommonSmiles() {
        saveSmile("AyaLucio.png", "AyaLucio", 0, Rarity.COMMON);
        saveSmile("KarHeart.png", "KarHeart", 0, Rarity.COMMON);
        saveSmile("Rage.png", "Rage", 0, Rarity.COMMON);
        saveSmile("Slowpoke.png", "Slowpoke", 0, Rarity.COMMON);
        saveSmile("Strawberry.png", "Strawberry", 0, Rarity.COMMON);

        saveSmile("bananya.png", "bananya", 0, Rarity.COMMON);
        saveSmile("rero.gif", "rero", 0, Rarity.COMMON);
        saveSmile("sex1.svg", "sex1", 0, Rarity.COMMON);
        saveSmile("sex2.svg", "sex2", 0, Rarity.COMMON);
    }

    private void saveSmile(String link, String smileName, int price, Rarity rarity) {
        Smile smile = new Smile();
        smile.setLink(link);
        smile.setName(smileName);
        smile.setPrice(price);
        smile.setRarity(rarity);
        smilesRepository.save(smile);
    }
}
