package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.exceptions.user.UserVKNotFoundException;
import ru.sovaowltv.model.apiauth.UserVK;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.requsts.VKRequest;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.factorys.UserFactory;
import ru.sovaowltv.service.factorys.vk.UserVKFactory;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UserVKUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VKOauthController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final SecurityUtil securityUtil;
    private final UserUtil userUtil;
    private final UserVKUtil userVKUtil;

    private final UserVKFactory userVKFactory;
    private final UserFactory userFactory;

    private final VKRequest vkRequest;
    private final DataExtractor dataExtractor;

    @GetMapping("/api/auth/vk")
    public String vkO2Auth(HttpSession session,
                           @RequestParam(required = false) String state,
                           @RequestParam(required = false) String code,
                           Model model) {
        securityUtil.verifyToken(session, state);

        Map<String, Object> mapToken = vkRequest.changeCodeToToken(code);

        String userId = mapToken.get("user_id").toString();

        Optional<UserVK> userVKOptional = userVKUtil.getUserVKBySub(userId);
        if (userVKOptional.isPresent()) {
            userUtil.setAuthContext(userVKOptional.get().getUser());
            return "redirect:/";
        } else {
            Optional<User> userOptional = Optional.empty();
            try {
                userOptional = userUtil.getUserOptionalFromContext();
                if (userOptional.isPresent()) {
                    return userVKUtil.linkUser(userOptional.get(), mapToken);
                } else {
                    return userVKUtil.createAccount(session, model, mapToken);
                }
            } finally {
                userOptional.ifPresent(usersRepositoryHandler::saveAndFree);
            }
        }
    }

    @PostMapping("/api/auth/vk")
    public String postApiRegVK(@RequestBody String json,
                               HttpSession session,
                               @RequestHeader String secTokenState) {
        securityUtil.verifyToken(session, secTokenState);

        Map<String, Object> mapBody = dataExtractor.joinJsonAndSessionData(json, session);
        User user = userFactory.createUserFromMap(mapBody, true);
        userVKFactory.createAndSaveUserVK(user, mapBody);

        session.removeAttribute("data");
        return "redirect:/";
    }

    @PostMapping("/api/auth/vk/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeVKToken() {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getUserVK() == null)
                throw new UserVKNotFoundException("Can't find vk link");
            userVKUtil.deleteVKUser(user);
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }
}