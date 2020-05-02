package ru.sovaowltv.service.factorys.twitch;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.user.UserApiCorruptedException;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.realization.TwitchChat;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:api/twitch.yml")

public class TwitchChatFactory {
    private final AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Value("${twitch_wssip}")
    private String twitchIp;

    @Value("${twitch_wssport}")
    private String twitchPort;

    @Value("${twitch_clientId}")
    private String twitchClientId;

    @Value("${twitch_clientSecret}")
    private String twitchClientSecret;

    public TwitchChat factoryTwitch(User userFromWebsite, UserTwitch userTwitch, String channel) {
        if (userTwitch.isCorrupted())
            throw new UserApiCorruptedException(userTwitch);

        boolean canRead = userTwitch.getNick().equalsIgnoreCase(channel);
//        if (canReadB != null) canRead = canReadB;

        TwitchChat twitchChat = new TwitchChat(
                userFromWebsite.getNickname(),
                twitchIp, twitchPort, twitchClientId, twitchClientSecret,
                userTwitch.getAccessToken(),
                channel, userTwitch,
                canRead, true);
        autowireCapableBeanFactory.autowireBean(twitchChat);
        return twitchChat;
    }
}
