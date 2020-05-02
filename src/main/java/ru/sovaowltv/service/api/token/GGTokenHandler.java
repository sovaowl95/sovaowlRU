package ru.sovaowltv.service.api.token;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.service.api.requsts.GGRequestUtil;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;
import ru.sovaowltv.service.user.UserGGUtil;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;

@Component
@PropertySource("classpath:api/gg.yml")
@RequiredArgsConstructor
@Slf4j
public class GGTokenHandler {
    private final UserGGUtil userGGUtil;
    private final GGRequestUtil ggRequestUtil;

    private final IOExtractor ioExtractor;

    private final URLConnectionPrepare urlConnectionPrepare;

    public synchronized boolean refresh(UserGG userGG) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresIn = userGG.getExpiresIn();
        if (now.plusMinutes(1).isAfter(expiresIn)) {
            refreshToken(userGG);
        }
        return getChatToken(userGG);
    }

    private boolean getChatToken(UserGG userGG) {
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://api2.goodgame.ru/chat/token?access_token=" + userGG.getAccessToken());
        connection.setRequestProperty("Accept", "application/json");
        JsonObject jsonObject = ioExtractor.extractJsonObject(connection);
        userGGUtil.setChatTokenAndSave(userGG, jsonObject.get("chat_token").getAsString());
        return true;
    }

    private void refreshToken(UserGG userGG) {
        String bodyPrepared = ggRequestUtil.getBodyForTokenRefresh(userGG);
        HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://api2.goodgame.ru/oauth");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        urlConnectionPrepare.setPOSTAndBody(bodyPrepared, connection);
        userGGUtil.setAccessTokenAndSave(userGG, connection);
    }
}
