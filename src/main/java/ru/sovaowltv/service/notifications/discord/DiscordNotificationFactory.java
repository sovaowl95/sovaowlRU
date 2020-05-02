package ru.sovaowltv.service.notifications.discord;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apinotification.DiscordNotification;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.website.DiscordNotificationRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscordNotificationFactory {
    private final DiscordNotificationRepository discordNotificationRepository;

    public DiscordNotification createDiscordNotification(String guildId, Stream stream) {
        Optional<DiscordNotification> notificationOptional = discordNotificationRepository.findByServer(guildId);
        return notificationOptional.orElseGet(() -> buildDiscordNotification(guildId, stream));
    }

    private DiscordNotification buildDiscordNotification(String guildId, Stream stream) {
        DiscordNotification discordNotification = new DiscordNotification();
        discordNotification.setServer(guildId);
        discordNotification.setChannel("");
        discordNotification.setText(getDefaultText(stream.getUser().isMale()));
        discordNotificationRepository.save(discordNotification);
        return discordNotification;
    }

    private String getDefaultText(boolean male) {
        String begin = male ? "начал" : "начала";
        return "@everyone" + "{n}" +
                "{user} " + begin + " трансляцию" + "{n}" +
                "Название: {streamName}" + "{n}" +
                "Игра: {streamGame}" + "{n}" +
                "Присоединяйся!" + "{n}" +
                "{links}";
    }
}
