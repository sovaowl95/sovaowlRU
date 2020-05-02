package ru.sovaowltv.service.factorys.google;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersGoogleRepository;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserGoogleFactory {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UsersGoogleRepository usersGoogleRepository;

    public void linkAndSaveUserGoogle(User user, Map<String, Object> data) {
        UserGoogle userGoogle = new UserGoogle();

        userGoogle.setSub(String.valueOf(data.get("sub")));
        userGoogle.setAccessToken(String.valueOf(data.get("access_token")));
        String refreshToken = String.valueOf(data.get("refresh_token"));
        if (refreshToken != null) userGoogle.setRefreshToken(refreshToken);
        LocalDateTime time = LocalDateTime.parse(String.valueOf(data.get("exp")));
        userGoogle.setExpiresIn(time);
        userGoogle.setId(user.getId());
        userGoogle.setCorrupted(false);

        user.setUserGoogle(userGoogle);
        userGoogle.setUser(user);

        usersGoogleRepository.save(userGoogle);
        usersRepositoryHandler.saveAndFree(user);
    }
}
