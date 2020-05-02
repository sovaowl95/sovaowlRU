package ru.sovaowltv.service.api.requsts;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.security.RsaVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/twitch.yml")
public class TwitchRequestUtil {
    private final UsersTwitchRepository usersTwitchRepository;
    private final DataExtractor dataExtractor;
    private final IOExtractor ioExtractor;

    private final RsaVerifier rsaVerifier;

    @Value("${twitch_clientId}")
    private String twitchClientId;

    @Value("${twitch_clientSecret}")
    private String twitchClientSecret;

    @Value("${twitch_keysLink}")
    private String keysLink;

    Map<String, Object> joinData(Map<String, Object> dataFromResponse) {
        HashMap<String, Object> resultMap = new HashMap<>();
        try {
            String idToken = String.valueOf(dataFromResponse.get("id_token"));
            String kid = JwtHelper.headers(idToken).get("kid");
            Jwt tokenDecoded = JwtHelper.decodeAndVerify(idToken, rsaVerifier.verifier(kid, keysLink));
            Map<String, Object> authInfo = dataExtractor.extractMapFromString(tokenDecoded.getClaims());
            authInfo.forEach((k, v) -> resultMap.put(k, String.valueOf(v)));
            dataFromResponse.forEach((k, v) -> resultMap.put(k, String.valueOf(v)));
            return resultMap;
        } catch (Exception e) {
            log.error("extractDataToMap failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
        }
    }


    public String getUriForRefreshToken(String refreshToken) {
        return "https://id.twitch.tv/oauth2/token" + "?" +
                "grant_type=" + "refresh_token" + "&" +
                "refresh_token=" + refreshToken + "&" +
                "client_id=" + twitchClientId + "&" +
                "client_secret=" + twitchClientSecret;
    }

    public String getBodyForWebhookSub(UserTwitch userTwitch, Stream stream) {
        Map<String, Object> data = new HashMap<>();
        data.put("hub.callback", "https://sovaowl.ru/api/twitch/webhooks/" + stream.getId());
        data.put("hub.mode", "subscribe");
        data.put("hub.topic", "https://api.twitch.tv/helix/streams?user_id=" + userTwitch.getUserTwitchChannelId());
        data.put("hub.lease_seconds", 90000);
        //data.put("hub.secret", ); TODO: IMPORTANT!
        return new Gson().toJson(data);
    }


    public void setAuthHeaders(UserTwitch userTwitch, HttpsURLConnection connection) {
        connection.addRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
        connection.addRequestProperty("Authorization", "Bearer " + userTwitch.getAccessToken());
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("Client-ID", twitchClientId);
    }

    public String getChannelId(UserTwitch userTwitch, HttpsURLConnection connection) {
        JsonObject jsonObject = ioExtractor.extractJsonObject(connection);
        JsonArray jArray = jsonObject.getAsJsonArray("data");
        JsonObject jUser = jArray.get(0).getAsJsonObject();
        if (jUser == null) throw new RuntimeException("Can't find User");
        String id = jUser.getAsJsonPrimitive("id").getAsString();
        userTwitch.setUserTwitchChannelId(id);
        usersTwitchRepository.save(userTwitch);
        return id;
    }
}
