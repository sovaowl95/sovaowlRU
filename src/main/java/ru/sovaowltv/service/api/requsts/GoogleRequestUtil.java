package ru.sovaowltv.service.api.requsts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.security.RsaVerifier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/google.yml")
public class GoogleRequestUtil {
    private final DataExtractor dataExtractor;
    private final RsaVerifier rsaVerifier;

    @Value("${google_clientId}")
    private String clientId;

    @Value("${google_clientSecret}")
    private String clientSecret;

    @Value("${google_redirectUri}")
    private String redirectUri;

    @Value("${google_keysLink}")
    private String googleKeysLink;

    Map<String, Object> fillMap(Map<String, Object> stringObjectMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", stringObjectMap.get("access_token").toString());
        map.put("refresh_token", stringObjectMap.get("refresh_token").toString());

        Jwt idToken = testJWT(stringObjectMap);
        if (idToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CLAIMS NULL");
        }
        Map<String, Object> hashMap = dataExtractor.extractMapFromString(idToken.getClaims());

        map.put("email", String.valueOf(hashMap.get("email")));
        map.put("sub", String.valueOf(hashMap.get("sub")));

        Double iat = Double.parseDouble(hashMap.get("iat").toString());
        Double exp = Double.parseDouble(hashMap.get("exp").toString());
        LocalDateTime time = LocalDateTime.now().plusSeconds((long) (exp - iat));
        map.put("exp", time.toString());

        return map;
    }

    String generateBodyCodeToToken(String code) {
        return "code=" + code + "&" +
                "client_id=" + clientId + "&" +
                "client_secret=" + clientSecret + "&" +
                "redirect_uri=" + redirectUri + "&" +
                "grant_type=authorization_code" + "&" +
                "access_type=offline";
    }


    private Jwt testJWT(Map<String, Object> stringObjectMap) {
        String idToken = String.valueOf(stringObjectMap.get("id_token"));
        String kid = JwtHelper.headers(idToken).get("kid");
        try {
            return JwtHelper.decodeAndVerify(idToken, rsaVerifier.verifier(kid, googleKeysLink));
        } catch (Exception e) {
            log.error("can't decode or verify JWT", e);
            return null;
        }
    }

    public String prepareBodyForRefreshToken(UserGoogle userGoogle) {
        return "client_id=" + clientId + "&" +
                "client_secret=" + clientSecret + "&" +
                "grant_type=refresh_token" + "&" +
                "access_type=offline" + "&" +
                "refresh_token=" + userGoogle.getRefreshToken();
    }
}
