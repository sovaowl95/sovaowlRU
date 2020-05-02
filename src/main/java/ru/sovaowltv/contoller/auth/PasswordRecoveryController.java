package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.email.EmailUtil;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.unclassified.LanguageUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PasswordRecoveryController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final LanguageUtil languageUtil;
    private final UserUtil userUtil;
    private final SecurityUtil securityUtil;
    private final EmailUtil emailUtil;

    private final DataExtractor dataExtractor;


    @GetMapping("/recoverypassword")
    public String getPasswordRecoveryPage() {
        return "email/recoverypassword";
    }

    @PostMapping("/recoverypassword")
    @ResponseStatus(HttpStatus.OK)
    public void getPasswordRecoveryMail(@RequestBody String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        String email = String.valueOf(map.get("email")).toLowerCase();
        String subject = languageUtil.getStringFor("recoveryForm.subject");
        User user = null;
        try {
            user = usersRepositoryHandler.getUserByEmail(email);
            emailUtil.sendPasswordRecovery(email, subject, user);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }


    @GetMapping("/profile/recover/{id}/{code}")
    public String getActivateFromEmail(@PathVariable(required = false) String code,
                                       @PathVariable(required = false) String id,
                                       HttpServletRequest httpServletRequest,
                                       Model model) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserById(id);
            if (user.getRecoveryToken().equalsIgnoreCase(code)) {
                securityUtil.generateSecTokenStateForSession(httpServletRequest.getSession(), model);
                httpServletRequest.getSession().setAttribute("id", user.getId());
                return "email/inputNewPassword";
            } else {
                return "email/recoveryPasswordHands";
            }
        } finally {
            usersRepositoryHandler.free(user);
        }
    }


    @PostMapping("/profile/recover/newPassword")
    @ResponseStatus(HttpStatus.OK)
    public void changePass(@RequestBody String json, HttpServletRequest httpServletRequest) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        HttpSession session = httpServletRequest.getSession();

        String secTokenState = String.valueOf(map.get("secTokenState"));
        securityUtil.equalSessionToken(secTokenState, session);

        String id = session.getAttribute("id").toString();
        String password = String.valueOf(map.get("password"));
        userUtil.setNewPassword(password, id);
    }


    @GetMapping("/profile/recover")
    public String getActivateFromEmail() {
        return "email/recoveryPasswordHands";
    }


    @GetMapping("/recoveryPassword/success")
    public String getSuccess() {
        return "email/recoveryPasswordSuccess";
    }
}
