package ru.sovaowltv.service.chat.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersGoogleRepository;
import ru.sovaowltv.service.chat.ChatCreator;
import ru.sovaowltv.service.chat.realization.ApiForChat;
import ru.sovaowltv.service.chat.realization.ApiWebsiteChats;
import ru.sovaowltv.service.chat.realization.YTChat;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.net.ssl.HttpsURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class YTChatUtil {
    private final UsersGoogleRepository usersGoogleRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;
    private final ApiChatsUtil apiChatsUtil;

    private final ApiWebsiteChats apiWebsiteChats;
    private final ChatCreator chatCreator;

    private final Map<String, List<YTChat>> ytSubToListOfConnections = new HashMap<>();

    public void addYT(YTChat apiForChat) {
        String sub = apiForChat.getApiUser().getSub();
        ytSubToListOfConnections.putIfAbsent(sub, new ArrayList<>());
        List<YTChat> ytChatsList = ytSubToListOfConnections.get(sub);
        ytChatsList.add(apiForChat);
    }

    public void deleteStreamChatYOUTUBEConnection(Stream stream) {
        try {
            if (stream.getUser().getUserGoogle() == null) return;
            List<ApiForChat> list = apiWebsiteChats.getChatByChannel(stream.getUser().getNickname());
            if (list != null)
                apiChatsUtil.disconnectChats(list, YTChat.class);
            List<YTChat> apiForChats = ytSubToListOfConnections.get(stream.getUser().getUserGoogle().getSub());
            if (apiForChats != null) apiForChats.forEach(ApiForChat::disconnect);
            ytSubToListOfConnections.remove(stream.getUser().getUserGoogle().getSub());
        } catch (Exception e) {
            log.error("cant delete stream chat", e);
        }
    }

    private Optional<ApiForChat> getYTChatByChannelAndSub(String channelToConnect, String subOfMessageSender) {
        List<YTChat> apiForChats = ytSubToListOfConnections.get(subOfMessageSender);
        if (apiForChats != null) {
            for (ApiForChat apiForChat : apiForChats) {
                if (apiChatsUtil.channelsEquals(channelToConnect, apiForChat)) {
                    return Optional.of(apiForChat);
                }
            }
        }
        Optional<UserGoogle> userGoogle = usersGoogleRepository.findBySub(subOfMessageSender);
        return userGoogle.flatMap(google -> chatCreator.createYTChat(channelToConnect, subOfMessageSender, apiForChats, google, ytSubToListOfConnections));
    }

    public Optional<ApiForChat> getYTChatOwner(String webSiteChannel) {
        return apiChatsUtil.getChatOwner(webSiteChannel, YTChat.class);
    }

    public String prepareBodySendToServer(String text, UserGoogle userGoogle) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> snippet = new HashMap<>();
        Map<String, Object> textMessageDetails = new HashMap<>();

        textMessageDetails.put("messageText", text);

        snippet.put("liveChatId", userGoogle.getLiveChatId());
        snippet.put("type", "textMessageEvent");
        snippet.put("textMessageDetails", textMessageDetails);

        map.put("snippet", snippet);

        return new Gson().toJson(map);
    }

    public String prepareTimeoutUserBody(String time, Message message, UserGoogle userGoogle) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> snippet = new HashMap<>();
        Map<String, Object> bannedUserDetails = new HashMap<>();

        bannedUserDetails.put("channelId", message.getIssuerId());

        snippet.put("liveChatId", userGoogle.getLiveChatId());
        snippet.put("type", "temporary");
        snippet.put("banDurationSeconds", Long.parseLong(time));
        snippet.put("bannedUserDetails", bannedUserDetails);

        map.put("snippet", snippet);
        return new Gson().toJson(map);
    }


    public String prepareBanBody(Message message, UserGoogle userGoogle) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> snippet = new HashMap<>();
        Map<String, Object> bannedUserDetails = new HashMap<>();

        bannedUserDetails.put("channelId", message.getIssuerId());

        snippet.put("liveChatId", userGoogle.getLiveChatId());
        snippet.put("type", "permanent");
        snippet.put("bannedUserDetails", bannedUserDetails);

        map.put("snippet", snippet);
        return new Gson().toJson(map);
    }

    public void addConnectionAuth(HttpsURLConnection connection, String accessToken) {
        connection.addRequestProperty("Authorization", "Bearer " + accessToken);
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Content-Type", "application/json");
    }

    public Map<String, String> extractMessageData(JsonObject jObj) {
        if (jObj.getAsJsonObject("error") != null) {
            log.error("youtube api request error. {}", jObj.toString());
            Map<String, String> map = new HashMap<>();
            if (jObj.toString().contains("The quota will be reset at midnight Pacific Time (PT)")
                    || jObj.toString().contains("The request cannot be completed because you have exceeded your"))
                map.put("quota", "mid");
            return map;
        } else if (jObj.getAsJsonPrimitive("kind").getAsString().equalsIgnoreCase("youtube#liveChatMessage")) {
            Map<String, String> map = new HashMap<>();
            JsonObject jsonObject;

            jsonObject = jObj;
            map.put("id", jsonObject.getAsJsonPrimitive("id").getAsString());

            jsonObject = jObj.getAsJsonObject("snippet");
            map.put("text", jsonObject.getAsJsonObject("textMessageDetails").getAsJsonPrimitive("messageText").getAsString());

            jsonObject = jObj.getAsJsonObject("authorDetails");
            map.put("displayName", jsonObject.getAsJsonPrimitive("displayName").getAsString());
            map.put("channelId", jsonObject.getAsJsonPrimitive("channelId").getAsString());
            return map;
        } else {
            Map<String, String> map = new HashMap<>();
            JsonObject jsonObject;

            jsonObject = jObj;
            map.put("id", jsonObject.getAsJsonPrimitive("id").getAsString());

            jsonObject = jObj.getAsJsonObject("snippet").getAsJsonObject("bannedUserDetails");
            map.put("displayName", jsonObject.getAsJsonPrimitive("displayName").getAsString());
            map.put("channelId", jsonObject.getAsJsonPrimitive("channelId").getAsString());
            return map;
        }
    }

    public void sendMessageToYT(Message message, String webSiteChannel, Object apiChatForExclude, User userMessageSender) {
        List<ApiForChat> chatList = apiWebsiteChats.getChatByChannel(webSiteChannel);

        if (userMessageSender != null && userMessageSender.getUserGoogle() != null) {
            String sub = userMessageSender.getUserGoogle().getSub();
            String nick;
            User userByNickname = null;
            try {
                userByNickname = usersRepositoryHandler.getUserByNickname(webSiteChannel);
                if (userByNickname == null
                        || userByNickname.getUserGoogle() == null
                        || userByNickname.getUserGoogle().getNick() == null)
                    return;
                nick = userByNickname.getUserGoogle().getNick();
            } catch (NullPointerException e) {
                log.debug("can't find channel to connect");
                return;
            } finally {
                usersRepositoryHandler.free(userByNickname);
            }
            String channelToConnect = nick == null ? webSiteChannel : nick;
            if (chatList != null) {
                List<ApiForChat> collect = chatList
                        .stream()
                        .filter(apiForChat -> apiForChat instanceof YTChat)
                        .filter(apiForChat -> apiForChat != apiChatForExclude)
                        .filter(apiForChat -> apiForChat.getChannelToConnect().equalsIgnoreCase(channelToConnect))
                        .collect(Collectors.toList());

                collect.forEach(apiForChat -> {
                    Optional<ApiForChat> ytChatByChannelAndName = getYTChatByChannelAndSub(channelToConnect, sub);
                    //с сайта хозяин чата
                    //с сайта есть твич.
                    if (ytChatByChannelAndName.isPresent()) {
                        ytChatByChannelAndName.get().sendMessage(message, true);
                    } else {
                        apiForChat.sendMessage(message, false);
                    }
                });
            }
        } else {
            Optional<ApiForChat> ytChatOwner = getYTChatOwner(webSiteChannel);
            if (ytChatOwner.isPresent() && apiChatForExclude != ytChatOwner.get()) {
                //с сайта, нет твича.
                //с твича.
                ytChatOwner.get().sendMessage(message, false);
            }
        }
    }
}