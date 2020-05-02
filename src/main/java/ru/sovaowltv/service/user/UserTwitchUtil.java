package ru.sovaowltv.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;
import ru.sovaowltv.service.factorys.twitch.UserTwitchFactory;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.TwitchStreamUtil;
import ru.sovaowltv.service.unclassified.RegFormInitModel;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/twitch.yml")
public class UserTwitchUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UsersTwitchRepository usersTwitchRepository;

    private final SecurityUtil securityUtil;
    private final TwitchStreamUtil twitchStreamUtil;

    private final UserTwitchFactory userTwitchFactory;

    private final RegFormInitModel regFormInitModel;

    @Value("${twitch_clientId}")
    private String clientId;

    @Value("${twitch_iss}")
    private String iss;

    public Optional<UserTwitch> getUserTwitchById(long id) {
        return usersTwitchRepository.findById(id);
    }

    public Optional<UserTwitch> getUserTwitchBySub(String sub) {
        return usersTwitchRepository.findBySub(sub);
    }

    public User getUserByUserTwitchChannelId(String channelId) {
        Optional<UserTwitch> userTwitchOptional = usersTwitchRepository.findByUserTwitchChannelId(channelId);
        if (userTwitchOptional.isEmpty()) {
            log.error("no twitch user " + channelId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "getUserByUserTwitchChannelId");
        }
        return userTwitchOptional.get().getUser();
    }


    public String createAccount(HttpSession session, Model model, Map<String, Object> mapTokens) {
        securityUtil.generateSecTokenStateForSession(session, model);
        redesignMap(mapTokens);
        regFormInitModel.initModelForRegForm(session, model, mapTokens);
        return "oauth2Reg";
    }

    private void redesignMap(Map<String, Object> targetMap) {
        targetMap.put("from", "Twitch");
        targetMap.put("link", "/api/auth/twitch");
        targetMap.put("needEmail", !targetMap.containsKey("email"));
        targetMap.put("username", targetMap.get("preferred_username"));
    }


    public boolean isNeedVerification(Map<String, Object> data) {
        return !data.containsKey("email_verified") || !data.get("email_verified").toString().equalsIgnoreCase("true");
    }


    public void checkAUDAndISS(Map<String, Object> mapTokens) {
        if (!mapTokens.get("aud").toString().equalsIgnoreCase(clientId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Security error");
        if (!mapTokens.get("iss").toString().equalsIgnoreCase(iss))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Security error");
    }


    public String linkUser(User user, Map<String, Object> mapTokens) {
        if (user.getUserTwitch() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already linked user");
        }
        userTwitchFactory.createAndSaveUserTwitch(user, mapTokens);
        twitchStreamUtil.solveNewTwitchUserConnection(user);
        return "redirect:/profile/settings";
    }

    public void deleteTwitchUser(User user) {
        UserTwitch userTwitch = user.getUserTwitch();
        userTwitch.setUser(null);
        user.setUserTwitch(null);

        usersTwitchRepository.delete(userTwitch);
        usersRepositoryHandler.saveUser(user);
    }

    public void setCorrupted(UserTwitch apiUser) {
        apiUser.setCorrupted(true);
        usersTwitchRepository.save(apiUser);
    }
}