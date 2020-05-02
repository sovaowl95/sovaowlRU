package ru.sovaowltv.service.factorys.vk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserVK;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersVKRepository;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserVKFactory {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UsersVKRepository usersVKRepository;

    public void createAndSaveUserVK(User user, Map<String, Object> data) {
        UserVK userVK = new UserVK();
        userVK.setSub(String.valueOf(((Double) data.get("user_id")).intValue()));
        userVK.setAccessToken(data.get("access_token").toString());

        String expiresIn = data.get("expires_in").toString();
        double seconds = Double.parseDouble(expiresIn);
        userVK.setExpiresIn(LocalDateTime.now().plusSeconds((long) seconds));

        userVK.setId(user.getId());
        userVK.setUser(user);
        user.setUserVK(userVK);

        usersVKRepository.save(userVK);
        usersRepositoryHandler.saveUser(user);
    }
}
