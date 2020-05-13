package ru.sovaowltv.service.messages;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageRepository;
import ru.sovaowltv.repositories.messages.MessageStatusRepository;
import ru.sovaowltv.service.unclassified.LanguageUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.HashMap;
import java.util.Optional;

import static ru.sovaowltv.service.unclassified.Constants.MOD_ACTION;
import static ru.sovaowltv.service.unclassified.Constants.RANK_UP;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class MessagesUtil {
    private static final String TOPIC = "/topic/";
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final LanguageUtil languageUtil;

    @Value("${err}")
    private String error;

    public Optional<Message> getMessageOptionalById(String id) {
        return messageRepository.findById(Long.parseLong(id));
    }

    public Optional<MessageStatus> getMessageStatusOptionalById(String id) {
        return messageStatusRepository.findById(Long.parseLong(id));
    }

    public Optional<Message> getMessageOptionalBySubId(String id) {
        return messageRepository.findByMessageSubId(id);
    }

    public MessageStatus getErrorMessageStatus(String type, String info) {
        return getOkMessageStatus(type, error + " " + info);
    }

    public MessageStatus getOkMessageStatus(String type, String info) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(type);
        messageStatus.setInfo(info);
        return messageStatus;
    }

    public MessageStatus getUserNeverTypedOrNotExist(String nickName) {
        Optional<User> userByNickname = usersRepositoryHandler.getUserByNicknameOptional(nickName);
        if (userByNickname.isPresent()) {
            usersRepositoryHandler.free(userByNickname.get());
            return getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.userNeverTypedHere"));

        } else {
            return getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.userNotFound"));
        }
    }

    public void sendErrorMessageToLogin(String channel, String type, String info, String login) {
        MessageStatus st = getErrorMessageStatus(type, info);
        simpMessagingTemplate.convertAndSendToUser(login, TOPIC + channel, st);
    }

    public void sendMessageToLogin(String channel, String type, String info, String login) {
        MessageStatus messageStatus = getOkMessageStatus(type, info);
        simpMessagingTemplate.convertAndSendToUser(login, TOPIC + channel, messageStatus);
    }

    public void sendLvlUpMessage(User user, String channel) {
        Gson gson = new Gson();
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(RANK_UP);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("nickName", String.valueOf(user.getNickname()));
        hashMap.put("level", String.valueOf(user.getLevel()));
        messageStatus.setInfo(gson.toJson(hashMap));
        simpMessagingTemplate.convertAndSend(TOPIC + channel, gson.toJson(messageStatus));
    }

    public void convertAndSendToUser(String login, String channel, Object message) {
        simpMessagingTemplate.convertAndSendToUser(login, TOPIC + channel, message);
    }

    public void convertAndSend(String channel, Object message) {
        simpMessagingTemplate.convertAndSend(TOPIC + channel, message);
    }

    public void createAndSendMessageStatus(
            String type,
            String key,
            String userName,
            String channel
    ) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(type);
        messageStatus.setInfo(languageUtil.getStringFor(key));
        convertAndSendToUser(userName, channel, messageStatus);
    }

    public void createAndSendSimpleMessageStatus(
            String type,
            String info,
            String userName,
            String channel
    ) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(type);
        messageStatus.setInfo(info);
        convertAndSendToUser(userName, channel, messageStatus);
    }

    public void createAndSendMessageStatus(
            String type,
            String info,
            String sessionId,
            String destination,
            MessageHeaders messageHeaders
    ) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(type);
        messageStatus.setInfo(info);
        simpMessagingTemplate.convertAndSendToUser(sessionId, destination, messageStatus, messageHeaders);
    }

    public void sendMessageWithHeaders(
            String sessionId,
            String destination,
            MessageStatus messageStatus,
            MessageHeaders messageHeaders
    ) {
        simpMessagingTemplate.convertAndSendToUser(sessionId, destination, messageStatus, messageHeaders);
    }
}
