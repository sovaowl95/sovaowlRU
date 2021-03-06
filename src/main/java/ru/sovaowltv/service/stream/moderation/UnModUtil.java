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
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.unclassified.LanguageUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import static ru.sovaowltv.service.unclassified.Constants.MOD_ACTION;

@Service
@RequiredArgsConstructor
public class UnModUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final MessageRepository messageRepository;

    private final UserUtil userUtil;
    private final MessagesUtil messagesUtil;
    private final LanguageUtil languageUtil;

    @Value("${website}")
    private String website;

    public MessageStatus unmodUserByNickName(User moderator, String text, String channel) {
        String[] split = text.trim().split(" ", 2);
        if (split.length != 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatUnmodByNick"));
        }

        Message message;
        try {
            message = messageRepository.findTop1ByNickEqualsOrderByIdDesc(split[1])
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "MESSAGE NOT FOUND"));
        } catch (ResponseStatusException e) {
            return messagesUtil.getUserNeverTypedOrNotExist(split[1]);
        }
        return unmodUserByMessageId(moderator, split[0] + " " + message.getId(), channel);
    }

    public MessageStatus unmodUserByMessageId(User moderator, String text, String channel) {
        User channelOwner = usersRepositoryHandler.getUserByNickname(channel);
        Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
        usersRepositoryHandler.saveAndFree(channelOwner);
        String[] split = text.trim().split(" ", 2);
        if (split.length != 2) {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.wrongFormatUnmodById"));
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
            MessageStatus messageStatus = unmodUserOnChannel(moderator, userForMod, stream, message);
            usersRepositoryHandler.free(userForMod);
            return messageStatus;
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.notFromWebsite"));
        }
    }

    private MessageStatus unmodUserOnChannel(User moderator, User userForUnMod, Stream stream, Message message) {
        if (!stream.getModeratorsList().contains(userForUnMod))
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.userNotModerator"));

        if (canModerateStreamAsOwner(moderator, stream)) {
            stream.getModeratorsList().remove(userForUnMod);
            streamRepositoryHandler.save(stream);
            return messagesUtil.getOkMessageStatus(MOD_ACTION,
                    "unmodUserByMessageId " + message.getId() + " " + moderator.getNickname() + " " + userForUnMod.getNickname());
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.modError"));
        }
    }

    private boolean canModerateStreamAsOwner(User user, Stream stream) {
        return userUtil.isAdminOrModerator(user) || stream.getUser().getId() == user.getId();
    }
}
