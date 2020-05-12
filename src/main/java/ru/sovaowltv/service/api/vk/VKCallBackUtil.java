package ru.sovaowltv.service.api.vk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.website.VKNotificationRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.io.URLConnectionPrepare;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import javax.net.ssl.HttpsURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
@Slf4j
public class VKCallBackUtil {
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final VKNotificationRepository vkNotificationRepository;

    private final DataExtractor dataExtractor;
    private final IOExtractor ioExtractor;
    private final URLConnectionPrepare urlConnectionPrepare;

    public String solveMessageNew(Map<String, Object> map, Stream stream) {
        Map<String, Object> object
                = dataExtractor.extractMapFromString(map.get("object").toString());

        Map<String, Object> message
                = dataExtractor.extractMapFromString(object.get("message").toString());

        Long fromUserId = ((Double) message.get("from_id")).longValue();
        String text = message.get("text").toString();

        if (text.equalsIgnoreCase("!sub")) {
            sub(fromUserId, stream);
        } else if (text.equalsIgnoreCase("!unsub")) {
            unsub(fromUserId, stream);
        } else {
            log.error("unknown command: ");
            sendErrorMessage(fromUserId, stream);
            return "ok";
        }
        return "ok";
    }

    private void sub(Long fromId, Stream stream) {
        log.info("sub");
        stream.getVkNotification().getVkIds().add(fromId);
        streamRepositoryHandler.save(stream);
        vkNotificationRepository.save(stream.getVkNotification());
        sendConfirmMessage(fromId, stream);
    }

    private void unsub(Long fromId, Stream stream) {
        log.info("unsub");
        stream.getVkNotification().getVkIds().remove(fromId);
        streamRepositoryHandler.save(stream);
        vkNotificationRepository.save(stream.getVkNotification());
        sendConfirmMessage(fromId, stream);
    }

    private void sendConfirmMessage(Long toUserId, Stream stream) {
        sendMessage(toUserId, stream, "ok");
    }

    private void sendErrorMessage(Long toUserId, Stream stream) {
        sendMessage(toUserId, stream, "unknown command. use !sub or !unsub");
    }

    public void sendMessage(Long toUserId, Stream stream, String message) {
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://api.vk.com/method/messages.send" +
                        "?" +
                        "user_id=" + toUserId +
                        "&" +
                        "message=" + URLEncoder.encode(message, UTF_8) +
                        "&" +
                        "v=5.70" +
                        "&" +
                        "access_token=" + stream.getVkNotification().getAccessKey());

        Map<String, Object> map = ioExtractor.extractDataFromResponse(connection);
        log.info(map.toString());
    }
}
