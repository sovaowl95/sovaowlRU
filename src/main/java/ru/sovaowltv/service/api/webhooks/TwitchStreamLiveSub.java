package ru.sovaowltv.service.api.webhooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.requsts.TwitchRequestUtil;
import ru.sovaowltv.service.api.token.TwitchTokenHandler;
import ru.sovaowltv.service.io.URLConnectionPrepare;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/twitch.yml")
public class TwitchStreamLiveSub {
    private final TwitchRequestUtil twitchRequestUtil;

    private final TwitchTokenHandler twitchTokenHandler;
    private final URLConnectionPrepare urlConnectionPrepare;

    public boolean subForStream(User userFromWebsite, UserTwitch userTwitch, Stream stream) {
        String userTwitchChannelId = userTwitch.getUserTwitchChannelId() == null
                ? getChannelId(userTwitch)
                : userTwitch.getUserTwitchChannelId();
        if (userFromWebsite != null && !userTwitchChannelId.isBlank()) {
            return sub(userTwitch, stream);
        }
        return false;
    }

    private boolean sub(UserTwitch userTwitch, Stream stream) {
        log.info("sub {}", userTwitch.getNick());
        twitchTokenHandler.refresh(userTwitch);
        String body = twitchRequestUtil.getBodyForWebhookSub(userTwitch, stream);
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://api.twitch.tv/helix/webhooks/hub");
        twitchRequestUtil.setAuthHeaders(userTwitch, connection);
        urlConnectionPrepare.setPOSTAndBody(body, connection);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                log.info(input);
            }
        } catch (IOException e) {
            log.error("webhook", e);
        }
        return true;
    }


    private boolean unsub(UserTwitch userTwitch, Stream stream) {
        //tODO: unsub
        return false;
    }

    private String getChannelId(UserTwitch userTwitch) {
        twitchTokenHandler.refresh(userTwitch);
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://api.twitch.tv/helix/users" + "?"
                        + "login=" + userTwitch.getNick());
        twitchRequestUtil.setAuthHeaders(userTwitch, connection);
        //лишний заголовок может причинить "неудобства"? "Accept", "application/vnd.twitchtv.v5+json")

        return twitchRequestUtil.getChannelId(userTwitch, connection);
    }
}