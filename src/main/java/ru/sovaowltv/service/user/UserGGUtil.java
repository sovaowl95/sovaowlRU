package ru.sovaowltv.service.user;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersGGRepository;
import ru.sovaowltv.service.factorys.gg.UserGGFactory;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.stream.GGStreamUtil;
import ru.sovaowltv.service.unclassified.RegFormInitModel;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserGGUtil {
    private final UsersGGRepository usersGGRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UserUtil userUtil;
    private final GGStreamUtil ggStreamUtil;

    private final UserGGFactory userGGFactory;

    private final IOExtractor ioExtractor;

    private final RegFormInitModel regFormInitModel;

    public Optional<UserGG> getUserGGBySub(String userId) {
        return usersGGRepository.findBySub(userId);
    }

    public String nextStepRegistration(HttpSession session, Model model, Map<String, Object> data) {
        data.put("from", "Goodgame");
        data.put("link", "/api/auth/gg");
        data.put("needEmail", true);
        regFormInitModel.initModelForRegForm(session, model, data);
        return "oauth2Reg";
    }

    public String linkUser(Map<String, Object> data) {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getUserGG() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "USER ALREADY LINKED");
            }
            userGGFactory.createAndSaveUserGG(user, data);
            ggStreamUtil.solveNewGGUserConnection(user);

            return "redirect:/profile/settings";
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public String authUser(User user) {
        userUtil.setAuthContext(user);
        return "redirect:/";
    }

    public void deleteAndSaveGGUser(User user) {
        UserGG userGG = user.getUserGG();
        userGG.setUser(null);
        user.setUserGG(null);

        usersGGRepository.delete(userGG);
        usersRepositoryHandler.saveUser(user);
    }

    public void setChatTokenAndSave(UserGG userGG, String chatToken) {
        userGG.setChatToken(chatToken);
        usersGGRepository.save(userGG);
    }

    public void setAccessTokenAndSave(UserGG userGG, HttpsURLConnection connection) {
        JsonObject jsonObject = ioExtractor.extractJsonObject(connection);

        String accessToken = jsonObject.get("access_token").getAsString();
        String expiresIn = jsonObject.get("expires_in").getAsString();
        String refreshToken = jsonObject.get("refresh_token").getAsString();

        userGG.setAccessToken(accessToken);
        double seconds = Double.parseDouble(expiresIn);
        userGG.setExpiresIn(LocalDateTime.now().plusSeconds((long) seconds));
        userGG.setRefreshToken(refreshToken);
        usersGGRepository.save(userGG);
    }
}
