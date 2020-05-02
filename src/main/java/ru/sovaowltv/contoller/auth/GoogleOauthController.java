package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.exceptions.user.EmailAlreadyInUseException;
import ru.sovaowltv.exceptions.user.UserGoogleNotFoundException;
import ru.sovaowltv.exceptions.user.UserGoogleRevokeFoundException;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.requsts.GoogleRequest;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.factorys.UserFactory;
import ru.sovaowltv.service.factorys.google.UserGoogleFactory;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.YTStreamUtil;
import ru.sovaowltv.service.user.UserGoogleUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GoogleOauthController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final SecurityUtil securityUtil;
    private final UserUtil userUtil;
    private final YTStreamUtil ytStreamUtil;
    private final UserGoogleUtil userGoogleUtil;
    private final GoogleRequest googleRequest;

    private final UserFactory userFactory;
    private final UserGoogleFactory userGoogleFactory;

    private final DataExtractor dataExtractor;

    @GetMapping("/api/auth/google")
    public String google(HttpSession session,
                         @RequestParam(required = false) String state,
                         @RequestParam(required = false) String code,
                         Model model) {
        securityUtil.verifyToken(session, state);
        Map<String, Object> mapTokens = googleRequest.changeCodeToToken(code);

        String sub = ((String) mapTokens.get("sub"));
        Optional<UserGoogle> userGoogleOptional = userGoogleUtil.getGoogleUserBySub(sub);
        if (userGoogleOptional.isPresent()) {
            return userGoogleUtil.authUser(userGoogleOptional.get().getUser());
        } else {
            Optional<User> userOptional = userUtil.getUserOptionalFromContext();
            if (userOptional.isPresent()) {
                usersRepositoryHandler.saveAndFree(userOptional.get());
                return userGoogleUtil.linkUser(mapTokens);
            } else {
                return userGoogleUtil.createAccount(session, model, mapTokens);
            }
        }
    }


    @PostMapping("/api/auth/google")
    public String postApiRegGoogle(@RequestBody String json,
                                   HttpSession session,
                                   @RequestHeader String secTokenState) {
        securityUtil.equalSessionToken(secTokenState, session);
        Map<String, Object> data = dataExtractor.joinJsonAndSessionData(json, session);

        User userByEmail = null;
        try {
            String email = data.get("email").toString();
            userByEmail = usersRepositoryHandler.getUserByEmail(email);
            if (userByEmail.getUserGoogle() != null)
                throw new EmailAlreadyInUseException("User with this email already have google acc " + email);
            userGoogleFactory.linkAndSaveUserGoogle(userByEmail, data);
            return userGoogleUtil.authUser(userByEmail);

        } catch (UserNotFoundException e) {
            log.info("user not found. create new user");
            User user = userFactory.createUserFromMap(data, false);
            userGoogleFactory.linkAndSaveUserGoogle(user, data);
            return userGoogleUtil.authUser(user);
        } finally {
            usersRepositoryHandler.saveAndFree(userByEmail);
        }
    }


    @PostMapping("/api/auth/google/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeGoogleToken() {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getUserGoogle() == null)
                throw new UserGoogleNotFoundException("Can't find google link");

            if (!googleRequest.revokeGoogleToken(user.getUserGoogle().getAccessToken())) {
                throw new UserGoogleRevokeFoundException("cant revoke google token");
            }
            ytStreamUtil.solveGoogleUserDisconnected(user);
            userGoogleUtil.deleteGoogleUser(user);
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }
}
