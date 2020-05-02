package ru.sovaowltv.contoller.website.stream;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.service.stream.StreamSettingsUtil;

@Controller
@RequiredArgsConstructor
public class StreamSettingsDiscordNotification {
    private final StreamSettingsUtil streamSettingsUtil;

    @PostMapping("/stream/settings/changeDiscordNotification")
    @ResponseStatus(HttpStatus.OK)
    public void changeDiscordNotification(@RequestBody String json) {
        streamSettingsUtil.changeDiscordNotification(json);
    }

    @PostMapping("/stream/settings/changeDiscordText")
    @ResponseStatus(HttpStatus.OK)
    public void changeDiscordText(@RequestBody String json) {
        streamSettingsUtil.changeDiscordText(json);
    }
}
