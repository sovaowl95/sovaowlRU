package ru.sovaowltv.service.factorys.twitch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserTwitchFactory {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UsersTwitchRepository usersTwitchRepository;

    public void createAndSaveUserTwitch(User user, Map<String, Object> data) {
        UserTwitch userTwitch = new UserTwitch();

        userTwitch.setSub(data.get("sub").toString());
        userTwitch.setScope(data.get("scope").toString());
        userTwitch.setAccessToken(data.get("access_token").toString());
        userTwitch.setRefreshToken(data.get("refresh_token").toString());
        String expiresIn = data.get("expires_in").toString();
        double seconds = Double.parseDouble(expiresIn);
        userTwitch.setExpiresIn(LocalDateTime.now().plusSeconds((long) seconds));
        userTwitch.setNick(data.get("preferred_username").toString());
        userTwitch.setId(user.getId());
        userTwitch.setCorrupted(false);

        userTwitch.setUser(user);
        user.setUserTwitch(userTwitch);

        usersTwitchRepository.save(userTwitch);
        usersRepositoryHandler.saveUser(user);
    }
}
