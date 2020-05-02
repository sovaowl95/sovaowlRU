package ru.sovaowltv.service.api.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.service.api.requsts.GoogleRequestUtil;
import ru.sovaowltv.service.io.URLConnectionPrepare;
import ru.sovaowltv.service.user.UserGoogleUtil;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;

@Component
@PropertySource("classpath:api/google.yml")
@RequiredArgsConstructor
public class YTTokenHandler {
    private final GoogleRequestUtil googleRequestUtil;
    private final UserGoogleUtil userGoogleUtil;

    private final URLConnectionPrepare urlConnectionPrepare;

    @Value("${google_RefreshTokenLink}")
    private String googleRefreshTokenLink;

    public boolean refresh(UserGoogle userGoogle) {
        LocalDateTime expiresIn = userGoogle.getExpiresIn();
        LocalDateTime now = LocalDateTime.now();
        if (now.plusMinutes(1).isAfter(expiresIn)) {
            return refreshToken(userGoogle);
        }
        return true;
    }

    private boolean refreshToken(UserGoogle userGoogle) {
        String body = googleRequestUtil.prepareBodyForRefreshToken(userGoogle);
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(googleRefreshTokenLink);
        urlConnectionPrepare.setPOSTAndBody(body, connection);
        userGoogleUtil.updateUserAndSave(userGoogle, connection);
        return true;
    }
}