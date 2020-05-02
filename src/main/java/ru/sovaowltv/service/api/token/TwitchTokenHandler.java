package ru.sovaowltv.service.api.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;
import ru.sovaowltv.service.api.requsts.TwitchRequestUtil;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@PropertySource("classpath:api/twitch.yml")
@RequiredArgsConstructor
@Slf4j
public class TwitchTokenHandler {
    private final UsersTwitchRepository usersTwitchRepository;
    private final TwitchRequestUtil twitchRequestUtil;

    private final IOExtractor ioExtractor;
    private final URLConnectionPrepare urlConnectionPrepare;

    public boolean refresh(UserTwitch userTwitch) {
        LocalDateTime expiresIn = userTwitch.getExpiresIn();
        LocalDateTime now = LocalDateTime.now();
        if (now.plusMinutes(1).isAfter(expiresIn)) {
            return refreshToken(userTwitch);
        }
        return true;
    }

    private boolean refreshToken(UserTwitch userTwitch) {
        String uri = twitchRequestUtil.getUriForRefreshToken(userTwitch.getRefreshToken());
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(uri);
        urlConnectionPrepare.setPOST(connection);
        return getNewUserTwitchTokenAndSave(userTwitch, connection);
    }

    private boolean getNewUserTwitchTokenAndSave(UserTwitch userTwitch, HttpsURLConnection urlConnection) {
        try {
            Map<String, Object> stringObjectMap = ioExtractor.extractDataFromResponse(urlConnection);
            Object status = stringObjectMap.get("status");
            if (status != null) {
                log.error("Can't refresh twitch token for " + userTwitch.getNick() + "\n" + stringObjectMap.get("message"));
                return false;
            }
            userTwitch.setAccessToken(String.valueOf(stringObjectMap.get("access_token")));
            userTwitch.setRefreshToken(String.valueOf(stringObjectMap.get("refresh_token")));
            Object expiresIn = stringObjectMap.get("expires_in");
            double seconds = Double.parseDouble(String.valueOf(expiresIn));
            userTwitch.setExpiresIn(LocalDateTime.now().plusSeconds((long) seconds));
            userTwitch.setCorrupted(false);
            usersTwitchRepository.save(userTwitch);
            return true;
        } catch (Exception e) {
            log.error("Can't refresh twitch token for " + userTwitch.getNick(), e);
            return false;
        }
    }
}
