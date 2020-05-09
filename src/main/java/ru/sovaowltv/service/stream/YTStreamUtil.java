package ru.sovaowltv.service.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.user.UserApiCorruptedException;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.realization.ApiChats;
import ru.sovaowltv.service.chat.realization.YTChat;
import ru.sovaowltv.service.chat.util.YTChatUtil;
import ru.sovaowltv.service.factorys.google.YTChatFactory;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class YTStreamUtil {
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final YTChatUtil ytChatUtil;

    private final YTChatFactory ytChatFactory;

    private final ApiChats apiChats;

    public void solveNewYTUserConnection(User user) {
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUser(user);
        streamOptional.ifPresent(stream -> createYTChatReader(user, user.getUserGoogle(), stream.getUser().getNickname(), true));
    }

    public void solveGoogleUserDisconnected(User user) {
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUser(user);
        streamOptional.ifPresent(ytChatUtil::deleteStreamChatYOUTUBEConnection);
    }


    void launchYTChat(User user) {
        try {
            UserGoogle userGoogle = user.getUserGoogle();
            if (userGoogle != null) createYTChatReader(user, userGoogle, userGoogle.getNick(), true);
        } catch (UserApiCorruptedException e) {
            log.error("error launch yt chat for user {} corrupted.", user.getNickname());
        } catch (Exception e) {
            log.error("error launch yt chat for user {} {}", user.getNickname(), e);
        }
    }

    public void createYTChatReader(User userFromWebsite, UserGoogle userGoogle, String channel, boolean isStreamer) {
        YTChat ytChat = ytChatFactory.factoryYT(userFromWebsite, userGoogle);
        if (isStreamer) {
            ytChat.setCanRead(true);
            ytChat.setCanWrite(true);
        }
        apiChats.addStreamChatConnection(userFromWebsite.getNickname(), ytChat);
    }

    public void addAnotherYTToChat(User userFromWebsite, String anotherChannel) {
        YTChat ytChat = ytChatFactory.factoryYT(userFromWebsite, userFromWebsite.getUserGoogle());
        ytChat.setCanRead(true);
        apiChats.addStreamChatConnection(userFromWebsite.getNickname(), ytChat);
    }

    private void reloadYTStreamChat(User userFromWebsite) {
        launchYTChat(userFromWebsite);
    }

}
