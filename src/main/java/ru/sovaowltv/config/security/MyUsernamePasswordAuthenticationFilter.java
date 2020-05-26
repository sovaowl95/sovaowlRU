package ru.sovaowltv.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.sovaowltv.service.data.DataExtractor;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class MyUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    private final DataExtractor dataExtractor;

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Map<String, Object> stringObjectMap = extractDataToMap(request);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = getAuthToken(stringObjectMap);
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (authenticate.isAuthenticated()) {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticate);
            SecurityContextHolder.setContext(securityContext);
            rememberUser(request, response, stringObjectMap);
        }
        return authenticate;
    }

    @Nullable
    private Map<String, Object> extractDataToMap(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = dataExtractor.extractMapFromString(request.getReader().readLine());
        } catch (IOException e) {
            log.error("extractDataToMap ", e);
        }
        return stringObjectMap;
    }

    @NotNull
    private UsernamePasswordAuthenticationToken getAuthToken(Map<String, Object> map) {
        return new UsernamePasswordAuthenticationToken(map.get("login"), map.get("password"));
    }

    private void rememberUser(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        if (map == null) return;
        if (map.get("remember").equals("true")) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("SESSION")) {
                    cookie.setMaxAge(Integer.MAX_VALUE);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
    }
}