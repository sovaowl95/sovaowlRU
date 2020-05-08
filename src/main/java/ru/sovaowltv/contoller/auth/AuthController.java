package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.data.DataFieldsChecker;
import ru.sovaowltv.service.factorys.UserFactory;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.user.UserUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserUtil userUtil;
    private final SecurityUtil securityUtil;

    private final UserFactory userFactory;

    private final DataExtractor dataExtractor;

    private final DataFieldsChecker dataFieldsChecker;

    private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";

    @GetMapping("/login")
    public String getLoginPage(HttpServletRequest request, Model model) {
        try {
            userUtil.getUserREADONLY();
            return LOGOUT;
        } catch (UserNotFoundException e) {
            securityUtil.generateSecTokenStateForSession(request.getSession(), model);
            return LOGIN;
        }
    }

    @PostMapping("/logout")
    public String doLogout() {
        return LOGIN;
    }

    @PostMapping("/reg")
    public String regPost(@RequestBody String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        dataFieldsChecker.checkDataReg(map);
        userFactory.createUserFromMapWithNeedVerification(map);
        return LOGIN;
    }
}
