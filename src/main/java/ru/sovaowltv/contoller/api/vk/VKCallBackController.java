package ru.sovaowltv.contoller.api.vk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sovaowltv.exceptions.BadParamsException;
import ru.sovaowltv.model.apinotification.VKNotification;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.api.vk.VKCallBackUtil;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VKCallBackController {
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final VKCallBackUtil vkCallBackUtil;
    private final DataExtractor dataExtractor;

    @PostMapping("/api/vk/callback/{streamId}")
    @ResponseBody
    public String getMessageFromGroup(@RequestBody String body, @PathVariable Long streamId) {
        log.info("body = {}", body);
        try {
            Stream stream = streamRepositoryHandler.getStreamById(streamId);
            VKNotification vkNotification = stream.getVkNotification();

            Map<String, Object> map = dataExtractor.extractMapFromString(body);
            if (!vkNotification.getCallbackSecretKey().equalsIgnoreCase(map.get("secret").toString())) {
                log.info("incorrect secret key {} {}",
                        vkNotification.getCallbackResponseKey(),
                        map.get("secret").toString());
                throw new BadParamsException("incorrect secret key");
            }

            String type = map.get("type").toString();
            if ("message_new".equals(type)) {
                return vkCallBackUtil.solveMessageNew(map, stream);
            } else if ("confirmation".equals(type)) {
                return vkNotification.getCallbackResponseKey();
            } else {
                log.error("NOT IMPLEMENTED: " + body);
            }
        } catch (BadParamsException e) {
            log.info("bad params", e);
        } catch (Exception e) {
            log.error("vk callback exception", e);
        }

        return "ok";
    }
}
