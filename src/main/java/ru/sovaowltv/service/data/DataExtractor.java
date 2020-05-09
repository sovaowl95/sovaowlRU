package ru.sovaowltv.service.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataExtractor {
    private final DataFieldsChecker dataFieldsChecker;

    @SuppressWarnings("unchecked")
    public Map<String, Object> extractMapFromString(String jsonString) {
        return new Gson().fromJson(jsonString, Map.class);
    }

    public Map<String, Object> joinJsonAndSessionData(String json, HttpSession session) {
        Map<String, Object> stringObjectMap = new BasicJsonParser().parseMap(json);
        Map<String, Object> data = extractDataFromSession(session);
        stringObjectMap.forEach(data::put);
        dataFieldsChecker.checkDataMinSetFromO2Auth(data);
        return data;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractDataFromSession(HttpSession session) {
        return (Map<String, Object>) session.getAttribute("data");
    }

    public JsonObject extractJsonFromString(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public Set<String> getSetFromJsonInLoverCase(JsonObject jObject, String param) {
        Set<String> set = new HashSet<>();
        String alias = jObject.getAsJsonPrimitive(param).getAsString();
        for (String s : alias.split(" ")) {
            set.add(s.toLowerCase());
        }
        return set;
    }

    public String getPrimitiveAsStringFromJson(JsonObject jObject, String param) {
        try {
            return jObject.get(param).getAsJsonPrimitive().getAsString();
        } catch (Exception e) {
            log.error("getPrimitiveAsStringFromJson {} {}", jObject, e);
            return null;
        }
    }

    public int getPrimitiveAsIntFromJson(JsonObject jObject, String param) {
        return jObject.get(param).getAsJsonPrimitive().getAsInt();
    }

    public boolean getPrimitiveAsBooleanFromJson(JsonObject jObject, String param) {
        return jObject.get(param).getAsJsonPrimitive().getAsBoolean();
    }
}
