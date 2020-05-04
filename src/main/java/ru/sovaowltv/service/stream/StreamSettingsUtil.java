package ru.sovaowltv.service.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.website.DiscordNotificationRepository;
import ru.sovaowltv.service.chat.realization.ApiChats;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.notifications.discord.DiscordNotificationUtil;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;

import java.util.Map;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class StreamSettingsUtil {
    private final DiscordNotificationRepository discordNotificationRepository;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final StreamUtil streamUtil;
    private final DiscordNotificationUtil discordNotificationUtil;
    private final DataExtractor dataExtractor;

    private final HtmlTagsClear htmlTagsClear;
    private final ApiChats apiChats;

    public void deleteStream(String streamName) {
        Stream stream = streamUtil.getStreamByUserNickname(streamName);
        streamUtil.isYourStream(stream);
        apiChats.deleteStreamChatConnection(stream);
        streamRepositoryHandler.delete(stream);
    }

    public void changeStreamName(String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        Stream stream = streamUtil.getStreamByAuthContext();
        streamUtil.isYourStream(stream);
        String streamName = String.valueOf(map.get("streamName"));
        stream.setStreamName(htmlTagsClear.removeTags(streamName));
        streamRepositoryHandler.save(stream);
    }

    public void changeGame(String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        Stream stream = streamUtil.getStreamByAuthContext();
        streamUtil.isYourStream(stream);
        String game = String.valueOf(map.get("streamGame"));
        game = htmlTagsClear.removeTags(game);
        if (game.trim().length() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Length must be longer than 0");
        stream.setGame(game);
        streamRepositoryHandler.save(stream);
    }

    public void changeChatDailyInfo(String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        Stream stream = streamUtil.getStreamByAuthContext();
        streamUtil.isYourStream(stream);
        String chatDailyInfo = String.valueOf(map.get("chatDailyInfo"));
        chatDailyInfo = htmlTagsClear.removeTags(chatDailyInfo);
        if (chatDailyInfo.trim().length() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Length must be longer than 0");
        stream.setChatDailyInfo(chatDailyInfo);
        streamRepositoryHandler.save(stream);
    }

    public void changeDiscordNotification(String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        Stream stream = streamUtil.getStreamByAuthContext();
        streamUtil.isYourStream(stream);
        String changeDiscordNotification = String.valueOf(map.get("changeDiscordNotification"));
        changeDiscordNotification = htmlTagsClear.removeTags(changeDiscordNotification);

        if (discordNotificationUtil.checkChannelName(changeDiscordNotification, stream)) {
            stream.getDiscordNotification().setChannel(changeDiscordNotification);
            discordNotificationRepository.save(stream.getDiscordNotification());
            streamRepositoryHandler.save(stream);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find id");
        }
    }

    public void changeDiscordText(String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        Stream stream = streamUtil.getStreamByAuthContext();
        streamUtil.isYourStream(stream);
        String discordText = String.valueOf(map.get("discordText"));
        discordText = htmlTagsClear.removeTags(discordText);
        stream.getDiscordNotification().setText(discordText);
        discordNotificationRepository.save(stream.getDiscordNotification());
        streamRepositoryHandler.save(stream);
    }

    public void changeStreamStatus(Stream stream, String body) {
        Map<String, Object> map = dataExtractor.extractMapFromString(body);
        streamUtil.isYourStream(stream);
        stream.setLive(Boolean.parseBoolean(map.get("status").toString()));
        streamRepositoryHandler.save(stream);
    }
}
