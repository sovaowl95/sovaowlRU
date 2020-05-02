package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.exceptions.user.EmailAlreadyInUseException;
import ru.sovaowltv.exceptions.user.UserGGNotFoundException;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.requsts.GGRequest;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.factorys.UserFactory;
import ru.sovaowltv.service.factorys.gg.UserGGFactory;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.GGStreamUtil;
import ru.sovaowltv.service.user.UserGGUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GGOauthController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final SecurityUtil securityUtil;
    private final UserUtil userUtil;
    private final UserGGUtil userGGUtil;
    private final GGStreamUtil ggStreamUtil;

    private final UserFactory userFactory;
    private final UserGGFactory userGGFactory;

    private final GGRequest ggRequest;
    private final DataExtractor dataExtractor;

    @GetMapping("/api/auth/gg")
    public String ggO2Auth(HttpSession session,
                           @RequestParam(required = false) String state,
                           @RequestParam(required = false) String code,
                           Model model) {
        securityUtil.verifyToken(session, state);

        Map<String, Object> map = ggRequest.changeCodeToToken(code);
        Map<String, Object> data = ggRequest.changeTokenToData(map);
        map.forEach(data::put);

        String userId = data.get("user_id").toString();
        Optional<UserGG> userGG = userGGUtil.getUserGGBySub(userId);
        if (userGG.isPresent()) {
            return userGGUtil.authUser(userGG.get().getUser());
        } else {
            Optional<User> userOptional = Optional.empty();
            try {
                userOptional = userUtil.getUserOptionalFromContext();
                if (userOptional.isPresent()) {
                    return userGGUtil.linkUser(data);
                } else {
                    return userGGUtil.nextStepRegistration(session, model, data);
                }
            } finally {
                userOptional.ifPresent(usersRepositoryHandler::saveAndFree);
            }
        }
    }

    @PostMapping("/api/auth/gg")
    public String ggRegUser(@RequestBody String json,
                            HttpSession session,
                            @RequestHeader String secTokenState) {
        securityUtil.verifyToken(session, secTokenState);

        Map<String, Object> data = dataExtractor.joinJsonAndSessionData(json, session);

        User userByEmail = null;
        try {
            String email = data.get("email").toString();
            userByEmail = usersRepositoryHandler.getUserByEmail(email);
            if (userByEmail.getUserGG() != null)
                throw new EmailAlreadyInUseException("User with this email already have gg acc " + email);
            userGGFactory.createAndSaveUserGG(userByEmail, data);
            return userGGUtil.authUser(userByEmail);
        } catch (UserNotFoundException e) {
            log.info("user not found. create new user");
            User user = userFactory.createUserFromMap(data, true);
            userGGFactory.createAndSaveUserGG(user, data);
            usersRepositoryHandler.saveUser(user);
            return userGGUtil.authUser(user);
        } finally {
            usersRepositoryHandler.saveAndFree(userByEmail);
        }
    }

    @PostMapping("/api/auth/gg/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeGGToken() {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getUserGG() == null)
                throw new UserGGNotFoundException("Can't find gg link");

            ggRequest.revokeGGToken(user.getUserGG().getAccessToken());
            ggStreamUtil.solveGGUserDisconnected(user);
            userGGUtil.deleteAndSaveGGUser(user);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }
}
