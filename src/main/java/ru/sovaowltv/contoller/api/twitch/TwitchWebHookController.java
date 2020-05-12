package ru.sovaowltv.contoller.api.twitch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.model.api.twitch.TwitchWebHook;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.api.webhooks.TwitchWebHookUtil;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.notifications.NotificationUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TwitchWebHookController {
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final TwitchWebHookUtil twitchWebhookUtil;
    private final NotificationUtil notificationUtil;

    private final DataExtractor dataExtractor;

    @GetMapping("/api/twitch/webhooks/*")
    @ResponseBody
    public String getWebHook(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter("hub.challenge");
    }

    @PostMapping("/api/twitch/webhooks/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void twitchWebHookCallback(@RequestBody String body, @PathVariable Long id) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(body);
        log.info("twitch webHook {}", jsonObject);
        JsonArray jArr = jsonObject.getAsJsonArray("data");
        if (jArr.size() != 0) { //online
            TwitchWebHook twitchWebhook = new TwitchWebHook(jArr.get(0).getAsJsonObject(), dataExtractor);
            if (twitchWebhookUtil.alreadySolvedHook(twitchWebhook)) return;
            Stream stream = twitchWebhookUtil.setStreamSettingByWebHookAndGetStream(twitchWebhook);
            notificationUtil.notifyAll(stream);
        } else {
            Stream stream = streamRepositoryHandler.getStreamById(id);
            stream.setLive(false);
            streamRepositoryHandler.save(stream);
        }
    }
}
