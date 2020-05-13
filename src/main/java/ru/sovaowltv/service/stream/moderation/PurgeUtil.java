package ru.sovaowltv.service.stream.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.chat.ChatMessage;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageRepository;
import ru.sovaowltv.service.chat.realization.*;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.unclassified.LanguageUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.List;

import static ru.sovaowltv.service.unclassified.Constants.MOD_ACTION;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class PurgeUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final MessageRepository messageRepository;

    private final MessagesUtil messagesUtil;
    private final LanguageUtil languageUtil;

    private final ApiWebsiteChats apiWebsiteChats;


    @Value("${website}")
    private String website;

    @Value("${twitch}")
    private String twitch;

    @Value("${gg}")
    private String gg;

    @Value("${yt}")
    private String yt;

    public MessageStatus purgeUserByNickName(User moderator, String text, String channel) {
        String[] split = text.trim().split(" ", 3);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatPurgeByNick"));
        }
        Message message;
        try {
            message = messageRepository.findTop1ByNickEqualsOrderByIdDesc(split[1])
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "MESSAGE NOT FOUND"));
        } catch (ResponseStatusException e) {
            return messagesUtil.getUserNeverTypedOrNotExist(split[1]);
        }
        return purgeUserByMessageId(moderator, split[0] + " " + message.getId() + " " + (split.length > 2 ? split[2] : ""), channel);
    }

    public MessageStatus purgeUserByMessageId(User moderator, String text, String channel) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.free(channelOwner);
        String[] split = text.trim().split(" ", 3);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatUnmodById"));
        }

        String targetId = split[1];
        Message message = messageRepository.findById(Long.parseLong(targetId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CAN'T FIND MESSAGE"));

        String issuerId = message.getIssuerId();
        if (message.getSource().equalsIgnoreCase(website)) {
            User userForMod = usersRepositoryHandler.getUserById(issuerId);
            MessageStatus messageStatus = purgeUserOnChannel(moderator, userForMod, stream, message);
            usersRepositoryHandler.free(userForMod);
            return messageStatus;
        } else if (message.getSource().equalsIgnoreCase(twitch)) {
            return purgeUserOnChannel(moderator, null, stream, message);
        } else if (message.getSource().equalsIgnoreCase(gg)) {
            return purgeUserOnChannel(moderator, null, stream, message);
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.notFromWebsite"));
        }
    }

    private MessageStatus purgeUserOnChannel(User moderator, User userForPurge, Stream stream, Message message) {
        List<ChatMessage> list;
        if (userForPurge != null)
            list = messageRepository.findByStreamIdAndTimeAfterAndBannedIsFalseAndIssuerIdOrderByIdDesc(
                    stream.getId(), LocalDateTime.now().minusMinutes(30), String.valueOf(userForPurge.getId()));
        else
            list = messageRepository.findByStreamIdAndTimeAfterAndBannedIsFalseAndNickOrderByIdDesc(
                    stream.getId(), LocalDateTime.now().minusMinutes(30), message.getNick());
        StringBuilder stringBuilder = new StringBuilder();
        list.forEach(m -> {
            if (m instanceof Message) {
                Message msg = (Message) m;
                stringBuilder.append(msg.getId()).append(" ");
                clearMessage(msg);
            }
        });

        if (!list.isEmpty()) {
            ChatMessage chatMessage = list.get(0);
            if (chatMessage instanceof Message)
                sendPurgeMessageToAllChats(((Message) chatMessage), stream.getUser().getNickname());
        }

        String nickname = userForPurge != null ? userForPurge.getNickname() : message.getNick();
        return messagesUtil.getOkMessageStatus(MOD_ACTION,
                "purgeUserByMessageId " + moderator.getNickname() + " " + nickname + " " + stringBuilder.toString());
    }

    void clearMessage(Message message) {
        message.setBanned(true);
        messageRepository.save(message);
    }

    private void sendPurgeMessageToAllChats(Message message, String webSiteChannel) {
        List<ApiForChat> chatByChannel = apiWebsiteChats.getChatByChannel(webSiteChannel);
        if (chatByChannel != null) {
            if (message.getSource().equalsIgnoreCase(twitch)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof TwitchChat) {
                        TwitchChat chat = ((TwitchChat) apiForChat);
                        chat.purgeUser(message.getNick(), message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(gg)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof GGChat) {
                        GGChat chat = ((GGChat) apiForChat);
                        chat.purgeUser(message.getNick(), message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(yt)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof YTChat) {
                        YTChat chat = ((YTChat) apiForChat);
                        chat.purgeUser(message.getNick(), message);
                        break;
                    }
                }
            }
        }
    }
}
