package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.exceptions.user.EmailAlreadyInUseException;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.exceptions.user.UserTwitchNotFoundException;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.requsts.TwitchRequest;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.factorys.UserFactory;
import ru.sovaowltv.service.factorys.twitch.UserTwitchFactory;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.TwitchStreamUtil;
import ru.sovaowltv.service.user.UserTwitchUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
@Slf4j
public class TwitchOauthController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final SecurityUtil securityUtil;
    private final UserUtil userUtil;
    private final TwitchStreamUtil twitchStreamUtil;
    private final UserTwitchUtil userTwitchUtil;

    private final UserFactory userFactory;
    private final UserTwitchFactory userTwitchFactory;

    private final TwitchRequest twitchRequest;
    private final DataExtractor dataExtractor;

    @GetMapping("/api/auth/twitch")
    public String twitchO2Auth(HttpSession session,
                               @RequestParam(required = false) String state,
                               @RequestParam(required = false) String code,
                               Model model) {
        securityUtil.verifyToken(session, state);

        Map<String, Object> mapToken = twitchRequest.changeCodeToToken(code);
        userTwitchUtil.checkAUDAndISS(mapToken);

        String sub = mapToken.get("sub").toString();
        Optional<UserTwitch> userTwitchOptional = userTwitchUtil.getUserTwitchBySub(sub);
        if (userTwitchOptional.isPresent()) {
            userUtil.setAuthContext(userTwitchOptional.get().getUser());
            return "redirect:/";
        } else {
            Optional<User> userOptional = Optional.empty();
            try {
                userOptional = userUtil.getUserOptionalFromContext();
                if (userOptional.isPresent()) {
                    return userTwitchUtil.linkUser(userOptional.get(), mapToken);
                } else {
                    return userTwitchUtil.createAccount(session, model, mapToken);
                }
            } finally {
                userOptional.ifPresent(usersRepositoryHandler::saveAndFree);
            }
        }
    }

    @PostMapping("/api/auth/twitch")
    public String postApiRegTwitch(@RequestBody String json,
                                   HttpSession session,
                                   @RequestHeader String secTokenState) {
        securityUtil.verifyToken(session, secTokenState);

        Map<String, Object> mapBody = dataExtractor.joinJsonAndSessionData(json, session);

        boolean needVerification = userTwitchUtil.isNeedVerification(mapBody);

        User userByEmail = null;
        try {
            String email = mapBody.get("email").toString();
            userByEmail = usersRepositoryHandler.getUserByEmail(email);
            if (userByEmail.getUserTwitch() != null)
                throw new EmailAlreadyInUseException("User with this email already have twitch acc " + email);
            userTwitchFactory.createAndSaveUserTwitch(userByEmail, mapBody);
            userUtil.setAuthContext(userByEmail);
        } catch (UserNotFoundException e) {
            log.info("user not found. create new user");
            User user = userFactory.createUserFromMap(mapBody, needVerification);
            userTwitchFactory.createAndSaveUserTwitch(user, mapBody);
            userUtil.setAuthContext(user);
        } finally {
            usersRepositoryHandler.saveAndFree(userByEmail);
        }

        session.removeAttribute("data");
        return "redirect:/";
    }

    @PostMapping("/api/auth/twitch/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeTwitchToken() {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getUserTwitch() == null)
                throw new UserTwitchNotFoundException("Can't find twitch link");

            twitchRequest.revokeTwitchToken(user.getUserTwitch().getAccessToken());
            twitchStreamUtil.solveTwitchUserDisconnected(user);
            userTwitchUtil.deleteTwitchUser(user);
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }
}