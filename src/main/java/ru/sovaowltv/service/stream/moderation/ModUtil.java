package ru.sovaowltv.service.stream.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageRepository;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamModerationUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.unclassified.LanguageUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import static ru.sovaowltv.service.unclassified.Constants.MOD_ACTION;

@Service
@RequiredArgsConstructor
public class ModUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final MessageRepository messageRepository;

    private final StreamModerationUtil streamModerationUtil;
    private final MessagesUtil messagesUtil;
    private final LanguageUtil languageUtil;

    @Value("${website}")
    private String website;

    public MessageStatus modUserByNickName(User moderator, String text, String channel) {
        String[] split = text.trim().split(" ", 2);
        if (split.length != 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatModByNick"));
        }

        Message message;
        try {
            message = messageRepository.findTop1ByNickEqualsOrderByIdDesc(split[1])
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "MESSAGE NOT FOUND"));
        } catch (ResponseStatusException e) {
            return messagesUtil.getUserNeverTypedOrNotExist(split[1]);
        }
        return modUserByMessageId(moderator, split[0] + " " + message.getId(), channel);
    }

    public MessageStatus modUserByMessageId(User moderator, String text, String channel) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.free(channelOwner);
        String[] split = text.trim().split(" ", 2);
        if (split.length != 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatModById"));
        }

        String targetId = split[1];
        Message message;
        try {
            message = messageRepository.findByIdAndSource(Long.parseLong(targetId), website)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CAN'T FIND MESSAGE"));
        } catch (Exception e) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.notFromWebsite"));
        }

        String issuerId = message.getIssuerId();
        if (message.getSource().equalsIgnoreCase(website)) {
            User userForMod = usersRepositoryHandler.getUserById(issuerId);
            MessageStatus messageStatus = modUserOnChannel(moderator, userForMod, stream, message);
            usersRepositoryHandler.free(userForMod);
            return messageStatus;
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.notFromWebsite"));
        }
    }

    private MessageStatus modUserOnChannel(User moderator, User userForMod, Stream stream, Message message) {
        if (stream.getModeratorsList().contains(userForMod))
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.userModerator"));

        if (streamModerationUtil.canModerateStreamAsOwner(moderator, stream)) {
            stream.getModeratorsList().add(userForMod);
            streamRepositoryHandler.save(stream);
            return messagesUtil.getOkMessageStatus(MOD_ACTION,
                    "modUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + userForMod.getNickname());
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.modError"));
        }
    }
}
