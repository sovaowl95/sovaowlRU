package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.user.UserSettingsUtil;
import ru.sovaowltv.service.user.UserUtil;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class ProfileSettingsController {
    private final UserUtil userUtil;
    private final UserSettingsUtil userSettingsUtil;
    private final SecurityUtil securityUtil;

    @GetMapping(value = {"/profile/settings"})
    public String getProfile(Model model, HttpServletRequest httpServletRequest) {
        securityUtil.generateSecTokenStateForSession(httpServletRequest.getSession(), model);
        userUtil.setUserIfExistInModelREADONLY(model);
        return "profileSettingsPage";
    }

    @PostMapping("/profile/settings/icon/add")
    @ResponseStatus(HttpStatus.OK)
    public void addIcon(@RequestParam String name) {
        userSettingsUtil.addIconInActive(name);
    }

    @PostMapping("/profile/settings/icon/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeIcon(@RequestParam String name) {
        userSettingsUtil.removeIconFromActive(name);
    }

    @PostMapping("/profile/settings/icon/clear")
    @ResponseStatus(HttpStatus.OK)
    public void clearIcon() {
        userSettingsUtil.clearIcon();
    }

    @PostMapping("/profile/settings/icon/recalculate")
    @ResponseStatus(HttpStatus.OK)
    public void recalculateIcon() {
        userSettingsUtil.recalculateIcons();
    }
}
