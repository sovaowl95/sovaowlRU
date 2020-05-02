package ru.sovaowltv.service.user;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersGoogleRepository;
import ru.sovaowltv.service.factorys.google.UserGoogleFactory;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.YTStreamUtil;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserGoogleUtil {
    private final UsersGoogleRepository usersGoogleRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;
    private final SecurityUtil securityUtil;
    private final UserUtil userUtil;
    private final YTStreamUtil ytStreamUtil;

    private final UserGoogleFactory userGoogleFactory;

    private final IOExtractor ioExtractor;

    public Optional<UserGoogle> getGoogleUserBySub(String sub) {
        return usersGoogleRepository.findBySub(sub);
    }

    public String authUser(User user) {
        userUtil.setAuthContext(user);
        return "redirect:/";
    }

    public String linkUser(Map<String, Object> mapTokens) {
        User user = userUtil.getUser();
        if (user.getUserGoogle() != null) {
            usersRepositoryHandler.saveAndFree(user);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already linked user");
        }
        userGoogleFactory.linkAndSaveUserGoogle(user, mapTokens);
        ytStreamUtil.solveNewYTUserConnection(user);
        usersRepositoryHandler.saveAndFree(user);
        return "redirect:/profile/settings";
    }

    public String createAccount(HttpSession session, Model model, Map<String, Object> mapTokens) {
        securityUtil.generateSecTokenStateForSession(session, model);
        session.setAttribute("email", mapTokens.get("email"));
        session.setAttribute("sub", mapTokens.get("sub"));
        model.addAttribute("from", "Google");
        model.addAttribute("needEmail", false);
        model.addAttribute("link", "/api/auth/google");
        return "oauth2Reg";
    }


    public void deleteGoogleUser(User user) {
        UserGoogle userGoogle = user.getUserGoogle();
        userGoogle.setUser(null);
        user.setUserGoogle(null);

        usersGoogleRepository.delete(userGoogle);
        usersRepositoryHandler.saveUser(user);
    }

    public void updateUserAndSave(UserGoogle userGoogle, HttpsURLConnection connection) {
        JsonObject jObj = ioExtractor.extractJsonObject(connection);
        String accessToken = jObj.getAsJsonPrimitive("access_token").getAsString();
        String expiresIn = jObj.getAsJsonPrimitive("expires_in").getAsString();

        userGoogle.setAccessToken(accessToken);
        double seconds = Double.parseDouble(expiresIn);
        userGoogle.setExpiresIn(LocalDateTime.now().plusSeconds((long) seconds));

        usersGoogleRepository.save(userGoogle);
    }
}
