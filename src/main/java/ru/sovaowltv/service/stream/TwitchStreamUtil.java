package ru.sovaowltv.service.stream;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.user.UserApiCorruptedException;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.webhooks.TwitchStreamLiveSub;
import ru.sovaowltv.service.chat.realization.ApiChats;
import ru.sovaowltv.service.chat.realization.TwitchChat;
import ru.sovaowltv.service.chat.util.TwitchChatUtil;
import ru.sovaowltv.service.factorys.twitch.TwitchChatFactory;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwitchStreamUtil {
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final TwitchChatUtil twitchChatUtil;

    private final TwitchChatFactory twitchChatFactory;

    private final TwitchStreamLiveSub twitchStreamLiveSub;
    private final ApiChats apiChats;

    public void solveNewTwitchUserConnection(User user) {
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUser(user);
        streamOptional.ifPresent(stream -> {
            createTwitchChatReader(user, user.getUserTwitch(), user.getUserTwitch().getNick(), true);
            createTwitchOnlineOfflineHook(user, user.getUserTwitch(), stream);
        });
    }

    public void solveTwitchUserDisconnected(User user) {
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUser(user);
        streamOptional.ifPresent(twitchChatUtil::deleteStreamChatTWITCHConnection);
    }

    void launchTwitchChat(User user, Stream stream) {
        try {
            UserTwitch userTwitch = user.getUserTwitch();
            if (userTwitch != null) createTwitchChatReader(user, userTwitch, userTwitch.getNick(), true);
            if (userTwitch != null) createTwitchOnlineOfflineHook(user, userTwitch, stream);
        } catch (UserApiCorruptedException e) {
            log.error("error launch twitch chat for user {} corrupted.", user.getNickname());
        } catch (Exception e) {
            log.error("error launch twitch chat for user {} {}", user.getNickname(), e);
        }
    }

    private void createTwitchOnlineOfflineHook(User userFromWebsite, UserTwitch userTwitch, Stream stream) {
        boolean res = twitchStreamLiveSub.subForStream(userFromWebsite, userTwitch, stream);
        if (!res) {
            log.error("couldn't create webHook for {}", userFromWebsite.getNickname());
        }
    }

    public void createTwitchChatReader(User userFromWebsite, UserTwitch userTwitch, String channel, boolean isStreamer) {
        TwitchChat twitchChat = twitchChatFactory.factoryTwitch(userFromWebsite, userTwitch, channel);
        if (isStreamer) {
            twitchChat.setCanRead(true);
            twitchChat.setCanWrite(true);
        }
        apiChats.addStreamChatConnection(userFromWebsite.getNickname(), twitchChat);
    }

    public void addAnotherTwitchToChat(User userFromWebsite, String anotherChannel) {
        TwitchChat twitchChat = twitchChatFactory.factoryTwitch(userFromWebsite, userFromWebsite.getUserTwitch(), anotherChannel);
        twitchChat.setCanRead(true);
        apiChats.addStreamChatConnection(userFromWebsite.getNickname(), twitchChat);
    }

    public void reloadTwitchStreamChat(User userFromWebsite) {
        log.info("reloadTwitchStreamChat {}", userFromWebsite.getLogin());
        UserTwitch userTwitch = userFromWebsite.getUserTwitch();
        if (userTwitch != null) {
            createTwitchChatReader(userFromWebsite, userTwitch, userTwitch.getNick(), true);
        }
    }
}
