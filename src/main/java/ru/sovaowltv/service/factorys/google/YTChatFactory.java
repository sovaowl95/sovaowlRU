package ru.sovaowltv.service.factorys.google;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.user.UserApiCorruptedException;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.realization.YTChat;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:api/google.yml")
public class YTChatFactory {
    private final AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Value("${google_clientId}")
    private String googleClientId;

    @Value("${google_clientSecret}")
    private String googleclientSecret;

    public YTChat factoryYT(User userFromWebsite, UserGoogle userGoogle) {
        //todo: WARNING!
        if (userGoogle.isCorrupted())
            throw new UserApiCorruptedException(userGoogle);
        String channel = userFromWebsite.getNickname();

        boolean canRead = userFromWebsite.getNickname().equalsIgnoreCase(channel);
//        if (canReadB != null) canRead = canReadB;

        YTChat ytChat = new YTChat(
                userFromWebsite.getNickname(),
                "0", "0", googleClientId, googleclientSecret,
                userGoogle.getAccessToken(),
                channel, userGoogle,
                canRead, true);
        autowireCapableBeanFactory.autowireBean(ytChat);
        return ytChat;
    }
}
