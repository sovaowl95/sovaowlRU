package ru.sovaowltv.service.factorys.gg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersGGRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserGGFactory {
    private final UsersGGRepository usersGGRepository;

    public void createAndSaveUserGG(User user, Map<String, Object> data) {
        UserGG userGG = new UserGG();

        userGG.setSub(String.valueOf(data.get("user_id")));
        userGG.setScope(String.valueOf(data.get("scope")));
        userGG.setAccessToken(String.valueOf(data.get("access_token")));
        userGG.setRefreshToken(String.valueOf(data.get("refresh_token")));
        String expiresIn = String.valueOf(data.get("expires_in"));
        double seconds = Double.parseDouble(expiresIn);
        userGG.setExpiresIn(LocalDateTime.now().plusSeconds((long) seconds));
        userGG.setNick(String.valueOf(data.get("username")));
        userGG.setId(user.getId());
        userGG.setCorrupted(false);

        userGG.setChannel(String.valueOf(data.get("channel")));
        userGG.setChannelId(String.valueOf(data.get("channel_id")));
        userGG.setSrc(String.valueOf(data.get("src")));

        userGG.setUser(user);
        user.setUserGG(userGG);

        usersGGRepository.save(userGG);
    }
}
