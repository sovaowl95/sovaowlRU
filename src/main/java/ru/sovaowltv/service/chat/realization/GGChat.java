package ru.sovaowltv.service.chat.realization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import ru.sovaowltv.model.apiauth.ApiUser;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.token.GGTokenHandler;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.messages.MessageValidationStatus;
import ru.sovaowltv.service.smiles.GGSmiles;
import ru.sovaowltv.service.user.UserGGUtil;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@ClientEndpoint
@PropertySource("classpath:constants.yml")
@Slf4j
public class GGChat extends ApiForChat {
    @Autowired
    private GGTokenHandler ggTokenHandler;
    @Autowired
    private UserGGUtil userGGUtil;
    @Autowired
    private DataExtractor dataExtractor;
    @Autowired
    private GGSmiles ggSmilesUtil;

    @Value("${gg}")
    private String source;

    private HashSet<String> nickNames;

    public GGChat(String topicTarget,
                  String ip, String port, String clientId, String clientSecret,
                  String accessToken,
                  String twitchChannel, ApiUser apiUser,
                  boolean canRead, boolean canWrite) {
        super(topicTarget,
                ip, port, clientId, clientSecret,
                accessToken,
                twitchChannel.toLowerCase(), apiUser,
                canRead, canWrite);
        if (canRead) nickNames = new HashSet<>();
    }


    @Override
    public boolean refreshToken() {
        if (ggTokenHandler.refresh((UserGG) apiUser)) {
            Optional<UserGG> byId = userGGUtil.getUserGGBySub(String.valueOf(apiUser.getSub()));
            apiUser = byId.orElseThrow(() -> new RuntimeException("User not found"));
            accessToken = apiUser.getAccessToken();
            return true;
        }
        return false;
    }

    @Override
    protected String getConnectString() {
        return ip + "/";
    }

    @Override
    @OnOpen
    public void onOpen(Session session) throws IOException {
        session.getBasicRemote().sendText(new Gson().toJson(getAuthString()));
        session.getBasicRemote().sendText(new Gson().toJson(getChannelJoinString()));
    }


    private Map<String, Object> getChannelJoinString() {
        Map<String, Object> map = new HashMap<>();
        HashMap<String, Object> data = new HashMap<>();
        UserGG apiUser = (UserGG) this.apiUser;

        data.put("channel_id", apiUser.getChannelId());
        data.put("hidden", false);

        map.put("type", "join");
        map.put("data", data);
        return map;
    }


    private Map<String, Object> getAuthString() {
        Map<String, Object> map = new HashMap<>();
        HashMap<String, Object> data = new HashMap<>();
        UserGG apiUser = (UserGG) this.apiUser;

        data.put("user_id", apiUser.getSub());
        data.put("token", apiUser.getChatToken());

        map.put("type", "auth");
        map.put("data", data);
        return map;
    }

    @Override
    @OnMessage
    public void onMessage(Session session, String ggMessage) {
        try {
            log.info("<<< " + apiUser.getNick() + " <<< #" + channelToConnect + " <<< " + ggMessage);
            JsonObject asJsonObject = dataExtractor.extractJsonFromString(ggMessage);
            String type = dataExtractor.getPrimitiveAsStringFromJson(asJsonObject, "type");
            if ("message".equals(type)) {
                if (!canRead) return;
                JsonObject jData = asJsonObject.getAsJsonObject("data");
                JsonElement userIdEl = jData.get("user_id");
                JsonElement userNameEl = jData.get("user_name");
                JsonElement textEl = jData.get("text");
                JsonElement messageIdEl = jData.get("message_id");

                String userId = userIdEl.getAsString();
                String userName = userNameEl.getAsString();
                String text = textEl.getAsString();
                String messageId = messageIdEl.getAsString();

//                if (text.startsWith("!"))
//                    sendToServer(prepareMessage(sovaowlRu + " [0] " + "Команды поддерживаются только на сайте. " + sovaowlRuHttps));

                if (isMessageFromSite(text, source))
                    return;

                Message message = createMessage(userId, userName, text, messageId);
                if (message == null) return;

                MessageValidationStatus messageValidationStatus = messageValidator.validateMessage(message);
                if (messageValidationStatus == MessageValidationStatus.SPAM) banUser(userName, "SPAM", message);
                if (messageValidationStatus != MessageValidationStatus.OK) return;

                if (nickNames != null && !nickNames.contains(userName)) {
                    nickNames.add(userName);
                    sendInviteMessage(userName);
                }
                messageDeliver.sendMessageToAllApiChats(message, topicTarget, this, null, null);
                template.convertAndSend("/topic/" + topicTarget, message);
            } else if ("channel_counters".equals(type)) {
            }
        } catch (Exception e) {
            log.error("GG - CHAT - ERROR", e);
        }
    }

    private Message createMessage(String userId, String userName, String text, String messageId) {
        Message message = new Message();
        Stream stream = streamUtil.getStreamByUserNickname(apiUser.getUser().getNickname());
        message.setStreamId(stream.getId());

        Optional<UserGG> bySub = userGGUtil.getUserGGBySub(userId);
        if (bySub.isPresent()) {
            User userAuthor = bySub.get().getUser();
            message.setMessageSubId(messageId);
            if (!streamModerationUtil.canChatInChannelBan(userAuthor, stream)) {
                banUser(userName, "banned by " + sovaowlRu, message);
                return null;
            } else if (!streamModerationUtil.canChatInChannelTimeout(userAuthor, stream)) {
                timeoutUser(userName, apiTimeouts.getTimeForChannelAndUser(stream, userAuthor), "timeout by " + sovaowlRu, message);
                return null;
            }
        }

        message.setNick(userName);
        message.setText(text);
        message.setType("message");
        message.setIcons("gg.svg");
        message.setPremText(false);
        message.setStyle(styleUtil.getWhiteStyle());
        message.setTime(LocalDateTime.now());
        message.setSource(source);
        message.setGgSmilesInfo(streamModerationUtil.generateSmilesInfo(text, ggSmilesUtil));
        message.setIssuerId(userId);
        message.setMessageSubId(messageId);
        return message;
    }

    @Override
    public void sendMessage(Message message, boolean fromSelf) {
        String text = message.getText();
        if (message.getOriginalMessage() != null) text = message.getOriginalMessage();

        String nick = "";
        if (!fromSelf || (source.equalsIgnoreCase(message.getSource()) && channelToConnect.equalsIgnoreCase(message.getNick()))) {
            nick = message.getNick();
        }

        sendToServer(prepareMessage(sovaowlRu + " [" + message.getId() + "] " + nick + " : " + text));
    }

    @Override
    public void sendInviteMessage(String nickName) {
        sendToServer(prepareMessage(sovaowlRu + " [0] " + "Привет, " + nickName + " , заходи на сайт " + sovaowlRuHttps + " . Мне нужна помощь в тестировании!)"));
    }

    @Override
    public void timeoutUser(String nickName, String time, String reason, Message message) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("channel_id", ((UserGG) apiUser).getChannelId());
        data.put("ban_channel", ((UserGG) apiUser).getChannelId());
        data.put("comment", sovaowlRu);
        data.put("duration", Long.parseLong(time));
        data.put("reason", sovaowlRu);
        data.put("user_id", message.getIssuerId());
        data.put("show_ban", true);

        map.put("type", "ban");
        map.put("data", data);

        sendToServer(new Gson().toJson(map));
    }

    @Override
    public void unTimeoutUser(String nickName, Message message) {
        timeoutUser(nickName, "1", "unban", message);
    }

    @Override
    public void unBanUser(String nickName, Message message) {
        timeoutUser(nickName, "1", "unban", message);
    }

    @Override
    public void banUser(String nickName, String reason, Message message) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("userId", Long.parseLong(message.getIssuerId()));
        data.put("hidden", false);
        data.put("deleteMessage", false);
        data.put("duration", 0);
        data.put("type", 2);
        data.put("roomId", ((UserGG) apiUser).getChannelId());
        data.put("reason", reason);
        data.put("comment", sovaowlRu);


        map.put("type", "ban2");
        map.put("data", data);

        String text = new Gson().toJson(map);
        sendToServer(text);
    }

    @Override
    public void deleteMessage(Message message) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("channel_id", ((UserGG) apiUser).getChannelId());
        data.put("message_id", message.getMessageSubId());

        map.put("type", "remove_message");
        map.put("data", data);
        sendToServer(new Gson().toJson(map));
    }

    @Override
    public void purgeUser(String nickName, Message message) {
        deleteMessage(message);
    }

    private String prepareMessage(String text) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("channel_id", ((UserGG) apiUser).getChannelId());
        data.put("text", text);
        data.put("hideIcon", false);
        data.put("mobile", false);

        map.put("type", "send_message");
        map.put("data", data);
        return new Gson().toJson(map);
    }
}
