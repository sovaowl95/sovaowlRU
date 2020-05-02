package ru.sovaowltv.service.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.notifications.discord.DiscordNotificationUtil;
import ru.sovaowltv.service.notifications.vk.VKNotificationUtil;

@Component
@RequiredArgsConstructor
public class NotificationUtil {
    private final DiscordNotificationUtil discordNotificationUtil;
    private final VKNotificationUtil vkNotificationUtil;

    //todo: 4.09.2019 more notifications!
    //todo: может быть стоит добавить отдельные уведомления на смену игры\\названия
    //todo: запоминать время старта, дабы избежать повторных уведомлений
    public void notifyAll(Stream stream) {
        discordNotificationUtil.sendNotification(stream);
        vkNotificationUtil.sendNotification(stream);
    }
}
