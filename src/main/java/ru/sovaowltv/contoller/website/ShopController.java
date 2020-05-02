package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.shop.ShopUtil;
import ru.sovaowltv.service.smiles.SmilesUtil;
import ru.sovaowltv.service.styles.StyleUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserExpUtil;
import ru.sovaowltv.service.user.params.UserPremiumUtil;

@Controller
@RequiredArgsConstructor
public class ShopController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final SmilesUtil smilesUtil;
    private final UserUtil userUtil;
    private final UserExpUtil userExpUtil;
    private final UserPremiumUtil userPremiumUtil;
    private final StyleUtil styleUtil;
    private final ShopUtil shopUtil;

    @GetMapping("/shop")
    public String getShopPage(Model model) {
        return shopUtil.prepareShopPage(model);
    }

    @PostMapping("/shop/buy/premium")
    @ResponseStatus(HttpStatus.OK)
    public void buyPremium() {
        User user = null;
        try {
            user = userUtil.getUser();
            userPremiumUtil.buyPremium(user, 1);
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    @PostMapping("/shop/buy/smile/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void buySmile(@PathVariable String id) {
        smilesUtil.buySmileById(id);
    }

    @PostMapping("/shop/buy/level")
    @ResponseStatus(HttpStatus.OK)
    public void buyLevel() {
        userExpUtil.buyOneLevel();
    }

    @PostMapping("/shop/buy/style/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void buyStyle(@PathVariable String id) {
        styleUtil.buyStyle(id);
    }
}
