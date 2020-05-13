package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.support.MissingSessionUserException;
import org.springframework.stereotype.Controller;
import ru.sovaowltv.exceptions.stream.StreamNotFoundException;
import ru.sovaowltv.model.chat.ChatMessage;
import ru.sovaowltv.model.multistream.MultiStream;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.multistream.MultiStreamRepository;
import ru.sovaowltv.service.chat.ChatHistoryUtil;
import ru.sovaowltv.service.chat.ChatUtil;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.log.LoggerUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static ru.sovaowltv.service.unclassified.Constants.MESSAGE;
import static ru.sovaowltv.service.unclassified.Constants.MOD_ACTION;

@Controller
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:constants.yml")
public class ChatController {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final MultiStreamRepository multiStreamRepository;

    private final UserUtil userUtil;
    private final StreamUtil streamUtil;
    private final ChatUtil chatUtil;
    private final ChatHistoryUtil chatHistoryUtil;
    private final LoggerUtil loggerUtil;

    private final DataExtractor dataExtractor;

    private final HtmlTagsClear htmlTagsClear;

    @MessageMapping("ms/{channel}")
    @SendTo("/topic/ms/{channel}")
    public ChatMessage solveMessageMS(@DestinationVariable String channel, java.security.Principal principal,
                                      String messageText, SimpMessageHeaderAccessor ha) {
        Stream stream = null;
        try {
            stream = streamUtil.getStreamByUserNickname(channel);
        } catch (StreamNotFoundException e) {
            Optional<MultiStream> multiStreamOptional = multiStreamRepository.findById(Long.parseLong(channel));
            if (multiStreamOptional.isPresent()) {
                channel = "ms/" + channel;

                stream = new Stream();
                stream.setId(-multiStreamOptional.get().getId());
                stream.setUser(multiStreamOptional.get().getUser());
                stream.getUser().setLogin(channel);

                stream.setSubscribersList(Collections.emptySet());
                stream.setSpammerSet(Collections.emptySet());
                stream.setModeratorsList(Collections.emptySet());
                stream.setCommandSet(Collections.emptySet());
                stream.setFollowersList(Collections.emptySet());
                stream.setBansList(Collections.emptySet());

                stream.setLive(true);
            }
        }
        return getChatMessage(channel, principal, messageText, ha, stream);
    }

    @MessageMapping("/{channel}")
    @SendTo("/topic/{channel}")
    public ChatMessage solveMessage(@DestinationVariable String channel, java.security.Principal principal,
                                    String messageText, SimpMessageHeaderAccessor ha) {
        Stream stream = streamUtil.getStreamByUserNickname(channel);
        return getChatMessage(channel, principal, messageText, ha, stream);
    }

    @Nullable
    private ChatMessage getChatMessage(@DestinationVariable String channel, Principal principal, String messageText, SimpMessageHeaderAccessor ha, Stream stream) {
        if (principal == null) {
            chatHistoryUtil.solveHistoryMessage(stream, channel, messageText, ha);
            return null;
        }
        loggerUtil.logMessageFromChat(channel, principal, messageText);
        userUtil.setAuthContextIfItsEmpty(principal);
        User user = null;
        try {
            user = usersRepositoryHandler.getUserByLogin(principal.getName());
            messageText = htmlTagsClear.removeTags(messageText);

            Map<String, Object> map = dataExtractor.extractMapFromString(messageText);
            String messageType = String.valueOf(map.get("type"));
            if (!chatUtil.userAllowedSendMessages(channel, messageType, user, stream)) {
                log.debug("user not allowed send message. channel:{}, principal:{}, messageText{}", channel, principal, messageText);
                return null;
            }
            switch (messageType) {
                case MESSAGE:
                    return chatUtil.solveMessageMessage(channel, map, user, stream);
                case "command":
                    return chatUtil.solveCommandMessage(channel, map, user, stream);
                case "history":
                    return chatHistoryUtil.solveHistoryMessage(channel, user, stream);
                case "moderator":
                    chatUtil.solveIsUserModeratorMessage(channel, user, stream);
                    return null;
                case MOD_ACTION:
                    return chatUtil.solveModActionMessage(channel, map, user, stream);
                default:
                    log.error("cant find message type:{}", messageType);
            }
            return null;
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    @MessageExceptionHandler(MissingSessionUserException.class)
    public void handleNoUserException(Exception e) {
        log.info("chat MissingSessionUserException -> {}", e.getMessage());
    }
}
