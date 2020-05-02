package ru.sovaowltv.service.shop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Smile;
import ru.sovaowltv.model.shop.Style;
import ru.sovaowltv.repositories.website.SmilesRepository;
import ru.sovaowltv.repositories.website.StylesRepository;
import ru.sovaowltv.service.unclassified.Constants;
import ru.sovaowltv.service.user.UserUtil;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopUtil {
    private final SmilesRepository smilesRepository;
    private final StylesRepository stylesRepository;

    private final UserUtil userUtil;

    private final Constants constants;

    public String prepareShopPage(Model model) {
        userUtil.setUserInModelREADONLY(model);
        model.addAttribute("premiumPrice", constants.getPremiumPrice());

        model.addAttribute("smiles", getSmilesList());
        model.addAttribute("styles", getStylesList());

        model.addAttribute("rarityOrder", Rarity.values());

        model.addAttribute("levelPrice", constants.getLevelPrice());
        model.addAttribute("levelExpMultiplier", constants.getLevelExpMultiplier());

        return "shop";
    }


    //todo: optimization
    public List<Smile> getSmilesList() {
        List<Smile> smileList = smilesRepository.findAll();
        smileList.sort(Comparator.comparingInt(s -> s.getRarity().ordinal()));
        return smileList;
    }

    public List<Style> getStylesList() {
        List<Style> styleList = stylesRepository.findAll();
        styleList.sort(Comparator.comparingInt(s -> s.getRarity().ordinal()));
        return styleList;
    }
}
