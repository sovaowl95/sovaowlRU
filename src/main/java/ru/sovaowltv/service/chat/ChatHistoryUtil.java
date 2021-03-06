package ru.sovaowltv.service.chat;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.chat.ChatMessage;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageRepository;
import ru.sovaowltv.repositories.messages.MessageStatusRepository;
import ru.sovaowltv.service.api.token.GGTokenHandler;
import ru.sovaowltv.service.api.token.TwitchTokenHandler;
import ru.sovaowltv.service.api.token.YTTokenHandler;
import ru.sovaowltv.service.caravan.CaravanStatus;
import ru.sovaowltv.service.caravan.CaravanUtil;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamModerationUtil;
import ru.sovaowltv.service.user.params.UserPremiumUtil;

import java.time.LocalDateTime;
import java.util.List;

import static ru.sovaowltv.service.unclassified.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryUtil {
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;

    private final CaravanUtil caravanUtil;
    private final StreamModerationUtil streamModerationUtil;
    private final MessagesUtil messagesUtil;
    private final UserPremiumUtil userPremiumUtil;

    private final TwitchTokenHandler twitchTokenHandler;
    private final GGTokenHandler ggTokenHandler;
    private final YTTokenHandler ytTokenHandler;

    private static final String HISTORY = "history";
    private static final String TOPIC = "/topic/";

    public ChatMessage solveHistoryMessage(String channel, User user, Stream stream) {
        solveHistory(channel, user, stream);
        return null;
    }

    public void solveHistoryMessage(Stream stream, String channel, String message, SimpMessageHeaderAccessor ha) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        String sessionId = ha.getSessionId();
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        if (isHistoryMessage(message)) {
            List<ChatMessage> messages = messageRepository.findTop20ByStreamIdAndTimeAfterAndBannedIsFalseOrderByIdDesc(
                    stream.getId(), LocalDateTime.now().minusMinutes(30));
            List<MessageStatus> statusMessages = messageStatusRepository.findByStreamIdAndTimeAfterOrderByIdDesc(
                    stream.getId(), LocalDateTime.now().minusMinutes(30));

            messages.addAll(statusMessages);
            messagesUtil.createAndSendMessageStatus(
                    HISTORY,
                    new Gson().toJson(messages),
                    sessionId,
                    TOPIC + channel,
                    createHeaders(sessionId)
            );

            if (caravanUtil.getCaravanStatus() == CaravanStatus.GROUP_UP) {
                messagesUtil.sendMessageWithHeaders(
                        sessionId,
                        TOPIC + channel,
                        caravanUtil.prepareCaravanStartMessage(),
                        createHeaders(sessionId)
                );
            }
        } else {
            messagesUtil.createAndSendMessageStatus(CARAVAN_JOIN_ERR_STATUS_JOIN_ANON,
                    "",
                    sessionId,
                    TOPIC + channel,
                    createHeaders(sessionId)
            );
        }
    }

    private boolean isHistoryMessage(String message) {
        return message.equals("{\"type\":\"history\",\"text\":\"\"}");
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    private void solveHistory(String channel, User user, Stream stream) {
        List<ChatMessage> messages = messageRepository.findTop20ByStreamIdAndTimeAfterAndBannedIsFalseOrderByIdDesc(
                stream.getId(), LocalDateTime.now().minusMinutes(30));
        List<MessageStatus> messages1 = messageStatusRepository.findByStreamIdAndTimeAfterOrderByIdDesc(
                stream.getId(), LocalDateTime.now().minusMinutes(30));

        MessageStatus ms = new MessageStatus();
        ms.setType(HISTORY);
        ms.setInfo(String.valueOf(streamModerationUtil.canModerateStream(user, stream)));

        messages.add(0, ms);
        messages.addAll(messages1);

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(HISTORY);
        messageStatus.setInfo(new Gson().toJson(messages));
        messagesUtil.convertAndSendToUser(user.getLogin(), channel, messageStatus);

        if (caravanUtil.getCaravanStatus() == CaravanStatus.GROUP_UP) {
            MessageStatus messageStatus2 = caravanUtil.prepareCaravanStartMessage();
            messagesUtil.convertAndSendToUser(user.getLogin(), channel, messageStatus2);
        }

        userPremiumUtil.solvePremiumExpiredSoon(user.getLogin(), channel, user);

        checkAllApiAuthCorrect(user, channel, stream.getUser());
    }


    private void checkAllApiAuthCorrect(User authorUser, String channel, User channelOwnerUser) {
        //streamer request
        if (authorUser.getId() == channelOwnerUser.getId()) {
            checkTwitch(channelOwnerUser, channel);
            checkGG(channelOwnerUser, channel);
            checkYT(channelOwnerUser, channel);
        } else {
            String firstPart = "You can join";
            String lastPart = "accounts in your profile!";
            String platforms = "";
            boolean found = false;
            if (channelOwnerUser.getUserTwitch() != null && authorUser.getUserTwitch() == null) {
                found = true;
                platforms = "Twitch ";
            }
            if (channelOwnerUser.getUserGG() != null && authorUser.getUserGG() == null) {
                found = true;
                platforms = platforms.trim() + " Goodgame ";
            }
            if (channelOwnerUser.getUserGoogle() != null && authorUser.getUserGoogle() == null) {
                found = true;
                platforms = platforms.trim() + " Youtube ";
            }

            if (!found) return;
            String info = firstPart + " " + platforms + " " + lastPart;
            messagesUtil.sendMessageToLogin(channel, API_MOTIVATION, info, authorUser.getLogin());
        }
    }

    private void checkTwitch(User authorUser, String channel) {
        UserTwitch userTwitch = authorUser.getUserTwitch();
        if (userTwitch != null) {
            if (!twitchTokenHandler.refresh(userTwitch)) {
                messagesUtil.sendErrorMessageToLogin(channel, ACC_REJOIN, "You must rejoin your Twitch account", authorUser.getLogin());
            } else {
                MessageStatus st = messagesUtil.getOkMessageStatus(ACC_REJOIN_OK, "Your Twitch account link is ok");
                messagesUtil.convertAndSendToUser(authorUser.getLogin(), channel, st);
            }
        }
    }


    private void checkGG(User authorUser, String channel) {
        UserGG userGG = authorUser.getUserGG();
        if (userGG != null) {
            if (!ggTokenHandler.refresh(userGG)) {
                messagesUtil.sendErrorMessageToLogin(channel, ACC_REJOIN, "You must rejoin your GoodGame account", authorUser.getLogin());
            } else {
                MessageStatus st = messagesUtil.getOkMessageStatus(ACC_REJOIN_OK, "Your GoodGame account link is ok");
                messagesUtil.convertAndSendToUser(authorUser.getLogin(), channel, st);
            }
        }
    }

    private void checkYT(User authorUser, String channel) {
        UserGoogle userGoogle = authorUser.getUserGoogle();
        if (userGoogle != null) {
            if (!ytTokenHandler.refresh(userGoogle)) {
                messagesUtil.sendErrorMessageToLogin(channel, ACC_REJOIN, "You must rejoin your YouTube account", authorUser.getLogin());
            } else {
                MessageStatus st = messagesUtil.getOkMessageStatus(ACC_REJOIN_OK, "Your YouTube account link is ok");
                messagesUtil.convertAndSendToUser(authorUser.getLogin(), channel, st);
            }
        }
    }
}
