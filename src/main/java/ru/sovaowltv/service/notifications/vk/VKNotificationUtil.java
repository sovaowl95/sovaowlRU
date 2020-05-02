package ru.sovaowltv.service.notifications.vk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apinotification.NotificationFor;
import ru.sovaowltv.model.apinotification.VKNotification;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;
import ru.sovaowltv.service.unclassified.KeyWordsReplacerUtil;

import javax.net.ssl.HttpsURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
@Slf4j
public class VKNotificationUtil extends ListenerAdapter {
    private final KeyWordsReplacerUtil keyWordsReplacerUtil;

    private final IOExtractor ioExtractor;
    private final URLConnectionPrepare urlConnectionPrepare;

    public void sendNotification(Stream stream) {
        try {
            VKNotification vkNotification = stream.getVkNotification();
            if (vkNotification == null) {
                log.debug("Can't find vk for stream " + stream.getUser().getNickname());
                return;
            }
            send(stream, vkNotification);
        } catch (Exception e) {
            log.error("vk send notification error", e);
        }
    }

    private void send(Stream stream, VKNotification vkNotification) {
        String message = keyWordsReplacerUtil.replaceAllKeyWords(stream, vkNotification.getText(), NotificationFor.VK);

        String token = vkNotification.getKey();
        String groupId = vkNotification.getGroupId();

        if (token == null || token.isEmpty() || groupId == null || groupId.isEmpty()) {
            log.info("can't find token or group id for vk notification for stream: " + stream.getId());
            return;
        }
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://api.vk.com/method/wall.post" +
                        "?" +
                        "owner_id=" + groupId +
                        "&" +
                        "from_group=1" +
                        "&" +
                        "message=" + URLEncoder.encode(message, UTF_8) +
                        "&" +
                        "signed=1" +
                        "&" +
                        "mark_as_ads=0" +
                        "&" +
                        "close_comments=0" +
                        "&" +
                        "mute_notifications=0" +
                        "&" +
                        "v=5.70" +
                        "&" +
                        "access_token=" + token);

        Map<String, Object> map = ioExtractor.extractDataFromResponse(connection);
        log.info(map.toString());
    }
}