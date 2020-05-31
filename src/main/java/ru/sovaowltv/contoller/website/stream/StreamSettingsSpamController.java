package ru.sovaowltv.contoller.website.stream;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.spammer.SpammerSettingsUtil;
import ru.sovaowltv.service.spammer.SpammerUtil;
import ru.sovaowltv.service.stream.StreamUtil;

@Controller
@RequiredArgsConstructor
public class StreamSettingsSpamController {
    private final StreamUtil streamUtil;
    private final SpammerUtil spammerUtil;
    private final SpammerSettingsUtil spammerSettingsUtil;

    private final DataExtractor dataExtractor;

    @PostMapping("/stream/settings/spam/create")
    @ResponseStatus(HttpStatus.OK)
    public void createSpam() {
        Stream stream = streamUtil.getStreamByAuthContext();

        spammerUtil.createSpam(stream);
    }

    @PostMapping("/stream/settings/spam/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSpam(@RequestBody String json) {
        Stream stream = streamUtil.getStreamByAuthContext();
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        long id = Long.parseLong(dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id"));

        spammerUtil.deleteSpam(stream, id);
    }

    @PostMapping("/stream/settings/spam/start/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void startSpam(@PathVariable Long id) {
        Stream stream = streamUtil.getStreamByAuthContext();
        spammerUtil.startSpammer(stream, id);
    }

    @PostMapping("/stream/settings/spam/stop/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void stopSpam(@PathVariable Long id) {
        Stream stream = streamUtil.getStreamByAuthContext();
        spammerUtil.stopSpammer(stream, id);
    }


    @PostMapping("/stream/settings/spam/edit/text")
    @ResponseStatus(HttpStatus.OK)
    public void editSpamText(@RequestBody String json) {
        Stream stream = streamUtil.getStreamByAuthContext();
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        long id = Long.parseLong(dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id"));
        String text = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "text");

        spammerSettingsUtil.editSpamText(stream, id, text);
    }


    @PostMapping("/stream/settings/spam/edit/time")
    @ResponseStatus(HttpStatus.OK)
    public void editSpamTime(@RequestBody String json) {
        Stream stream = streamUtil.getStreamByAuthContext();
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        long id = Long.parseLong(dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id"));
        String time = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "time");

        spammerSettingsUtil.editSpamTime(stream, id, time);
    }

    @PostMapping("/stream/settings/spam/edit/delay")
    @ResponseStatus(HttpStatus.OK)
    public void editSpamDelay(@RequestBody String json) {
        Stream stream = streamUtil.getStreamByAuthContext();
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        long id = Long.parseLong(dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id"));
        String time = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "time");

        spammerSettingsUtil.editSpamDelay(stream, id, time);
    }
}
