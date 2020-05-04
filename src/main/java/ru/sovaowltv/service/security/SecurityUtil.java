package ru.sovaowltv.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.exceptions.TokensNotEqualsException;

import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.security.SecureRandom;

@Slf4j
@Service
public class SecurityUtil {
    private final SecureRandom secureRandom = new SecureRandom();

    private String generateOrGetStateToken(HttpSession session) {
        Object secTokenState = session.getAttribute("secTokenState");
        if (secTokenState == null) {
            return new BigInteger(130, secureRandom).toString(32);
        }
        return secTokenState.toString();
    }

    public void generateSecTokenStateForSession(HttpSession session, Model model) {
        String token = generateOrGetStateToken(session);
        session.setAttribute("secTokenState", token);
        model.addAttribute("secTokenState", token);
    }


    public void equalSessionToken(String token, HttpSession session) {
        String sessionToken = generateOrGetStateToken(session);
        if (!token.equalsIgnoreCase(sessionToken))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't get secTokenState. Or wrong");
        session.removeAttribute("secTokenState");
    }

    public void verifyToken(HttpSession session, String state) {
        boolean tokenEquals = session.getAttribute("secTokenState").equals(state);
        if (!tokenEquals) {
            log.debug("TokensNotEqualsException session and state:");
            log.debug(session.getAttribute("secTokenState").toString());
            log.debug(state);
            throw new TokensNotEqualsException("Tokens not equals exception");
        }
    }
}
