package ru.sovaowltv.contoller.website.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.factorys.StreamFactory;
import ru.sovaowltv.service.notifications.NotificationUtil;
import ru.sovaowltv.service.stream.StreamSettingsUtil;
import ru.sovaowltv.service.stream.StreamUtil;

@Controller
@RequiredArgsConstructor
public class StreamSettingsController {
    private final StreamUtil streamUtil;
    private final NotificationUtil notificationUtil;
    private final StreamSettingsUtil streamSettingsUtil;

    private final StreamFactory streamFactory;

    @PostMapping("/{streamName}/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteStream(@PathVariable String streamName) {
        streamSettingsUtil.deleteStream(streamName);
    }

    @PostMapping("/createStream")
    @ResponseStatus(HttpStatus.OK)
    public void createStream() {
        streamFactory.createStream();
    }

    @PostMapping("/notifyThemAll")
    @ResponseStatus(HttpStatus.OK)
    public void notifyThemAll() {
        Stream stream = streamUtil.getStreamByAuthContext();
        notificationUtil.notifyAll(stream);
    }

    @PostMapping("/stream/settings/changeStatus")
    @ResponseStatus(HttpStatus.OK)
    public void changeStatus(@RequestBody String body) {
        Stream stream = streamUtil.getStreamByAuthContext();
        streamSettingsUtil.changeStreamStatus(stream, body);
    }


    //todo: ANOTHER API SERVICE
    @PostMapping("/stream/settings/changeStreamName")
    @ResponseStatus(HttpStatus.OK)
    public void changeStreamName(@RequestBody String json) {
        streamSettingsUtil.changeStreamName(json);
    }

    //todo: ANOTHER API SERVICE
    @PostMapping("/stream/settings/changeStreamGame")
    @ResponseStatus(HttpStatus.OK)
    public void changeGame(@RequestBody String json) {
        streamSettingsUtil.changeGame(json);
    }

    @PostMapping("/stream/settings/changeChatDailyInfo")
    @ResponseStatus(HttpStatus.OK)
    public void changeChatDailyInfo(@RequestBody String json) {
        streamSettingsUtil.changeChatDailyInfo(json);
    }
}