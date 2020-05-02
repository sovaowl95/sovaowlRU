package ru.sovaowltv.service.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

@Component
public class RsaVerifier {
    public org.springframework.security.jwt.crypto.sign.RsaVerifier verifier(String kid, String url) throws Exception {
        JwkProvider provider = new UrlJwkProvider(new URL(url));
        Jwk jwk = provider.get(kid);
        return new org.springframework.security.jwt.crypto.sign.RsaVerifier((RSAPublicKey) jwk.getPublicKey());
    }
}
