package ru.sovaowltv.service.messages;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageRepository;
import ru.sovaowltv.repositories.messages.MessageStatusRepository;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class MessagesUtil {
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;

    private final MessageSource messageSource;

    private final SimpMessagingTemplate simpMessagingTemplate;

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
            return getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.userNeverTypedHere", null, LocaleContextHolder.getLocale()));
        } else {
            return getErrorMessageStatus("modAction",
                    messageSource.getMessage("pages.chat.message.moderator.userNotFound", null, LocaleContextHolder.getLocale()));
        }
    }

    public void sendErrorMessageToLogin(String channel, String type, String info, String login) {
        MessageStatus st = getErrorMessageStatus(type, info);
        simpMessagingTemplate.convertAndSendToUser(login, "/topic/" + channel, st);
    }

    public void sendMessageToLogin(String channel, String type, String info, String login) {
        MessageStatus messageStatus = getOkMessageStatus(type, info);
        simpMessagingTemplate.convertAndSendToUser(login, "/topic/" + channel, messageStatus);
    }

    public void sendLvlUpMessage(User user, String channel) {
        Gson gson = new Gson();
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType("rankUp");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("nickName", String.valueOf(user.getNickname()));
        hashMap.put("level", String.valueOf(user.getLevel()));
        messageStatus.setInfo(gson.toJson(hashMap));
        simpMessagingTemplate.convertAndSend("/topic/" + channel, gson.toJson(messageStatus));
    }

    public void convertAndSendToUser(String login, String channel, Object message) {
        simpMessagingTemplate.convertAndSendToUser(login, "/topic/" + channel, message);
    }

    public void convertAndSend(String channel, Object message) {
        simpMessagingTemplate.convertAndSend("/topic/" + channel, message);
    }

    public void createAndSendMessageStatus(
            String type,
            String key,
            String userName,
            String channel
    ) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(type);
        messageStatus.setInfo(messageSource.getMessage(key, null, LocaleContextHolder.getLocale()));
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
}
