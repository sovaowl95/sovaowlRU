package ru.sovaowltv.service.api.requsts;

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
@PropertySource("classpath:api/google.yml")
public class GoogleRequest {
    private final GoogleRequestUtil googleRequestUtil;

    private final IOExtractor ioExtractor;

    private final URLConnectionPrepare urlConnectionPrepare;

    public Map<String, Object> changeCodeToToken(String code) {
        HttpsURLConnection urlConnection = urlConnectionPrepare.getConnection(
                "https://www.googleapis.com/oauth2/v4/token");

        String body = googleRequestUtil.generateBodyCodeToToken(code);
        urlConnectionPrepare.setPOSTAndBody(body, urlConnection);

        Map<String, Object> map = ioExtractor.extractDataFromResponse(urlConnection);
        return googleRequestUtil.fillMap(map);
    }

    public boolean revokeGoogleToken(String token) {
        try {
            HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                    "https://accounts.google.com/o/oauth2/revoke"
                            + "?"
                            + "token=" + token);
            connection.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.connect();
            return true;
        } catch (Exception e) {
            log.error("error in google token revoking", e);
            return false;
        }
    }
}
