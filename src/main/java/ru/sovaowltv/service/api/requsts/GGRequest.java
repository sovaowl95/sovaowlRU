package ru.sovaowltv.service.api.requsts;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;

import javax.net.ssl.HttpsURLConnection;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/gg.yml")
public class GGRequest {
    private final GGRequestUtil ggRequestUtil;

    private final IOExtractor ioExtractor;

    private final URLConnectionPrepare urlConnectionPrepare;

    public Map<String, Object> changeCodeToToken(String code) {
        HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://api2.goodgame.ru/oauth");
        ggRequestUtil.setConnectionPropertiesCodeToToken(code, connection);
        return ioExtractor.extractDataFromResponse(connection);
    }

    public Map<String, Object> changeTokenToData(Map<String, Object> mapTokens) {

        String token = mapTokens.get("access_token").toString();
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://api2.goodgame.ru/info"
                        + "?"
                        + "access_token=" + token);
        JsonObject asJsonObject = ioExtractor.extractJsonObject(connection);
        return ggRequestUtil.fillMap(asJsonObject);
    }

    public void revokeGGToken(String accessToken) {
        //gg have not revoke functional. L O L
    }
}
