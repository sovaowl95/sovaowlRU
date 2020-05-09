package ru.sovaowltv.service.messages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.util.GGChatUtil;
import ru.sovaowltv.service.chat.util.TwitchChatUtil;
import ru.sovaowltv.service.chat.util.YTChatUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageRunnable {
    private final TwitchChatUtil twitchChatUtil;
    private final GGChatUtil ggChatUtil;
    private final YTChatUtil ytChatUtil;

    Runnable getRunnableSendToYT(Message message, String webSiteChannel, Object apiChatObject, User user) {
        return () -> {
            try {
                ytChatUtil.sendMessageToYT(message, webSiteChannel, apiChatObject, user);
            } catch (Exception e) {
                log.error("cant send message to yt {} {}", message.getText(), e);
            }
        };
    }

    Runnable getRunnableSendToGG(Message message, String webSiteChannel, Object apiChatObject, User user) {
        return () -> {
            try {
                ggChatUtil.sendMessageToGG(message, webSiteChannel, apiChatObject, user);
            } catch (Exception e) {
                log.error("cant send message to gg {} {}", message.getText(), e);
            }
        };
    }

    Runnable getRunnableSendToTwitch(Message message, String webSiteChannel, Object apiChatObject, User user) {
        return () -> {
            try {
                twitchChatUtil.sendMessageToTwitch(message, webSiteChannel, apiChatObject, user);
            } catch (Exception e) {
                log.error("cant send message to twitch {} {}", message.getText(), e);
            }
        };
    }
}
