package ru.sovaowltv.service.chat.realization;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import ru.sovaowltv.model.apiauth.ApiUser;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.api.token.TwitchTokenHandler;
import ru.sovaowltv.service.chat.util.TwitchChatUtil;
import ru.sovaowltv.service.messages.MessageValidationStatus;
import ru.sovaowltv.service.user.UserTwitchUtil;
import ru.sovaowltv.service.user.UserUtil;

import javax.annotation.Nullable;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

@ClientEndpoint
@Getter
@Setter
@PropertySource("classpath:constants.yml")
@Slf4j
public class TwitchChat extends ApiForChat {
    @Autowired
    private UserUtil userUtil;
    @Autowired
    private UserTwitchUtil userTwitchUtil;
    @Autowired
    private TwitchChatUtil twitchChatUtil;
    @Autowired
    private TwitchTokenHandler twitchTokenHandler;

    @Value("${twitch}")
    private String source;

    private HashSet<String> nickNames;
    private static final String PRIVMSG_SHARP = "PRIVMSG #";

    public TwitchChat(String topicTarget,
                      String ip, String port, String clientId, String clientSecret,
                      String accessToken,
                      String twitchChannel, ApiUser apiUser,
                      boolean canRead, boolean canWrite) {
        super(topicTarget,
                ip, port, clientId, clientSecret,
                accessToken,
                twitchChannel.toLowerCase(), apiUser,
                canRead, canWrite
        );
        if (canRead) nickNames = new HashSet<>();
    }

    @Override
    public boolean refreshToken() {
        if (twitchTokenHandler.refresh((UserTwitch) apiUser)) {
            Optional<UserTwitch> byId = userTwitchUtil.getUserTwitchById(apiUser.getId());
            apiUser = byId.orElseThrow(() -> new RuntimeException("User not found"));
            accessToken = apiUser.getAccessToken();
            return true;
        }
        return false;
    }

    @Override
    protected String getConnectString() {
        return ip + ":" + port + "/";
    }

    @Override
    @OnOpen
    public void onOpen(Session session) throws IOException {
        session.getBasicRemote().sendText("PASS oauth:" + accessToken + "\r\n");
        session.getBasicRemote().sendText("NICK " + apiUser.getNick().toLowerCase() + "\r\n");
        session.getBasicRemote().sendText("JOIN #" + channelToConnect + "\r\n");
        session.getBasicRemote().sendText("CAP REQ :twitch.tv/membership" + "\r\n");
        session.getBasicRemote().sendText("CAP REQ :twitch.tv/tags" + "\r\n");
        session.getBasicRemote().sendText("CAP REQ :twitch.tv/commands" + "\r\n");
        session.getBasicRemote().sendText("CAP REQ :twitch.tv/membership" + "\r\n");
    }

    //todo: privmsg..whisper?
    @Override
    @OnMessage
    public void onMessage(Session session, String twitchMessage) {
        try {
            if (twitchMessage.startsWith("PING :tmi.twitch.tv")) {
                sendPongToServer("PONG :tmi.twitch.tv" + "\r\n");
                return;
            }

            if (twitchMessage.contains(":tmi.twitch.tv NOTICE * :Login authentication failed")) {
                log.error("Twitch chat Login auth failed " + apiUser.getNick());
                userTwitchUtil.setCorrupted((UserTwitch) apiUser);
                work = false;
                return;
            }

            if (!canRead) return;
            log.info("<<< " + apiUser.getNick() + " <<< #" + channelToConnect + " <<< " + twitchMessage);

            if (twitchMessage.contains(".tmi.twitch.tv " + PRIVMSG_SHARP + channelToConnect)) {
                String badges = twitchMessage.split(" :", 2)[0].trim();
                String mainPart = twitchMessage.split(" :", 2)[1].trim();

                String[] parts = badges.split(";");

                HashMap<String, String> map = getAllKeys(parts);

                String nick = map.get("display-name");
                String messageParsed = mainPart.split(":", 2)[1].trim();
                String twitchLogin = mainPart.substring(0, mainPart.indexOf('!'));

                if (!isEnglishNick(nick)) {
                    nick = nick + "(" + twitchLogin + ")";
                }

//            if (messageParsed.startsWith("!") && !anotherChat)
//                sendToServer("PRIVMSG #" + channelToConnect + " :" + "Команды поддерживаются только на сайте. " + sovaowlRu + "\r\n");
                if (isMessageFromSite(messageParsed, source))
                    return;

                Message message = createMessage(map, nick, messageParsed);
                if (message == null)
                    return;

                MessageValidationStatus messageValidationStatus = messageValidator.validateMessage(message);
                if (messageValidationStatus == MessageValidationStatus.SPAM) banUser(nick, "SPAM", message);
                if (messageValidationStatus != MessageValidationStatus.OK) return;

                if (nickNames != null && !nickNames.contains(nick)) {
                    nickNames.add(nick);
                    sendInviteMessage(nick);
                }
                messageDeliver.sendMessageToAllApiChats(message, topicTarget, this, null);
                template.convertAndSend("/topic/" + topicTarget, message);
            }
        } catch (Exception e) {
            log.error("TWITCH - CHAT - ERROR", e);
        }
    }

    @NotNull
    private HashMap<String, String> getAllKeys(String[] parts) {
        HashMap<String, String> map = new HashMap<>();
        for (String part : parts) {
            String key = part.split("=")[0];
            String value;
            try {
                value = part.split("=")[1];
            } catch (Exception e) {
                value = null;
            }
            map.putIfAbsent(key, value);
        }
        return map;
    }

    private boolean isEnglishNick(String nick) {
        for (char c : nick.toCharArray()) {
            if ((!Character.isDigit(c)) && (c < 'A' || c > 'z')) {
                return false;
            }
        }
        return true;
    }


    private Message createMessage(HashMap<String, String> map, String nick, String messageParsed) {
        Message message = new Message();
        User user = apiUser.getUser();
        Stream stream = streamUtil.getStreamByUserNickname(user.getNickname());
        message.setStreamId(stream.getId());

        String userId = map.get("user-id");
        Optional<UserTwitch> bySub = userTwitchUtil.getUserTwitchBySub(userId);
        if (bySub.isPresent()) {
            User userAuthor = bySub.get().getUser();
            if (!streamModerationUtil.canChatInChannelBan(userAuthor, stream)) {
                banUser(nick, "banned by " + sovaowlRu, null);
                return null;
            } else if (!streamModerationUtil.canChatInChannelTimeout(userAuthor, stream)) {
                timeoutUser(nick, apiTimeouts.getTimeForChannelAndUser(stream, userAuthor), "timeout by " + sovaowlRu, null);
                return null;
            }
        }

        message.setNick(nick);
        message.setText(messageParsed);
        message.setType("message");
        message.setIcons("twitch.ico");
        message.setPremText(false);
        message.setStyle(styleUtil.getWhiteStyle());
        message.setTime(LocalDateTime.now());
        message.setSource(source);
        message.setTwitchSmilesInfo(map.get("emotes"));
        message.setMessageSubId(map.get("id"));
        message.setIssuerId(map.get("user-id"));

        if (map.containsKey("msg-id") && map.get("msg-id").equals("highlighted-message")) {
            message.setHighlighted(true);
        }
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
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + sovaowlRu + " [" + message.getId() + "] " + nick + " : " + text + "\r\n");
    }

    @Override
    public void sendInviteMessage(String nickName) {
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + "Привет, " + nickName + " , заходи на сайт " + sovaowlRu + " . Мне нужна помощь в тестировании!) Hello, go to the site " + sovaowlRu + " I need help testing" + "\r\n");
    }

    @Override
    public void timeoutUser(String nickName, String time, String reason, @Nullable Message message) {
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + "/timeout " + nickName + " " + time + " " + reason + "\r\n");
    }

    @Override
    public void unTimeoutUser(String nick, Message message) {
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + "/untimeout " + nick + "\r\n");
    }

    @Override
    public void banUser(String nickName, String reason, @Nullable Message message) {
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + "/ban " + nickName + " " + reason + "\r\n");
    }

    @Override
    public void unBanUser(String nickName, Message message) {
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + "/unban " + nickName + "\r\n");
    }

    @Override
    public void deleteMessage(Message message) {
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + "/delete " + message.getMessageSubId());
    }

    @Override
    public void purgeUser(String nickName, Message message) {
        sendToServer(PRIVMSG_SHARP + channelToConnect + " :" + "/timeout " + nickName + " 1 " + "\r\n");
    }
}
