package ru.sovaowltv.service.stream.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import ru.sovaowltv.service.stream.StreamModerationUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class ClearUtil {
    private final UsersTwitchRepository usersTwitchRepository;
    private final UsersGGRepository usersGGRepository;
    private final UsersGoogleRepository usersGoogleRepository;
    private final MessageRepository messageRepository;
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final TwitchChatUtil twitchChatUtil;
    private final GGChatUtil ggChatUtil;
    private final YTChatUtil ytChatUtil;
    private final MessagesUtil messagesUtil;
    private final PurgeUtil purgeUtil;
    private final StreamModerationUtil streamModerationUtil;

    private final ApiWebsiteChats apiWebsiteChats;
    private final MessageSource messageSource;

    @Value("${website}")
    private String website;

    @Value("${twitch}")
    private String twitch;

    @Value("${gg}")
    private String gg;

    @Value("${yt}")
    private String yt;

    public MessageStatus clearUserByNickName(User moderator, String text, String channel) {
        String[] split = text.trim().split(" ", 3);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.wrongFormatClearByNick", null, LocaleContextHolder.getLocale()));
        }
        Message message;
        try {
            message = messageRepository.findTop1ByNickEqualsOrderByIdDesc(split[1])
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "MESSAGE NOT FOUND"));
        } catch (ResponseStatusException e) {
            return messagesUtil.getUserNeverTypedOrNotExist(split[1]);
        }
        return clearUserByMessageId(moderator, split[0] + " " + message.getId() + " " + (split.length > 2 ? split[2] : ""), channel);
    }

    public MessageStatus clearUserByMessageId(User moderator, String text, String channel) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.saveAndFree(channelOwner);
        String[] split = text.trim().split(" ", 3);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.wrongFormatClearById", null, LocaleContextHolder.getLocale()));
        }

        String targetId = split[1];
        Message message = messageRepository.findById(Long.parseLong(targetId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CAN'T FIND MESSAGE"));

        String issuerId = message.getIssuerId();
        if (message.getSource().equalsIgnoreCase(website)) {
            User userForClear = usersRepositoryHandler.getUserById(issuerId);
            MessageStatus messageStatus = clearUserOnChannel(moderator, userForClear, stream, message, (split.length > 2 ? split[2] : ""));
            usersRepositoryHandler.free(userForClear);
            return messageStatus;
        } else if (message.getSource().equalsIgnoreCase(twitch)) {
            Optional<UserTwitch> userTwitchBySub = usersTwitchRepository.findBySub(issuerId);
            if (userTwitchBySub.isPresent()) {
                return clearUserOnChannel(moderator, userTwitchBySub.get().getUser(), stream, message, (split.length > 2 ? split[2] : ""));
            } else {
                Optional<ApiForChat> twitchChatOwner = twitchChatUtil.getTwitchChatOwner(channel);
                twitchChatOwner.ifPresent(twitchChat -> twitchChat.purgeUser(message.getNick(), message));
                return messagesUtil.getOkMessageStatus("modAction",
                        "clearUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + (split.length > 2 ? split[2] : ""));
            }
        } else if (message.getSource().equalsIgnoreCase(gg)) {
            Optional<UserGG> userGGBySub = usersGGRepository.findBySub(issuerId);
            if (userGGBySub.isPresent()) {
                return clearUserOnChannel(moderator, userGGBySub.get().getUser(), stream, message, (split.length > 2 ? split[2] : ""));
            } else {
                Optional<ApiForChat> ggChatOwner = ggChatUtil.getGGChatOwner(channel);
                ggChatOwner.ifPresent(ggChat -> ggChat.purgeUser(message.getNick(), message));
                return messagesUtil.getOkMessageStatus("modAction",
                        "clearUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + (split.length > 2 ? split[2] : ""));
            }
        } else if (message.getSource().equalsIgnoreCase(yt)) {
            Optional<UserGoogle> userGoogleBySub = usersGoogleRepository.findBySub(issuerId);
            if (userGoogleBySub.isPresent()) {
                return clearUserOnChannel(moderator, userGoogleBySub.get().getUser(), stream, message, (split.length > 2 ? split[2] : ""));
            } else {
                Optional<ApiForChat> ytChatOwner = ytChatUtil.getYTChatOwner(channel);
                ytChatOwner.ifPresent(ytChat -> ytChat.purgeUser(message.getNick(), message));
                return messagesUtil.getOkMessageStatus("modAction",
                        "clearUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + (split.length > 2 ? split[2] : ""));
            }
        } else {
            return messagesUtil.getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.someErrorContactAdmin", null, LocaleContextHolder.getLocale()) + " clearUserByMessageId");
        }
    }

    private MessageStatus clearUserOnChannel(User moderator, User userForClear, Stream stream, Message message, String reason) {
        if (message.getStreamId() != stream.getId()) {
            return messagesUtil.getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.messageNotFromThisStream", null, LocaleContextHolder.getLocale()));
        }

        if (stream.getUser().getId() == userForClear.getId() && !userUtil.isAdminOrModerator(moderator)) {
            return messagesUtil.getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.ownerClear", null, LocaleContextHolder.getLocale()));
        }

        if (userUtil.isAdminOrModerator(userForClear) && !userUtil.isAdminOrModerator(moderator)) {
            return messagesUtil.getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.adminOrModerator", null, LocaleContextHolder.getLocale()));
        }

        if (stream.getModeratorsList().contains(userForClear)) {
            if (userUtil.isAdminOrModerator(moderator) || stream.getUser().getId() == moderator.getId() || moderator.getId() == userForClear.getId()) {
                if (!clearUserFromStream(userForClear, stream.getUser().getNickname())) {
                    return messagesUtil.getErrorMessageStatus("modAction",
                            messageSource.getMessage("pages.chat.message.moderator.ClearError", null, LocaleContextHolder.getLocale()));
                }
                sendClearMessageToAllChats(message, stream.getUser().getNickname());
                clearMessage(message);
                return messagesUtil.getOkMessageStatus("modAction", "clearUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + reason);
            } else {
                return messagesUtil.getErrorMessageStatus("modAction",
                        messageSource.getMessage("pages.chat.message.moderator.channelModerator", null, LocaleContextHolder.getLocale()));
            }
        }

        if (!stream.getModeratorsList().contains(userForClear)) {
            if (!clearUserFromStream(userForClear, stream.getUser().getNickname())) {
                return messagesUtil.getErrorMessageStatus("modAction",
                        messageSource.getMessage("pages.chat.message.moderator.clearError", null, LocaleContextHolder.getLocale()));
            }
            sendClearMessageToAllChats(message, stream.getUser().getNickname());
            clearMessage(message);
            return messagesUtil.getOkMessageStatus("modAction", "clearUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + reason);
        }

        return messagesUtil.getErrorMessageStatus("modAction",
                messageSource.getMessage("pages.chat.message.moderator.someErrorContactAdmin", null, LocaleContextHolder.getLocale()));
    }

    public MessageStatus clearAll(User moderator, String text, Stream stream, String channel) {
        if (streamModerationUtil.canModerateStream(moderator, stream)) {
            clearAllMessagesFromScreenLastHalfOur(stream);
            return messagesUtil.getOkMessageStatus("clearAll", moderator.getNickname());
        }
        return messagesUtil.getErrorMessageStatus("modAction",
                messageSource.getMessage("pages.chat.message.moderator.insufficientPermission", null, LocaleContextHolder.getLocale()));
    }


    private boolean clearUserFromStream(User user, String channel) {
        //todo: ?
        return true;
    }

    private void clearMessage(Message message) {
        message.setBanned(true);
        messageRepository.save(message);
    }

    private void clearAllMessagesFromScreenLastHalfOur(Stream stream) {
        List<Message> messageList = messageRepository.findByStreamIdAndTimeAfter(stream.getId(), LocalDateTime.now().minusMinutes(30));
        messageList.forEach(purgeUtil::clearMessage);
    }

    private void sendClearMessageToAllChats(Message message, String webSiteChannel) {
        List<ApiForChat> chatByChannel = apiWebsiteChats.getChatByChannel(webSiteChannel);
        if (chatByChannel != null) {
            if (message.getSource().equalsIgnoreCase(twitch)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof TwitchChat) {
                        TwitchChat chat = ((TwitchChat) apiForChat);
                        chat.deleteMessage(message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(gg)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof GGChat) {
                        GGChat chat = ((GGChat) apiForChat);
                        chat.deleteMessage(message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(yt)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof YTChat) {
                        YTChat chat = ((YTChat) apiForChat);
                        chat.deleteMessage(message);
                        break;
                    }
                }
            }
        }
    }
}
