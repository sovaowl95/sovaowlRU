package ru.sovaowltv.service.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.user.UserApiCorruptedException;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.realization.ApiChats;
import ru.sovaowltv.service.chat.realization.GGChat;
import ru.sovaowltv.service.chat.util.GGChatUtil;
import ru.sovaowltv.service.factorys.gg.GGChatFactory;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GGStreamUtil {
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final GGChatUtil ggChatUtil;

    private final GGChatFactory ggChatFactory;
    private final ApiChats apiChats;

    public void solveNewGGUserConnection(User user) {
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUser(user);
        streamOptional.ifPresent(stream -> createGGChatReader(user, user.getUserGG(), user.getUserGG().getNick(), true));
    }


    public void solveGGUserDisconnected(User user) {
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUser(user);
        streamOptional.ifPresent(ggChatUtil::deleteStreamChatGOODGAMEConnection);
    }


    void launchGGChat(User user) {
        try {
            UserGG userGG = user.getUserGG();
            if (userGG != null) createGGChatReader(user, userGG, userGG.getNick(), true);
        } catch (UserApiCorruptedException e) {
            log.error("error launch gg chat for user " + user.getNickname() + " corrupted.");
        } catch (Exception e) {
            log.error("error launch gg chat for user " + user.getNickname(), e);
        }
    }

    public void createGGChatReader(User userFromWebsite, UserGG userGG, String channel, boolean isStreamer) {
        GGChat ggChat = ggChatFactory.factoryGG(userFromWebsite, userGG, channel);
        if (isStreamer) {
            ggChat.setCanRead(true);
            ggChat.setCanWrite(true);
        }
        apiChats.addStreamChatConnection(userFromWebsite.getNickname(), ggChat);
    }

    public void addAnotherGGToChat(User userFromWebsite, String anotherChannel) {
        GGChat ggChat = ggChatFactory.factoryGG(userFromWebsite, userFromWebsite.getUserGG(), anotherChannel);
        ggChat.setCanRead(true);
        apiChats.addStreamChatConnection(userFromWebsite.getNickname(), ggChat);
    }

    private void reloadGGStreamChat(User userFromWebsite) {
        launchGGChat(userFromWebsite);
    }
}
