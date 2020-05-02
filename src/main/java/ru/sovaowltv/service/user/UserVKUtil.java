package ru.sovaowltv.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apiauth.UserVK;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersVKRepository;
import ru.sovaowltv.service.factorys.vk.UserVKFactory;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.unclassified.RegFormInitModel;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/vk.yml")
public class UserVKUtil {
    private final UsersVKRepository usersVKRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;

    private final SecurityUtil securityUtil;
    private final UserVKFactory userVKFactory;
    private final RegFormInitModel regFormInitModel;

    public Optional<UserVK> getUserVKBySub(String sub) {
        return usersVKRepository.findBySub(sub);
    }

    public String createAccount(HttpSession session, Model model, Map<String, Object> mapTokens) {
        securityUtil.generateSecTokenStateForSession(session, model);
        redesignMap(mapTokens);
        regFormInitModel.initModelForRegForm(session, model, mapTokens);
        return "oauth2Reg";
    }

    private void redesignMap(Map<String, Object> targetMap) {
        targetMap.put("from", "VK");
        targetMap.put("link", "/api/auth/vk");
        targetMap.put("needEmail", !targetMap.containsKey("email"));
        targetMap.put("username", "");
    }

    public String linkUser(User user, Map<String, Object> mapTokens) {
        if (user.getUserVK() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already linked user");
        }
        userVKFactory.createAndSaveUserVK(user, mapTokens);
        return "redirect:/profile/settings";
    }

    public void deleteVKUser(User user) {
        UserVK userVK = user.getUserVK();
        userVK.setUser(null);
        user.setUserVK(null);

        usersVKRepository.delete(userVK);
        usersRepositoryHandler.saveUser(user);
    }
}