package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.support.MissingSessionUserException;
import org.springframework.stereotype.Controller;
import ru.sovaowltv.model.chat.ChatMessage;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.ChatHistoryUtil;
import ru.sovaowltv.service.chat.ChatUtil;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.log.LoggerUtil;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:constants.yml")
public class ChatController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final ChatUtil chatUtil;
    private final ChatHistoryUtil chatHistoryUtil;
    private final LoggerUtil loggerUtil;

    private final DataExtractor dataExtractor;

    private final HtmlTagsClear htmlTagsClear;

//    @MessageMapping("ms/{channel}")
//    @SendTo("/topic/ms/{channel}")
//    public ChatMessage solveMessageMS(@DestinationVariable String channel, java.security.Principal principal,
//                                      String messageText, SimpMessageHeaderAccessor ha) {
//
//    }

    @MessageMapping("/{channel}")
    @SendTo("/topic/{channel}")
    public ChatMessage solveMessage(@DestinationVariable String channel, java.security.Principal principal,
                                    String messageText, SimpMessageHeaderAccessor ha) {
        if (principal == null) {
            chatHistoryUtil.solveHistoryMessage(channel, messageText, ha);
            return null;
        }
        loggerUtil.logMessageFromChat(channel, principal, messageText);
        userUtil.setAuthContextIfItsEmpty(principal);
        User userForCheck = null;
        try {
            userForCheck = usersRepositoryHandler.getUserByLogin(principal.getName());
            messageText = htmlTagsClear.removeTags(messageText);

            Map<String, Object> map = dataExtractor.extractMapFromString(messageText);
            String messageType = String.valueOf(map.get("type"));
            if (!chatUtil.userAllowedSendMessages(channel, principal, messageType, userForCheck)) {
                log.debug("user not allowed send message. channel:{}, principal:{}, messageText{}", channel, principal, messageText);
                return null;
            }
            switch (messageType) {
                case "message":
                    return chatUtil.solveMessageMessage(channel, principal, map, userForCheck);
                case "command":
                    return chatUtil.solveCommandMessage(channel, principal, map, userForCheck);
                case "history":
                    return chatHistoryUtil.solveHistoryMessage(channel, principal);
                case "moderator":
                    chatUtil.solveIsUserModeratorMessage(channel, principal);
                    return null;
                case "modAction":
                    return chatUtil.solveModActionMessage(channel, principal, map);
                default:
                    log.error("cant find message type:" + messageType);
            }
            return null;
        } finally {
            usersRepositoryHandler.saveAndFree(userForCheck);
        }
    }

    @MessageExceptionHandler(MissingSessionUserException.class)
    public void handleNoUserException(Exception e) {
        log.info("chat MissingSessionUserException -> " + e.getMessage());
    }
}
