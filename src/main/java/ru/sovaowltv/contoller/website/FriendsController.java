package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sovaowltv.service.user.UserUtil;

@Controller
@RequiredArgsConstructor
public class FriendsController {
    private final UserUtil userUtil;

    @GetMapping("/friends")
    public String getFriends(Model model) {
        userUtil.setUserInModelREADONLY(model);
        return "friends";
    }
}
