package ru.sovaowltv.service.chat.realization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import ru.sovaowltv.model.apiauth.ApiUser;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.user.UsersGoogleRepository;
import ru.sovaowltv.service.api.token.YTTokenHandler;
import ru.sovaowltv.service.chat.util.YTChatUtil;
import ru.sovaowltv.service.io.URLConnectionPrepare;
import ru.sovaowltv.service.messages.MessageValidationStatus;

import javax.net.ssl.HttpsURLConnection;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static ru.sovaowltv.service.unclassified.Constants.SPAM;

@ClientEndpoint
@PropertySources({
        @PropertySource("classpath:constants.yml"),
        @PropertySource("classpath:api/google.yml")
})
@Slf4j
public class YTChat extends ApiForChat {
    @Autowired
    private YTTokenHandler ytTokenHandler;
    @Autowired
    private UsersGoogleRepository usersGoogleRepository;
    @Autowired
    private URLConnectionPrepare urlConnectionPrepare;
    @Autowired
    private YTChatUtil ytChatUtil;

    @Value("${yt}")
    private String source;

    @Value("${google_clientId}")
    private String googleclientId;

    private long lastUpdateTimeInMillis;
    private boolean isConnected;

    private boolean quotaLimited;
    private int sleepSecCauseQuota;

    private final HashSet<String> messages = new HashSet<>();

    public YTChat(String topicTarget,
                  String ip, String port, String clientId, String clientSecret,
                  String accessToken,
                  String youtubeChannel, ApiUser apiUser,
                  boolean canRead, boolean canWrite) {
        super(topicTarget,
                ip, port, clientId, clientSecret,
                accessToken,
                youtubeChannel, apiUser,
                canRead, canWrite);
        if (canRead) nickNames = new HashSet<>();
    }


    @Override
    public boolean refreshToken() {
        if (ytTokenHandler.refresh((UserGoogle) apiUser)) {
            Optional<UserGoogle> byId = usersGoogleRepository.findBySub(apiUser.getSub());
            try {
                apiUser = byId.orElseThrow(() -> new RuntimeException("User not found"));
            } catch (RuntimeException e) {
                disconnect();
            }
            accessToken = apiUser.getAccessToken();
            return true;
        }
        return false;
    }

    @Override
    protected String getConnectString() {
        return "";
    }

    @Override
    @OnOpen
    public void onOpen(Session session) {
    }

    @Override
    @OnMessage
    public void onMessage(Session session, String ggMessage) {
    }

    @Override
    public boolean sendToServer(String text) {
        if (!work) throw new RuntimeException("Already stopped");
        if (quotaLimited) return false;
        if (!isConnected) return false;
        refreshToken();

        String body = ytChatUtil.prepareBodySendToServer(text, (UserGoogle) apiUser);

        HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://www.googleapis.com/youtube/v3/liveChat/messages" + "?" +
                "part=snippet,authorDetails" + "&" +
                "key=" + googleclientId);
        ytChatUtil.addConnectionAuth(connection, accessToken);
        urlConnectionPrepare.setPOSTAndBody(body, connection);

        JsonObject jsonObject = ioExtractor.extractJsonObject(connection);
        Map<String, String> mapE = ytChatUtil.extractMessageData(jsonObject);
        if (mapE.isEmpty())
            return false;

        String id = mapE.get("id");
        messages.add(id);
        return true;
    }


    @Override
    public void sendMessage(Message message, boolean fromSelf) {
        String text = message.getText();
        if (message.getOriginalMessage() != null) text = message.getOriginalMessage();

        String nick = "";
        if (!fromSelf || (source.equalsIgnoreCase(message.getSource()) && channelToConnect.equalsIgnoreCase(message.getNick()))) {
            nick = message.getNick();
        }
        sendToServer(sovaowlRu + " [" + message.getId() + "] " + nick + " : " + text);
    }

    @Override
    public void sendInviteMessage(String nickName) {
        sendToServer(sovaowlRu + " [0] " + "Привет, " + nickName + " , заходи на сайт " + sovaowlRuHttps + " . Мне нужна помощь в тестировании!)");
    }

    @Override
    public void timeoutUser(String nickName, String time, String reason, Message message) {
        if (!isConnected) return;
        refreshToken();
        HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://www.googleapis.com/youtube/v3/liveChat/bans" + "?" +
                "part=snippet" + "&" +
                "key=" + googleclientId);
        ytChatUtil.addConnectionAuth(connection, accessToken);
        String body = ytChatUtil.prepareTimeoutUserBody(time, message, ((UserGoogle) apiUser));
        urlConnectionPrepare.setPOSTAndBody(body, connection);
        JsonObject jsonObject = ioExtractor.extractJsonObject(connection);
        Map<String, String> mapE = ytChatUtil.extractMessageData(jsonObject);
        String id = mapE.get("id");
        messages.add(id);
    }

    @Override
    public void unTimeoutUser(String nickName, Message message) {
        timeoutUser(nickName, "1", "untimeout", message);
    }

    @Override
    public void unBanUser(String nickName, Message message) {
        timeoutUser(nickName, "1", "unban", message);
    }

    @Override
    public void banUser(String nickName, String reason, Message message) {
        if (!isConnected) return;
        refreshToken();
        HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://www.googleapis.com/youtube/v3/liveChat/bans" + "?" +
                "part=snippet" + "&" +
                "key=" + googleclientId);
        ytChatUtil.addConnectionAuth(connection, accessToken);
        String body = ytChatUtil.prepareBanBody(message, ((UserGoogle) apiUser));
        urlConnectionPrepare.setPOSTAndBody(body, connection);
        JsonObject jsonObject = ioExtractor.extractJsonObject(connection);
        Map<String, String> mapE = ytChatUtil.extractMessageData(jsonObject);
        String id = mapE.get("id");
        messages.add(id);
    }


    @Override
    public void deleteMessage(Message message) {
        try {
            if (!isConnected) return;
            refreshToken();
            HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://www.googleapis.com/youtube/v3/liveChat/messages" + "?" +
                    "id=" + message.getMessageSubId());
            ytChatUtil.addConnectionAuth(connection, accessToken);
            connection.setRequestMethod("DELETE");
            int responseCode = connection.getResponseCode();
            log.info("deleteMessage {}", responseCode);
        } catch (IOException e) {
            log.error("delete message error", e);
        }
    }

    @Override
    public void purgeUser(String nickName, Message message) {
        try {
            if (!isConnected) return;
            refreshToken();
            HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://www.googleapis.com/youtube/v3/liveChat/messages" + "?" +
                    "id=" + message.getMessageSubId());
            connection.setRequestMethod("DELETE");
            ytChatUtil.addConnectionAuth(connection, accessToken);
            int responseCode = connection.getResponseCode();
            log.info("purgeUser {}", responseCode);
        } catch (IOException e) {
            log.error("purge user", e);
        }
    }


    @Override
    public boolean connect() {
        refreshToken();
        HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://www.googleapis.com/youtube/v3/liveBroadcasts" + "?" +
                "part=snippet,contentDetails,status" + "&" +
                "broadcastStatus=active" + "&" +
                "broadcastType=all" + "&" +
                "key=" + googleclientId);
        ytChatUtil.addConnectionAuth(connection, accessToken);
        JsonObject jObj = ioExtractor.extractJsonObject(connection);
        JsonArray jArr = jObj.getAsJsonArray("items");
        if (jArr == null) {
            if (jObj.toString().contains("\"domain\":\"youtube.quota\",\"reason\":\"quotaExceeded\",")
                    || jObj.toString().contains("\"domain\":\"usageLimits\",\"reason\":\"dailyLimitExceeded\",")) {
                log.error("Youtube quota...");
                quotaLimited = true;
                ZonedDateTime america = ZonedDateTime.now(ZoneId.of("America/Tijuana"));
                ZonedDateTime americaPlusDayTR = america.plusDays(1).truncatedTo(ChronoUnit.DAYS);
                long secBetween = ChronoUnit.SECONDS.between(america, americaPlusDayTR);
                secBetween = Math.abs(secBetween) + 10;
                log.warn("SLEEP UNTIL PACIFIC MIDNIGHT {}", secBetween);
                sleepSecCauseQuota = (int) secBetween;
                if (sleepSecCauseQuota > 80000) sleepSecCauseQuota = 100;
                return false;
            }
            log.error("Youtube connect failed: {}", jObj.toString());
            return false;
        }
        if (jArr.size() == 0) {
            return false;
        }
        jObj = jArr.get(0).getAsJsonObject();

        String id = jObj.getAsJsonPrimitive("id").getAsString();
        JsonObject jObj2 = jObj.getAsJsonObject("snippet");
        String liveChatId = jObj2.getAsJsonPrimitive("liveChatId").getAsString();

        ((UserGoogle) apiUser).setVideoId(id);
        ((UserGoogle) apiUser).setLiveChatId(liveChatId);
        usersGoogleRepository.save(((UserGoogle) apiUser));
        return true;
    }


    private boolean videoAndChatIdCorrect() {
        refreshToken();
        HttpsURLConnection connection = urlConnectionPrepare.getConnection("https://www.googleapis.com/youtube/v3/liveBroadcasts" + "?" +
                "part=snippet,contentDetails,status" + "&" +
                "broadcastStatus=active" + "&" +
                "broadcastType=all" + "&" +
                "key=" + googleclientId);
        ytChatUtil.addConnectionAuth(connection, accessToken);

        JsonObject jObj = ioExtractor.extractJsonObject(connection);
        JsonArray jArr = jObj.getAsJsonArray("items");
        if (jArr.size() == 0) {
            return false;
        }
        jObj = jArr.get(0).getAsJsonObject();

        String id = jObj.getAsJsonPrimitive("id").getAsString();
        JsonObject jObj2 = jObj.getAsJsonObject("snippet");
        String liveChatId = jObj2.getAsJsonPrimitive("liveChatId").getAsString();

        return id.equals(((UserGoogle) apiUser).getVideoId()) && liveChatId.equals(((UserGoogle) apiUser).getLiveChatId());
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!work) break;
                while (!connect()) {
                    if (!work) break;
                    if (quotaLimited) {
                        log.info("YT QUOTA! TIME TO SLEEP D:");
                        timeUtil.sleepSeconds(sleepSecCauseQuota);
                        quotaLimited = false;
                        isConnected = false;
                    } else {
                        timeUtil.sleepSeconds(60);
                        isConnected = false;
                    }
                }
                isConnected = true;

                while (work) {
                    try {
                        while (!refreshToken()) timeUtil.sleepSeconds(5);
                        int timeout = readMessages();
                        if (timeout == -1)
                            break; //stream is over
                        timeUtil.sleepSeconds(timeout);
                    } catch (NullPointerException e) {
                        log.error("np exception on yt readmessage", e);
                        break;
                    } catch (RuntimeException e) {
                        log.error("yt cycle error run", e);
                        break;
                    } catch (Exception e) {
                        log.error("yt cycle error exc", e);
                        timeUtil.sleepSeconds(5);
                    }
                }
            } catch (Exception e) {
                log.error("ytchat run error", e);
            }
        }
    }

    private int readMessages() {
        if (!canRead) return Integer.MAX_VALUE;
        int pollingInterval;
        UserGoogle userGoogle = ((UserGoogle) apiUser);
        HttpsURLConnection connection = urlConnectionPrepare.getConnection(
                "https://www.googleapis.com/youtube/v3/liveChat/messages" +
                        "?" +
                        "liveChatId=" + userGoogle.getLiveChatId() + "&" +
                        "part=id,snippet,authorDetails" + "&" +
                        "key=" + googleclientId);
        ytChatUtil.addConnectionAuth(connection, accessToken);

        JsonObject jObj = ioExtractor.extractJsonObject(connection);

        //null pointer
        pollingInterval = jObj.getAsJsonPrimitive("pollingIntervalMillis").getAsInt() / 1000;

        JsonPrimitive offlineAt = jObj.getAsJsonPrimitive("offlineAt");
        if (offlineAt != null) return -1;

        JsonArray jArr = jObj.getAsJsonArray("items");
        for (JsonElement el : jArr) {
            lastUpdateTimeInMillis = System.currentTimeMillis();

            Map<String, String> map = ytChatUtil.extractMessageData(el.getAsJsonObject());
            if (map.size() == 1) {
                if (map.get("quota").equalsIgnoreCase("mid")) {
                    ZonedDateTime america = ZonedDateTime.now(ZoneId.of("America/Tijuana"));
                    ZonedDateTime americaPlusDayTR = america.plusDays(1).truncatedTo(ChronoUnit.DAYS);
                    long secBetween = ChronoUnit.SECONDS.between(america, americaPlusDayTR);
                    secBetween = Math.abs(secBetween) + 10;
                    log.warn("SLEEP UNTIL PACIFIC MIDNIGHT {}", secBetween);
                    if (sleepSecCauseQuota > 80000) sleepSecCauseQuota = 100;
                    return (int) secBetween;
                } else {
                    log.info("YT ERR. return 10 min");
                    return 600;
                }
            }
            String messageId = map.get("id");
            if (!messages.contains(messageId)) {
                messages.add(messageId);

                if (messagesUtil.getMessageOptionalBySubId(messageId).isPresent()) continue;
                if (isMessageFromSite(map.get("text"), source)) continue;

//                if (map.get("text").startsWith("!")) {
//                    sendToServer(sovaowlRu + " [0] " + "Команды поддерживаются только на сайте. " + sovaowlRuHttps);
//                }

                Message message = prepareMessage(map, messageId);

                MessageValidationStatus messageValidationStatus = messageValidator.validateMessage(message);
                if (messageValidationStatus == MessageValidationStatus.SPAM)
                    banUser(message.getNick(), SPAM, message);
                if (messageValidationStatus != MessageValidationStatus.OK)
                    continue;

                if (nickNames != null && !nickNames.contains(map.get("displayName"))) {
                    nickNames.add(map.get("displayName"));
                    sendInviteMessage(map.get("displayName"));
                }

                messageApiDeliver.sendMessageToAllApiChats(message, topicTarget, this, null, null);
                template.convertAndSend("/topic/" + topicTarget, message);
            }
        }

        // lastUpdateTime + 5 min
        if (lastUpdateTimeInMillis + 1000 * 60 * 5 < System.currentTimeMillis()) {
            boolean correct = videoAndChatIdCorrect();
            if (!correct) return -1;
            lastUpdateTimeInMillis = System.currentTimeMillis();
        }
        return Math.max(pollingInterval, 15);
    }

    @NotNull
    private Message prepareMessage(Map<String, String> map, String messageId) {
        Message message = new Message();
        Stream stream = streamUtil.getStreamByUserNickname(apiUser.getUser().getNickname());
        message.setStreamId(stream.getId());

        message.setNick(map.get("displayName"));
        message.setText(map.get("text"));
        message.setType("message");
        message.setIcons("youtube.png");
        message.setPremText(false);
        message.setStyle(styleUtil.getWhiteStyle());
        message.setTime(LocalDateTime.now());
        message.setSource(source);

        message.setIssuerId(map.get("channelId"));
        message.setMessageSubId(messageId);
        return message;
    }

    @Override
    public void disconnect() {
        work = false;
        messages.clear();
    }
}
