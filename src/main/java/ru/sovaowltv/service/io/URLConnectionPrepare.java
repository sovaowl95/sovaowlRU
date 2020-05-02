package ru.sovaowltv.service.io;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@Slf4j
public class URLConnectionPrepare {

    public HttpsURLConnection getConnection(String uri) {
        log.debug(uri);
        try {
            URL url = new URL(uri);
            return ((HttpsURLConnection) url.openConnection());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BAD URL");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANT OPEN CONNECTION");
        }
    }

    public void setPOSTAndBody(String body, HttpsURLConnection urlConnection) {
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.write(body);
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (Exception e) {
            log.error("set request properties and body error", e);
        }
    }

    public void setPOST(HttpsURLConnection urlConnection) {
        try {
            urlConnection.setRequestMethod("POST");
        } catch (Exception e) {
            log.error("set POST method error", e);
        }
    }
}
