package ru.sovaowltv.service.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
@Slf4j
public class DataFieldsChecker {
    public void checkDataReg(Map<String, Object> stringObjectMap) {
        boolean res = stringObjectMap.containsKey("login")
                && stringObjectMap.containsKey("password")
                && stringObjectMap.containsKey("email")
                && stringObjectMap.containsKey("gender")
                && stringObjectMap.containsKey("rules");
        if (!res)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request. Cant find parameter");
    }

    void checkDataMinSetFromO2Auth(Map<String, Object> stringObjectMap) {
        boolean res = stringObjectMap.containsKey("login")
                && stringObjectMap.containsKey("gender")
                && stringObjectMap.containsKey("rules");
        if (stringObjectMap.get("needEmail") == Boolean.TRUE) {
            res = res && stringObjectMap.containsKey("email");
        }
        if (!res)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request. Cant find parameter");
    }

    public void checkFeedbackMessage(Map<String, Object> map) {
        if (isIncorrect(map, "message") || isIncorrect(map, "theme")) {
            log.error("feedback service -> message or theme not found {}", map);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request. message or theme not found");
        }
    }

    private boolean isIncorrect(Map<String, Object> map, String key) {
        return String.valueOf(map.get(key)).isEmpty()
                || String.valueOf(map.get(key)).equalsIgnoreCase("null");
    }
}
