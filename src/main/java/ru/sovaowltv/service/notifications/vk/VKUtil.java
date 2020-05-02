package ru.sovaowltv.service.notifications.vk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apinotification.VKNotification;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.VKNotificationRepository;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserUtil;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VKUtil {
    private final VKNotificationRepository vkNotificationRepository;
    private final VKNotificationFactory vkNotificationFactory;

    private final UserUtil userUtil;
    private final StreamUtil streamUtil;

    public static final String REDIRECT_PROFILE_SETTINGS = "redirect:/profile/settings";

    public String addVKNotification() {
        User user = userUtil.getUserREADONLY();
        Stream stream = streamUtil.getStreamByUserNickname(user.getNickname());
        VKNotification vkNotification = vkNotificationFactory.createVKNotification(stream);
        stream.setVkNotification(vkNotification);
        streamUtil.save(stream);
        return REDIRECT_PROFILE_SETTINGS;
    }

    public String removeVKNotification() {
        User user = userUtil.getUserREADONLY();
        Stream stream = streamUtil.getStreamByUserNickname(user.getNickname());
        VKNotification vkNotification = stream.getVkNotification();
        stream.setVkNotification(null);
        vkNotificationRepository.delete(vkNotification);
        streamUtil.save(stream);
        return REDIRECT_PROFILE_SETTINGS;
    }

    public String setParams(Map<String, Object> map) {
        User user = userUtil.getUserREADONLY();
        Stream stream = streamUtil.getStreamByUserNickname(user.getNickname());
        VKNotification vkNotification = stream.getVkNotification();
        if (map.containsKey("group")) vkNotification.setGroupId(map.get("group").toString());
        if (map.containsKey("key")) vkNotification.setKey(map.get("key").toString());
        vkNotificationRepository.save(vkNotification);
        streamUtil.save(stream);
        return REDIRECT_PROFILE_SETTINGS;
    }
}
