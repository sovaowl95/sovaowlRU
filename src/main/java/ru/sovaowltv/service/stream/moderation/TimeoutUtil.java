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
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.sovaowltv.service.unclassified.Constants.MOD_ACTION;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class TimeoutUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final UsersTwitchRepository usersTwitchRepository;
    private final UsersGGRepository usersGGRepository;
    private final UsersGoogleRepository usersGoogleRepository;
    private final MessageRepository messageRepository;

    private final UserUtil userUtil;
    private final TwitchChatUtil twitchChatUtil;
    private final GGChatUtil ggChatUtil;
    private final YTChatUtil ytChatUtil;
    private final MessagesUtil messagesUtil;
    private final LanguageUtil languageUtil;

    private final ApiTimeouts apiTimeouts;
    private final ApiWebsiteChats apiWebsiteChats;

    @Value("${website}")
    private String website;

    @Value("${twitch}")
    private String twitch;

    @Value("${gg}")
    private String gg;

    @Value("${yt}")
    private String yt;

    private boolean timeoutUserFromStream(User user, String channel, String time) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.saveAndFree(channelOwner);
        Map<User, LocalDateTime> timeoutMap = apiTimeouts.getTimeoutsByStreamId(stream.getId());
        if (timeoutMap == null) {
            timeoutMap = apiTimeouts.setTimeoutsByStreamId(stream.getId());
        }
        double seconds = Double.parseDouble(time);
        timeoutMap.put(user, LocalDateTime.now().plusSeconds((long) seconds));
        streamRepositoryHandler.save(stream);
        return true;
    }

    private void timeoutMessage(Message message) {
        message.setBanned(true);
        messageRepository.save(message);
    }

    public MessageStatus timeoutUserByNickName(User moderator, String text, String channel) {
        String[] split = text.trim().split(" ", 4);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatTimeoutByNick"));
        }
        Message message;
        try {
            message = messageRepository.findTop1ByNickEqualsOrderByIdDesc(split[1])
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "MESSAGE NOT FOUND"));
        } catch (ResponseStatusException e) {
            return messagesUtil.getUserNeverTypedOrNotExist(split[1]);
        }
        String parsed = split[0] + " " + message.getId() + " " + (split.length > 2 ? split[2] : "") + " " + (split.length > 3 ? split[3] : "");
        return timeoutUserByMessageId(moderator, parsed, channel);
    }

    public MessageStatus timeoutUserByMessageId(User moderator, String text, String channel) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.saveAndFree(channelOwner);

        String[] split = text.trim().split(" ", 4);
        if (split.length < 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatTimeoutById"));
        }

        String targetId = split[1];
        Message message = messageRepository.findById(Long.parseLong(targetId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CAN'T FIND MESSAGE"));

        String issuerId = message.getIssuerId();
        if (message.getSource().equalsIgnoreCase(website)) {
            User userForTimeout = usersRepositoryHandler.getUserById(issuerId);
            String time = split.length > 2 ? split[2] : "600";
            String reason = split.length > 3 ? split[3] : "noReason";
            MessageStatus messageStatus = timeoutUserOnChannel(moderator, userForTimeout, stream, message, time, reason);
            usersRepositoryHandler.free(userForTimeout);
            return messageStatus;
        } else if (message.getSource().equalsIgnoreCase(twitch)) {
            Optional<UserTwitch> userTwitchBySub = usersTwitchRepository.findBySub(issuerId);
            if (userTwitchBySub.isPresent()) {
                String time = split.length > 2 ? split[2] : "600";
                String reason = split.length > 3 ? split[3] : "noReason";
                return timeoutUserOnChannel(moderator, userTwitchBySub.get().getUser(), stream, message, time, reason);
            } else {
                Optional<ApiForChat> twitchChatOwner = twitchChatUtil.getTwitchChatOwner(channel);
                String time = split.length > 2 ? split[2] : "600";
                String reason = split.length > 3 ? split[3] : "noReason";
                twitchChatOwner.ifPresent(twitchChat -> twitchChat.timeoutUser(message.getNick(), time, reason, message));
                return messagesUtil.getOkMessageStatus(MOD_ACTION,
                        "timeoutUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + time + " " + message.getNick() + " " + reason);
            }
        } else if (message.getSource().equalsIgnoreCase(gg)) {
            Optional<UserGG> userGGBySub = usersGGRepository.findBySub(issuerId);
            if (userGGBySub.isPresent()) {
                String time = split.length > 2 ? split[2] : "600";
                String reason = split.length > 3 ? split[3] : "noReason";
                return timeoutUserOnChannel(moderator, userGGBySub.get().getUser(), stream, message, time, reason);
            } else {
                Optional<ApiForChat> ggChatOwner = ggChatUtil.getGGChatOwner(channel);
                String time = split.length > 2 ? split[2] : "600";
                String reason = split.length > 3 ? split[3] : "noReason";
                ggChatOwner.ifPresent(ggChat -> ggChat.timeoutUser(message.getNick(), time, reason, message));
                return messagesUtil.getOkMessageStatus(MOD_ACTION,
                        "timeoutUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + time + " " + message.getNick() + " " + reason);
            }
        } else if (message.getSource().equalsIgnoreCase(yt)) {
            Optional<UserGoogle> userGoogleBySub = usersGoogleRepository.findBySub(issuerId);
            if (userGoogleBySub.isPresent()) {
                String time = split.length > 2 ? split[2] : "600";
                String reason = split.length > 3 ? split[3] : "noReason";
                return timeoutUserOnChannel(moderator, userGoogleBySub.get().getUser(), stream, message, time, reason);
            } else {
                Optional<ApiForChat> ytChatOwner = ytChatUtil.getYTChatOwner(channel);
                String time = split.length > 2 ? split[2] : "600";
                String reason = split.length > 3 ? split[3] : "noReason";
                ytChatOwner.ifPresent(ytChat -> ytChat.timeoutUser(message.getNick(), time, reason, message));
                return messagesUtil.getOkMessageStatus(MOD_ACTION,
                        "timeoutUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + time + " " + message.getNick() + " " + reason);
            }
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.someErrorContactAdmin") + " timeoutUserByMessageId");
        }
    }

    private MessageStatus timeoutUserOnChannel(User moderator, User userForTimeout, Stream stream, Message message, String time, String reason) {
        try {
            Long.parseLong(time);
        } catch (NumberFormatException e) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.messageTimeMustBeNumber"));
        }

        if (Long.parseLong(time) <= 0) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.messageTimeMustBePositive"));
        }

        if (message.getStreamId() != stream.getId()) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.messageNotFromThisStream"));
        }

        if (moderator.getId() == userForTimeout.getId()) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.selfTimeout"));
        }

        if (stream.getUser().getId() == userForTimeout.getId()) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.ownerTimeout"));
        }

        if (userUtil.isAdminOrModerator(userForTimeout)) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.adminOrModerator"));
        }

        if (stream.getModeratorsList().contains(userForTimeout)) {
            if (userUtil.isAdminOrModerator(moderator) || stream.getUser().getId() == moderator.getId()) {
                if (!timeoutUserFromStream(userForTimeout, stream.getUser().getNickname(), time)) {
                    return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                            languageUtil.getStringFor("pages.chat.message.moderator.timeoutError"));
                }
                sendTimeoutMessageToAllChats(message, stream.getUser().getNickname(), time, reason);
                timeoutMessage(message);
                return messagesUtil.getOkMessageStatus(MOD_ACTION,
                        "timeoutUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + time + " " + userForTimeout.getNickname() + " " + reason);
            } else {
                return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                        languageUtil.getStringFor("pages.chat.message.moderator.channelModerator"));
            }
        } else {
            if (!timeoutUserFromStream(userForTimeout, stream.getUser().getNickname(), time)) {
                return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                        languageUtil.getStringFor("pages.chat.message.moderator.timeoutError"));
            }
            sendTimeoutMessageToAllChats(message, stream.getUser().getNickname(), time, reason);
            timeoutMessage(message);
            return messagesUtil.getOkMessageStatus(MOD_ACTION,
                    "timeoutUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + time + " " + userForTimeout.getNickname() + " " + reason);
        }
    }

    private void sendTimeoutMessageToAllChats(Message message, String webSiteChannel, String time, String reason) {
        List<ApiForChat> chatByChannel = apiWebsiteChats.getChatByChannel(webSiteChannel);
        if (chatByChannel != null) {
            if (message.getSource().equalsIgnoreCase(twitch)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof TwitchChat) {
                        TwitchChat chat = ((TwitchChat) apiForChat);
                        chat.timeoutUser(message.getNick(), time, reason, message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(gg)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof GGChat) {
                        GGChat chat = ((GGChat) apiForChat);
                        chat.timeoutUser(message.getNick(), time, reason, message);
                        break;
                    }
                }
            } else if (message.getSource().equalsIgnoreCase(yt)) {
                for (ApiForChat apiForChat : chatByChannel) {
                    if (apiForChat instanceof YTChat) {
                        YTChat chat = ((YTChat) apiForChat);
                        chat.timeoutUser(message.getNick(), time, reason, message);
                        break;
                    }
                }
            }
        }
    }

}
