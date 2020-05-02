package ru.sovaowltv.service.chat;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserPremiumUtil;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryUtil {
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final StreamUtil streamUtil;
    private final CaravanUtil caravanUtil;
    private final StreamModerationUtil streamModerationUtil;
    private final MessagesUtil messagesUtil;
    private final UserPremiumUtil userPremiumUtil;

    private final TwitchTokenHandler twitchTokenHandler;
    private final GGTokenHandler ggTokenHandler;
    private final YTTokenHandler ytTokenHandler;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatMessage solveHistoryMessage(String channel, Principal principal) {
        solveHistory(principal.getName(), channel);
        return null;
    }

    public void solveHistoryMessage(@DestinationVariable String channel, String message, SimpMessageHeaderAccessor ha) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        String sessionId = ha.getSessionId();
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        if (isHistoryMessage(message)) {
            User channelOwnerUser = null;
            try {
                channelOwnerUser = usersRepositoryHandler.getUserByNickname(channel);
                Stream stream = streamUtil.getStreamByUserNickname(channelOwnerUser.getNickname());
                List<ChatMessage> messages = messageRepository.findTop20ByStreamIdAndTimeAfterAndBannedIsFalseOrderByIdDesc(
                        stream.getId(), LocalDateTime.now().minusMinutes(30));
                List<MessageStatus> statusMessages = messageStatusRepository.findByStreamIdAndTimeAfterOrderByIdDesc(
                        stream.getId(), LocalDateTime.now().minusMinutes(30));

                messages.addAll(statusMessages);

                MessageStatus messageStatus = new MessageStatus();
                messageStatus.setType("history");
                Gson gson = new Gson();
                messageStatus.setInfo(gson.toJson(messages));

                simpMessagingTemplate.convertAndSendToUser(sessionId, "/topic/" + channel, messageStatus, createHeaders(sessionId));
                if (caravanUtil.getCaravanStatus() == CaravanStatus.GROUP_UP) {
                    MessageStatus messageStatus2 = caravanUtil.prepareCaravanStartMessage();
                    simpMessagingTemplate.convertAndSendToUser(sessionId, "/topic/" + channel, messageStatus2, createHeaders(sessionId));
                }
            } finally {
                usersRepositoryHandler.saveAndFree(channelOwnerUser);
            }
        } else {
            MessageStatus messageStatus = new MessageStatus();
            messageStatus.setType("caravanErrStatusJoinAnon");
            messageStatus.setInfo("");
            simpMessagingTemplate.convertAndSendToUser(sessionId, "/topic/" + channel, messageStatus, createHeaders(sessionId));
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

    private void solveHistory(String login, String channel) {
        User userIssuer = null;
        User channelOwnerUser = null;
        try {
            channelOwnerUser = usersRepositoryHandler.getUserByNickname(channel);
            Stream stream = streamUtil.getStreamByUserNickname(channelOwnerUser.getNickname());
            List<ChatMessage> messages = messageRepository.findTop20ByStreamIdAndTimeAfterAndBannedIsFalseOrderByIdDesc(stream.getId(), LocalDateTime.now().minusMinutes(30));
            List<MessageStatus> messages1 = messageStatusRepository.findByStreamIdAndTimeAfterOrderByIdDesc(stream.getId(), LocalDateTime.now().minusMinutes(30));

            userIssuer = usersRepositoryHandler.getUserByLogin(login);

            MessageStatus ms = new MessageStatus();
            ms.setType("history");
            ms.setInfo(String.valueOf(streamModerationUtil.canModerateStream(userIssuer, stream)));

            messages.add(0, ms);
            messages.addAll(messages1);

            MessageStatus messageStatus = new MessageStatus();
            messageStatus.setType("history");
            Gson gson = new Gson();
            messageStatus.setInfo(gson.toJson(messages));
            messagesUtil.convertAndSendToUser(login, channel, messageStatus);

            if (caravanUtil.getCaravanStatus() == CaravanStatus.GROUP_UP) {
                MessageStatus messageStatus2 = caravanUtil.prepareCaravanStartMessage();
                messagesUtil.convertAndSendToUser(login, channel, messageStatus2);
            }

            userPremiumUtil.solvePremiumExpiredSoon(login, channel, userIssuer);

            checkAllApiAuthCorrect(userIssuer, channel, channelOwnerUser);

        } finally {
            usersRepositoryHandler.saveAndFree(userIssuer);
            usersRepositoryHandler.saveAndFree(channelOwnerUser);
        }
    }


    private void checkAllApiAuthCorrect(User authorUser, String channel, User channelOwnerUser) {
        //streamer request
        if (authorUser.getId() == channelOwnerUser.getId()) {
            checkTwitch(authorUser, channel);
            checkGG(authorUser, channel);
            checkYT(authorUser, channel);
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
            messagesUtil.sendMessageToLogin(channel, "API MOTIVATION", info, authorUser.getLogin());
        }
    }

    private void checkTwitch(User authorUser, String channel) {
        UserTwitch userTwitch = authorUser.getUserTwitch();
        if (userTwitch != null) {
            if (!twitchTokenHandler.refresh(userTwitch)) {
                messagesUtil.sendErrorMessageToLogin(channel, "ACC REJOIN", "You must rejoin your Twitch account", authorUser.getLogin());
            } else {
                MessageStatus st = messagesUtil.getOkMessageStatus("ACC REJOIN OK", "Your Twitch account link is ok");
                messagesUtil.convertAndSendToUser(authorUser.getLogin(), channel, st);
            }
        }
    }


    private void checkGG(User authorUser, String channel) {
        UserGG userGG = authorUser.getUserGG();
        if (userGG != null) {
            if (!ggTokenHandler.refresh(userGG)) {
                messagesUtil.sendErrorMessageToLogin(channel, "ACC REJOIN", "You must rejoin your GoodGame account", authorUser.getLogin());
            } else {
                MessageStatus st = messagesUtil.getOkMessageStatus("ACC REJOIN OK", "Your GoodGame account link is ok");
                messagesUtil.convertAndSendToUser(authorUser.getLogin(), channel, st);
            }
        }
    }

    private void checkYT(User authorUser, String channel) {
        UserGoogle userGoogle = authorUser.getUserGoogle();
        if (userGoogle != null) {
            if (!ytTokenHandler.refresh(userGoogle)) {
                messagesUtil.sendErrorMessageToLogin(channel, "ACC REJOIN", "You must rejoin your YouTube account", authorUser.getLogin());
            } else {
                MessageStatus st = messagesUtil.getOkMessageStatus("ACC REJOIN OK", "Your YouTube account link is ok");
                messagesUtil.convertAndSendToUser(authorUser.getLogin(), channel, st);
            }
        }
    }


}
