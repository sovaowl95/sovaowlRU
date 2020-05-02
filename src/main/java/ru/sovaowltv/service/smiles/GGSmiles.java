package ru.sovaowltv.service.smiles;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:api/gg.yml")
@Slf4j
public class GGSmiles extends SmileAbstract {
    private final SmilesUtil smilesUtil;

    private final DataExtractor dataExtractor;
    private final IOExtractor ioExtractor;

    private final URLConnectionPrepare urlConnectionPrepare;

    @Value("${gg_SmilesLink}")
    private String smilesLink;

    @Value("${gg_SmilesUnparsed}")
    private String ggSmilesUnparsed;

    @Value("${gg_SmilesParsed}")
    private String ggSmilesParsed;

    @Override
    public void initSmiles() {
        smilesUtil.smilesInit(ggSmilesParsed, smiles);
    }

    @Override
    public void parseSmiles() {
        smilesUtil.makeSureFileWithSmilesExists(ggSmilesParsed);
        Path path = Paths.get(ggSmilesUnparsed);
        try (BufferedReader bufferedReader = Files.newBufferedReader(path);
             BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(ggSmilesParsed))) {
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                JsonObject jObject = dataExtractor.extractJsonFromString(input);
                String key = jObject.getAsJsonPrimitive("key").getAsString();
                JsonObject urls = jObject.getAsJsonObject("urls");

                String memberName;
                if (urls.has("gif") && !urls.get("gif").isJsonNull()) {
                    memberName = "gif";
                } else if (urls.has("big") && !urls.get("big").isJsonNull()) {
                    memberName = "big";
                } else {
                    memberName = "img";
                }

                JsonObject asJsonObject = urls.getAsJsonObject();
                String url = asJsonObject.get(memberName).getAsString();
                url = url.replace("/", "%")
                        .replaceFirst("https:", "");

                bufferedWriter.write(url + " :" + key + ":");
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (Exception e) {
            log.error("can't parse gg smiles", e);
        }
    }

    @Override
    public void loadSmiles() {
        smilesUtil.makeSureFilesWithSmilesExists(ggSmilesParsed, ggSmilesUnparsed);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(ggSmilesUnparsed))) {
            String tempUrl = smilesLink;
            while (true) {
                HttpsURLConnection connection = urlConnectionPrepare.getConnection(tempUrl);
                JsonObject jObject = ioExtractor.extractJsonObject(connection);
                JsonObject jLinks = jObject.getAsJsonObject("_links");
                try {
                    tempUrl = jLinks.getAsJsonObject("next").getAsJsonPrimitive("href").getAsString();
                } catch (Exception e) {
                    tempUrl = "FINISH";
                }

                JsonObject jEmbedded = jObject.getAsJsonObject("_embedded");
                JsonArray smiles = jEmbedded.getAsJsonArray("smiles");

                smiles.forEach(v -> {
                    try {
                        bufferedWriter.write(v.toString());
                        bufferedWriter.newLine();
                    } catch (IOException e) {
                        log.error("GGSmiles loadSmiles -> write error", e);
                    }
                });

                if (tempUrl.equalsIgnoreCase("FINISH"))
                    break;
            }
            bufferedWriter.flush();
        } catch (Exception e) {
            log.error("error load gg smiles", e);
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
