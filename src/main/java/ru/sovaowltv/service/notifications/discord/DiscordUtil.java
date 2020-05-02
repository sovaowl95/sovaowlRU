package ru.sovaowltv.service.notifications.discord;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apinotification.DiscordNotification;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.DiscordNotificationRepository;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserUtil;

@Service
@RequiredArgsConstructor
public class DiscordUtil {
    private final DiscordNotificationRepository discordNotificationRepository;

    private final UserUtil userUtil;
    private final StreamUtil streamUtil;

    private final DiscordNotificationFactory discordNotificationFactory;

    public String addDiscordNotification(String guildId) {
        User user = userUtil.getUserREADONLY();
        Stream stream = streamUtil.getStreamByUserNickname(user.getNickname());
        DiscordNotification discordNotification = discordNotificationFactory.createDiscordNotification(guildId, stream);
        stream.setDiscordNotification(discordNotification);
        streamUtil.save(stream);
        return "redirect:/profile/settings";
    }

    public String removeDiscordNotification() {
        User user = userUtil.getUserREADONLY();
        Stream stream = streamUtil.getStreamByUserNickname(user.getNickname());
        DiscordNotification discordNotification = stream.getDiscordNotification();
        stream.setDiscordNotification(null);
        discordNotificationRepository.delete(discordNotification);
        streamUtil.save(stream);
        return "redirect:/profile/settings";
    }
}
