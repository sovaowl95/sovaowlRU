package ru.sovaowltv.service.io;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.service.data.DataExtractor;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IOExtractor {
    private final DataExtractor dataExtractor;

    public Map<String, Object> extractDataFromResponse(HttpsURLConnection urlConnection) {
        try {
            InputStream inputStream = getInputOrErrorStream(urlConnection);
            String response = readResponse(inputStream);
            Map<String, Object> map = dataExtractor.extractMapFromString(response);
            log.debug(map.toString());
            return map;
        } catch (IOException e) {
            log.error("cant' extract data object: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't extract JSON object");
        }
    }

    public JsonObject extractJsonObject(HttpsURLConnection urlConnection) {
        String response = null;
        try (InputStream inputStream = getInputOrErrorStream(urlConnection)) {
            response = readResponse(inputStream);
            return dataExtractor.extractJsonFromString(response);
        } catch (IOException e) {
            log.error("cant' extract JSON object: " + response, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't extract JSON object");
        }
    }

    private InputStream getInputOrErrorStream(HttpsURLConnection urlConnection) throws IOException {
        int responseCode = urlConnection.getResponseCode();
        return responseCode >= 300 ? urlConnection.getErrorStream() : urlConnection.getInputStream();
    }

    private String readResponse(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String input;
            while ((input = bufferedReader.readLine()) != null)
                sb.append(input);
            return sb.toString();
        }
    }
}
