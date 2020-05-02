package ru.sovaowltv.service.notifications.vk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apinotification.VKNotification;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.website.VKNotificationRepository;

@Service
@RequiredArgsConstructor
public class VKNotificationFactory {
    private final VKNotificationRepository vkNotificationRepository;

    public VKNotification createVKNotification(Stream stream) {
        VKNotification vkNotification = new VKNotification();
        vkNotification.setGroupId("");
        vkNotification.setKey("");
        vkNotification.setText(getDefaultText(stream.getUser().isMale()));
        vkNotificationRepository.save(vkNotification);
        return vkNotification;
    }

    private String getDefaultText(boolean male) {
        String begin = male ? "начал" : "начала";
        return "{user} " + begin + " трансляцию" + "{n}" +
                "Название: {streamName}" + "{n}" +
                "Игра: {streamGame}" + "{n}" +
                "Присоединяйся!" + "{n}" +
                "{links}";
    }
}
