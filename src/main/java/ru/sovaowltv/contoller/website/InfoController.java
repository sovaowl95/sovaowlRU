package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sovaowltv.service.user.UserUtil;

@Controller
@RequiredArgsConstructor
public class InfoController {
    private final UserUtil userUtil;

    @GetMapping("/info")
    public String getInfo(Model model) {
        userUtil.setUserIfExistInModelREADONLY(model);
        return "info";
    }
}
