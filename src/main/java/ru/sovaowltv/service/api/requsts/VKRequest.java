package ru.sovaowltv.service.api.requsts;

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
@PropertySource("classpath:api/vk.yml")
public class VKRequest {
    private final IOExtractor ioExtractor;

    private final URLConnectionPrepare urlConnectionPrepare;

    @Value("${vk_clientId}")
    private String clientId;

    @Value("${vk_clientSecret}")
    private String clientSecret;

    @Value("${vk_redirectUri}")
    private String redirectUri;


    public Map<String, Object> changeCodeToToken(String code) {
        HttpsURLConnection connection = urlConnectionPrepare
                .getConnection("https://oauth.vk.com/access_token" + "?" +
                        "client_id=" + clientId + "&" +
                        "client_secret=" + clientSecret + "&" +
                        "code=" + code + "&" +
                        "redirect_uri=" + redirectUri);
        return ioExtractor.extractDataFromResponse(connection);
    }
}
