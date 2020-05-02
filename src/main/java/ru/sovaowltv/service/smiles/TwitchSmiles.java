package ru.sovaowltv.service.smiles;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.io.URLConnectionPrepare;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:api/twitch.yml")
@Slf4j
public class TwitchSmiles extends SmileAbstract {
    private final SmilesUtil smilesUtil;

    private final URLConnectionPrepare urlConnectionPrepare;

    @Value("${twitch_smilesLink}")
    private String smilesLink;

    @Value("${twitch_clientId}")
    private String clientId;

    @Value("${twitch_SmilesUnparsed}")
    private String twitchSmilesUnparsed;

    @Value("${twitch_SmilesParsed}")
    private String twitchSmilesParsed;

    @Override
    public void initSmiles() {
        smilesUtil.smilesInit(twitchSmilesParsed, smiles);
    }

    @Override
    public void parseSmiles() {
        smilesUtil.makeSureFilesWithSmilesExists(twitchSmilesParsed, twitchSmilesUnparsed);
        Path path = Paths.get(twitchSmilesUnparsed);
        try {
            readJsonFromStream(path.toFile().getAbsoluteFile().getAbsolutePath());
        } catch (Exception e) {
            log.error("can't read twitch smiles parsed", e);
        }
    }

    public void readJsonFromStream(String path) throws IOException {
        try (JsonReader jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            while (true) {
                JsonToken nextToken = jsonReader.peek();

                if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
                    jsonReader.beginObject();
                } else if (JsonToken.NAME.equals(nextToken)) {
                    String name = jsonReader.nextName();
                    log.debug("Token KEY >>>> " + name);
                } else if (JsonToken.STRING.equals(nextToken)) {
                    String value = jsonReader.nextString();
                    log.debug("Token Value >>>> " + value);
                } else if (JsonToken.NUMBER.equals(nextToken)) {
                    long value = jsonReader.nextLong();
                    log.debug("Token Value >>>> " + value);
                } else if (JsonToken.NULL.equals(nextToken)) {
                    jsonReader.nextNull();
                    log.debug("Token Value >>>> null");
                } else if (JsonToken.END_OBJECT.equals(nextToken)) {
                    jsonReader.endObject();
                } else if (JsonToken.BEGIN_ARRAY.equals(nextToken)) {
                    readArray(jsonReader);
                } else if (JsonToken.END_DOCUMENT.equals(nextToken)) {
                    return;
                } else {
                    log.error("UNKNOWN TOKEN = " + nextToken);
                    throw new RuntimeException("Parse failed");
                }
            }
        }
    }

    private void readArray(JsonReader jsonReader) {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(twitchSmilesParsed))) {
            log.info("ARRAY FOUND");
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();

                jsonReader.nextName();
                long id = jsonReader.nextLong();

                jsonReader.nextName();
                String regex = jsonReader.nextString();

                bufferedWriter.write(id + " " + regex);
                bufferedWriter.newLine();

                skipOtherFields(jsonReader);
                if (jsonReader.peek().equals(JsonToken.END_DOCUMENT)) {
                    break;
                }
                jsonReader.endObject();

            }
            jsonReader.endArray();
        } catch (Exception e) {
            log.error("error while reading array", e);
        }
    }

    private void skipOtherFields(JsonReader jsonReader) throws IOException {
        int count = 1;
        while (true) {
            JsonToken nextToken = jsonReader.peek();

            if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
                count++;
                jsonReader.beginObject();
            } else if (JsonToken.NAME.equals(nextToken)) {
                jsonReader.nextName();
            } else if (JsonToken.STRING.equals(nextToken)) {
                jsonReader.nextString();
            } else if (JsonToken.NUMBER.equals(nextToken)) {
                jsonReader.nextLong();
            } else if (JsonToken.NULL.equals(nextToken)) {
                jsonReader.nextNull();
            } else if (JsonToken.END_OBJECT.equals(nextToken)) {
                if (--count == 0) return;
                jsonReader.endObject();
            } else if (JsonToken.BEGIN_ARRAY.equals(nextToken)) {
                readArray(jsonReader);
            } else if (JsonToken.END_ARRAY.equals(nextToken)) {
                jsonReader.endArray();
            } else if (JsonToken.END_DOCUMENT.equals(nextToken)) {
                return;
            } else {
                log.error("UNKNOWN TOKEN = " + nextToken);
                throw new RuntimeException("Parse failed");
            }
        }
    }


    @Override
    public void loadSmiles() {
        smilesUtil.makeSureFileWithSmilesExists(twitchSmilesUnparsed);
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(smilesLink);

        connection.addRequestProperty("Client-ID", clientId);
        connection.addRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
        log.info("loadSmiles connection.getContentLength() = " + connection.getContentLength());

        try(InputStream inputStream = connection.getInputStream()) {
            Files.write(Paths.get(twitchSmilesUnparsed), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);

            log.info("reading twitch smiles");
            double readed = 0;
            int total = connection.getContentLength();
            int percent = 0;
            int tempPercent;
            while (true) {
                byte[] bytes = inputStream.readNBytes(80000);
                Files.write(Paths.get(twitchSmilesUnparsed), bytes, StandardOpenOption.APPEND);

                readed += bytes.length;
                tempPercent = (int) (readed * 1.0 / total * 100);
                if (tempPercent != percent) {
                    percent = tempPercent;
                    log.info(percent + "%");
                }

                if (bytes.length == 0) {
                    log.info("reading complete");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("can't load twitch smiles", e);
        }
    }

    @Override
    public boolean isSmile(String smile) {
        return smiles.containsKey(smile);
    }

    @Override
    public String getSmile(String smile) {
        return smiles.get(smile);
    }

    @Override
    public boolean canUseSmile(String smile) {
        return true;
    }
}
