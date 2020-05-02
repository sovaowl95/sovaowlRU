package ru.sovaowltv.service.factorys.gg;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.user.UserApiCorruptedException;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.realization.GGChat;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:api/gg.yml")
public class GGChatFactory {
    private final AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Value("${gg_wssip}")
    private String ggIp;

    @Value("${gg_wssport}")
    private String ggPort;

    @Value("${gg_clientId}")
    private String ggClientId;

    @Value("${gg_clientSecret}")
    private String ggClientSecret;

    public GGChat factoryGG(User userFromWebsite, UserGG userGG, String channel) {
        if (userGG.isCorrupted())
            throw new UserApiCorruptedException(userGG);

        boolean canRead = userGG.getNick().equalsIgnoreCase(channel);
//        if (canReadB != null) canRead = canReadB;

        GGChat ggChat = new GGChat(
                userFromWebsite.getNickname(),
                ggIp, ggPort, ggClientId, ggClientSecret,
                userGG.getChatToken(),
                channel, userGG,
                canRead, true);
        autowireCapableBeanFactory.autowireBean(ggChat);
        return ggChat;
    }
}
