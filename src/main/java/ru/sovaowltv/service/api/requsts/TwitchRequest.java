package ru.sovaowltv.service.api.requsts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;

import javax.net.ssl.HttpsURLConnection;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/twitch.yml")
public class TwitchRequest {
    private final TwitchRequestUtil twitchRequestUtil;

    private final IOExtractor ioExtractor;

    private final URLConnectionPrepare urlConnectionPrepare;

    @Value("${twitch_clientId}")
    private String clientId;

    @Value("${twitch_clientSecret}")
    private String clientSecret;

    @Value("${twitch_redirectUri}")
    private String redirectUri;

    public String changeTwitchGameIdToTitle(String gameId) {
        if (gameId == null || gameId.isEmpty()) return "";
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://api.twitch.tv/helix/games?id=" + gameId);
        connection.addRequestProperty("Client-ID", clientId);
        connection.addRequestProperty("Accept", "application/vnd.twitchtv.v5+json");

        JsonObject jObj = ioExtractor.extractJsonObject(connection);
        JsonArray jArr = jObj.getAsJsonArray("data");
        jObj = jArr.get(0).getAsJsonObject();
        return jObj.getAsJsonPrimitive("name").getAsString();
    }

    public Map<String, Object> changeCodeToToken(String code) {
        HttpsURLConnection connection = urlConnectionPrepare
                .getConnection("https://id.twitch.tv/oauth2/token" + "?" +
                        "client_id=" + clientId + "&" +
                        "client_secret=" + clientSecret + "&" +
                        "code=" + code + "&" +
                        "grant_type=authorization_code" + "&" +
                        "redirect_uri=" + redirectUri);
        urlConnectionPrepare.setPOST(connection);
        Map<String, Object> dataFromResponse = ioExtractor.extractDataFromResponse(connection);
        return twitchRequestUtil.joinData(dataFromResponse);

    }


    public void revokeTwitchToken(String token) {
        try {
            HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                    "https://id.twitch.tv/oauth2/revoke" + "?" +
                            "client_id=" + clientId + "&" +
                            "token" + token);
            urlConnectionPrepare.setPOST(connection);
            connection.connect();
        } catch (Exception e) {
            log.error("error in twitch token revoking", e);
        }
    }
}
