package ru.sovaowltv.service.api.requsts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;

import javax.net.ssl.HttpsURLConnection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/gg.yml")
public class GGRequestUtil {
    private final DataExtractor dataExtractor;
    private final URLConnectionPrepare urlConnectionPrepare;

    @Value("${gg_clientId}")
    private String clientId;

    @Value("${gg_clientSecret}")
    private String clientSecret;

    private static final String CHANNEL = "channel";

    Map<String, Object> fillMap(JsonObject asJsonObject) {
        Map<String, Object> map = new HashMap<>();
        JsonObject user = asJsonObject.getAsJsonObject("user");
        String userId = user.get("user_id").getAsString();
        String username = user.get("username").getAsString();
        map.put("user_id", userId);
        map.put("username", username);

        JsonObject jObjChannel = asJsonObject.getAsJsonObject(CHANNEL);

        String channelJ = dataExtractor.getPrimitiveAsStringFromJson(jObjChannel, CHANNEL);
        String channelId = dataExtractor.getPrimitiveAsStringFromJson(jObjChannel, "channel_id");
        String src = dataExtractor.getPrimitiveAsStringFromJson(jObjChannel, "src");

        map.put(CHANNEL, channelJ);
        map.put("channel_id", channelId);
        map.put("src", src);
        return map;
    }


    void setConnectionPropertiesCodeToToken(String code, HttpsURLConnection urlConnection) {
        String body = prepareRequestBody(code);
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnectionPrepare.setPOSTAndBody(body, urlConnection);
    }

    private String prepareRequestBody(String code) {
        Map<String, Object> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", "authorization_code");
        body.put("redirect_uri", "https://sovaowl.ru/api/auth/gg");
        body.put("code", code);
        return new Gson().toJson(body);
    }

    public String getBodyForTokenRefresh(UserGG userGG) {
        Map<String, Object> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", userGG.getRefreshToken());
        return new Gson().toJson(body);
    }
}