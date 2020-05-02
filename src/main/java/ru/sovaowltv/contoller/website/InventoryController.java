package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sovaowltv.service.user.UserUtil;

@Controller
@RequiredArgsConstructor
public class InventoryController {
    private final UserUtil userUtil;

    @GetMapping("/inventory")
    public String getInv(Model model) {
        userUtil.setUserInModelREADONLY(model);
        return "inventory";
    }
}
