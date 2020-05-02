package ru.sovaowltv.service.factorys;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.webhooks.TwitchStreamLiveSub;
import ru.sovaowltv.service.stream.GGStreamUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.stream.TwitchStreamUtil;
import ru.sovaowltv.service.stream.YTStreamUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreamFactory {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final UserUtil userUtil;
    private final TwitchStreamUtil twitchStreamUtil;
    private final GGStreamUtil ggStreamUtil;
    private final YTStreamUtil ytStreamUtil;

    private final TwitchStreamLiveSub twitchStreamLiveSub;

    //todo: ANOTHER API SERVICE
    public void createStream() {
        User user = null;
        try {
            user = userUtil.getUser();
            Optional<Stream> streamOptional = streamRepositoryHandler.getByUserId(user.getId());
            if (streamOptional.isPresent())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already have stream");

            Stream stream = new Stream();
            stream.setStreamName("setStreamName");
            stream.setGame("setGame");
            stream.setStreamDescription("setStreamDescription");
            stream.setUser(user);
            stream = streamRepositoryHandler.saveAndFlush(stream);

            if (user.getUserTwitch() != null) {
                twitchStreamUtil.createTwitchChatReader(user, user.getUserTwitch(), user.getNickname(), true);
                twitchStreamLiveSub.subForStream(user, user.getUserTwitch(), stream);
            }

            if (user.getUserGG() != null) {
                ggStreamUtil.createGGChatReader(user, user.getUserGG(), user.getNickname(), true);
            }

            if (user.getUserGoogle() != null) {
                ytStreamUtil.createYTChatReader(user, user.getUserGoogle(), user.getNickname(), true);
            }

        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }
}
