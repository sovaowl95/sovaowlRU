package ru.sovaowltv.service.api.webhooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.api.twitch.TwitchWebHook;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.requsts.TwitchRequest;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserTwitchUtil;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@PropertySource("classpath:api/twitch.yml")
public class TwitchWebHookUtil {
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final UserTwitchUtil userTwitchUtil;
    private final StreamUtil streamUtil;

    private final TwitchRequest twitchRequest;

    private final Set<String> setOfWebHooksId = new HashSet<>();

    public Stream setStreamSettingByWebHookAndGetStream(TwitchWebHook twitchWebhook) {
        Stream stream = getStreamByWebHook(twitchWebhook);

        String game = twitchRequest.changeTwitchGameIdToTitle(twitchWebhook.getGameId(), stream.getUser().getUserTwitch());
        String streamTitle = twitchWebhook.getStreamTitle();

        stream.setStreamName(streamTitle);
        stream.setGame(game);
        stream.setLive(true);
        streamRepositoryHandler.save(stream);

        return stream;
    }

    public boolean alreadySolvedHook(TwitchWebHook twitchWebhook) {
        if (setOfWebHooksId.contains(twitchWebhook.getId())) return true;
        setOfWebHooksId.add(twitchWebhook.getId());
        return false;
    }

    private Stream getStreamByWebHook(TwitchWebHook twitchWebhook) {
        User user = userTwitchUtil.getUserByUserTwitchChannelId(twitchWebhook.getChannelId());
        return streamUtil.getStreamByUserNickname(user.getNickname());
    }
}
