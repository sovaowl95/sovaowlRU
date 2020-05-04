package ru.sovaowltv.service.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.chat.Smiles;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.chat.realization.ApiTimeouts;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
//todo: ANOTHER API SERVICE
public class StreamModerationUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final UserUtil userUtil;

    private final ApiTimeouts apiTimeouts;

    public boolean canModerateStreamAsOwner(User user, Stream stream) {
        return userUtil.isAdminOrModerator(user) || stream.getUser().getId() == user.getId();
    }

    public boolean canModerateStream(User user, Stream stream) {
        return userUtil.isAdminOrModerator(user) || stream.getModeratorsList().contains(user) || stream.getUser().getId() == user.getId();
    }

    public boolean canChatInChannelBan(User user, String channel) {
        User channelOwner = null;
        try {
            channelOwner = usersRepositoryHandler.getUserByNickname(channel);
            Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
            return !stream.getBansList().contains(user);
        } finally {
            usersRepositoryHandler.free(channelOwner);
        }
    }

    public boolean canChatInChannelTimeout(User user, String channel) {
        User channelOwner = null;
        try {
            channelOwner = usersRepositoryHandler.getUserByNickname(channel);
            Stream stream = streamRepositoryHandler.getByUser(channelOwner)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "STREAM NOT FOUND"));
            Map<User, LocalDateTime> timeoutMap = apiTimeouts.getTimeoutsByStreamId(stream.getId());
            if (timeoutMap == null || !timeoutMap.containsKey(user)) return true;
            LocalDateTime localDateTime = timeoutMap.get(user);
            if (localDateTime == null) return true;

            if (localDateTime.isBefore(LocalDateTime.now())) {
                apiTimeouts.getTimeoutsByStreamId(stream.getId()).remove(user);
                return true;
            }
            return false;
        } finally {
            usersRepositoryHandler.free(channelOwner);
        }
    }


    public String generateSmilesInfo(String message, Smiles smiles) {
        Set<String> meetId = new HashSet<>();
        StringBuilder smilesMeta = new StringBuilder();
        for (String word : message.split(" ")) {
            if (smiles.isSmile(word) && smiles.canUseSmile(word)) {
                String id = smiles.getSmile(word);
                if (meetId.contains(id)) continue;
                else meetId.add(id);
                smilesMeta.append(id).append(":");
                int fromIndex = -1;
                while (true) {
                    int index = message.indexOf(word, fromIndex);
                    if (index != -1) {
                        if (checkIndex(message, word, index)) {
                            fromIndex = index + word.length();
                            smilesMeta.append(index).append("-").append(fromIndex).append(",");
                        } else {
                            fromIndex++;
                        }
                    } else {
                        smilesMeta.append("/");
                        break;
                    }
                }
                int index = smilesMeta.length() - 2;
                if (index >= 0 && smilesMeta.length() > index &&
                        smilesMeta.charAt(smilesMeta.length() - 2) == ',') {
                    smilesMeta.deleteCharAt(smilesMeta.length() - 2);
                }
            }
        }
        int index = smilesMeta.length() - 1;
        if (index >= 0 && smilesMeta.length() > index &&
                smilesMeta.charAt(smilesMeta.length() - 1) == '/') {
            smilesMeta.deleteCharAt(smilesMeta.length() - 1);
        }
        return smilesMeta.toString();
    }

    private boolean checkIndex(String message, String word, int index) {
        char[] chars = message.toCharArray();
        int indexFrom = index - 1;
        int indexTo = index + word.length();

        return (indexFrom <= 0 || Character.isWhitespace(chars[indexFrom]))
                && (indexTo >= message.length() || Character.isWhitespace(chars[indexTo]));
    }
}