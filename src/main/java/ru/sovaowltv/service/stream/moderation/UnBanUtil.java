package ru.sovaowltv.service.stream.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageRepository;
import ru.sovaowltv.repositories.user.UsersGGRepository;
import ru.sovaowltv.repositories.user.UsersGoogleRepository;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;
import ru.sovaowltv.service.chat.realization.*;
import ru.sovaowltv.service.chat.util.GGChatUtil;
import ru.sovaowltv.service.chat.util.TwitchChatUtil;
import ru.sovaowltv.service.chat.util.YTChatUtil;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.unclassified.LanguageUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.List;
import java.util.Optional;

import static ru.sovaowltv.service.unclassified.Constants.MOD_ACTION;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class UnBanUtil {
    private final UsersTwitchRepository usersTwitchRepository;
    private final UsersGGRepository usersGGRepository;
    private final UsersGoogleRepository usersGoogleRepository;
    private final MessageRepository messageRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final TwitchChatUtil twitchChatUtil;
    private final GGChatUtil ggChatUtil;
    private final YTChatUtil ytChatUtil;
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

    private boolean unBanUserFromStream(User user, String channel) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.saveAndFree(user);
        stream.getBansList().remove(user);
        streamRepositoryHandler.save(stream);
        return true;
    }

    public MessageStatus unBanUserByNickName(User moderator, String text, String channel) {
        String[] split = text.trim().split(" ", 3);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatUnBanByNick"));
        }
        Message message;
        try {
            message = messageRepository.findTop1ByNickEqualsOrderByIdDesc(split[1]).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "MESSAGE NOT FOUND"));
        } catch (ResponseStatusException e) {
            return messagesUtil.getUserNeverTypedOrNotExist(split[1]);
        }
        return unBanUserByMessageId(moderator, split[0] + " " + message.getId() + " " + (split.length > 2 ? split[2] : ""), channel);
    }

    public MessageStatus unBanUserByMessageId(User moderator, String text, String channel) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.saveAndFree(channelOwner);

        String[] split = text.trim().split(" ", 3);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatUnBanById"));
        }

        String targetId = split[1];
        Message message = messageRepository.findById(Long.parseLong(targetId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CAN'T FIND MESSAGE"));

        String issuerId = message.getIssuerId();
        if (message.getSource().equalsIgnoreCase(website)) {
            User userForUnBan = usersRepositoryHandler.getUserById(issuerId);
            MessageStatus messageStatus = unBanUserOnChannel(moderator, userForUnBan, stream, message, (split.length > 2 ? split[2] : ""));
            usersRepositoryHandler.saveAndFree(userForUnBan);
            return messageStatus;
        } else if (message.getSource().equalsIgnoreCase(twitch)) {
            Optional<UserTwitch> userTwitchBySub = usersTwitchRepository.findBySub(issuerId);
            if (userTwitchBySub.isPresent()) {
                return unBanUserOnChannel(moderator, userTwitchBySub.get().getUser(), stream, message, (split.length > 2 ? split[2] : ""));
            } else {
                Optional<ApiForChat> twitchChatOwner = twitchChatUtil.getTwitchChatOwner(channel);
                twitchChatOwner.ifPresent(twitchChat -> twitchChat.unBanUser(message.getNick(), message));
                return messagesUtil.getOkMessageStatus(MOD_ACTION,
                        "unBanUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + (split.length > 2 ? split[2] : ""));
            }
        } else if (message.getSource().equalsIgnoreCase(gg)) {
            Optional<UserGG> userGGBySub = usersGGRepository.findBySub(issuerId);
            if (userGGBySub.isPresent()) {
                return unBanUserOnChannel(moderator, userGGBySub.get().getUser(), stream, message, (split.length > 2 ? split[2] : ""));
            } else {
                Optional<ApiForChat> ggChatOwner = ggChatUtil.getGGChatOwner(channel);
                ggChatOwner.ifPresent(ggChat -> ggChat.unBanUser(message.getNick(), message));
                return messagesUtil.getOkMessageStatus(MOD_ACTION,
                        "unBanUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + (split.length > 2 ? split[2] : ""));
            }
        } else if (message.getSource().equalsIgnoreCase(yt)) {
            Optional<UserGoogle> userGoogleBySub = usersGoogleRepository.findBySub(issuerId);
            if (userGoogleBySub.isPresent()) {
                return unBanUserOnChannel(moderator, userGoogleBySub.get().getUser(), stream, message, (split.length > 2 ? split[2] : ""));
            } else {
                Optional<ApiForChat> ytChatOwner = ytChatUtil.getYTChatOwner(channel);
                ytChatOwner.ifPresent(ytChat -> ytChat.unBanUser(message.getNick(), message));
                return messagesUtil.getOkMessageStatus(MOD_ACTION,
                        "unBanUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + (split.length > 2 ? split[2] : ""));
            }
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.someErrorContactAdmin") + " unBanUserByMessageId");
        }
    }

    private MessageStatus unBanUserOnChannel(User moderator, User userForUnBan, Stream stream, Message message, String reason) {
        if (message.getStreamId() != stream.getId()) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.messageNotFromThisStream"));
        }

        if (stream.getBansList().remove(userForUnBan)) {
            if (!unBanUserFromStream(userForUnBan, stream.getUser().getNickname())) {
                return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                        languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatUnBanById"));
            }
            sendUnBanMessageToAllChats(message, stream.getUser().getNickname());
            return messagesUtil.getOkMessageStatus(MOD_ACTION, "unBanUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + reason);
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.userNotItBanList"));
        }
    }

    private void sendUnBanMessageToAllChats(Message message, String webSiteChannel) {
        List<ApiForChat> chatByChannel = apiWebsiteChats.getChatByChannel(webSiteChannel);
        if (chatByChannel != null) {
            if (message.getSource().equalsIgnoreCase(twitch)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof TwitchChat) {
                        TwitchChat chat = ((TwitchChat) apiForChat);
                        chat.unBanUser(message.getNick(), message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(gg)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof GGChat) {
                        GGChat chat = ((GGChat) apiForChat);
                        chat.unBanUser(message.getMessageSubId(), message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(yt)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof YTChat) {
                        YTChat chat = ((YTChat) apiForChat);
                        chat.unBanUser(message.getMessageSubId(), message);
                        break;
                    }
                }
            }
        }
    }
}
