package ru.sovaowltv.service.chat.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;
import ru.sovaowltv.service.chat.ChatCreator;
import ru.sovaowltv.service.chat.realization.ApiForChat;
import ru.sovaowltv.service.chat.realization.ApiWebsiteChats;
import ru.sovaowltv.service.chat.realization.TwitchChat;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwitchChatUtil {
    private final UsersTwitchRepository usersTwitchRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;
    private final ApiChatsUtil apiChatsUtil;

    private final ApiWebsiteChats apiWebsiteChats;
    private final ChatCreator chatCreator;

    private final Map<String, List<TwitchChat>> twitchSubToListOfConnections = new HashMap<>();

    public void addTwitch(TwitchChat apiForChat) {
        String sub = apiForChat.getApiUser().getSub();
        twitchSubToListOfConnections.putIfAbsent(sub, new ArrayList<>());
        List<TwitchChat> twitchChatList = twitchSubToListOfConnections.get(sub);
        twitchChatList.add(apiForChat);
    }

    public void deleteStreamChatTWITCHConnection(Stream stream) {
        try {
            if (stream.getUser().getUserTwitch() == null) return;
            List<ApiForChat> list = apiWebsiteChats.getChatByChannel(stream.getUser().getNickname());
            if (list != null)
                apiChatsUtil.disconnectChats(list, TwitchChat.class);
            List<TwitchChat> apiForChats = twitchSubToListOfConnections.get(stream.getUser().getUserTwitch().getSub());
            if (apiForChats != null) apiForChats.forEach(ApiForChat::disconnect);
            twitchSubToListOfConnections.remove(stream.getUser().getUserTwitch().getSub());
        } catch (Exception e) {
            log.error("cant delete stream chat", e);
        }
    }

    private Optional<ApiForChat> getTwitchChatByChannelAndSub(String channelToConnect, String subOfMessageSender) {
        List<TwitchChat> apiForChats = twitchSubToListOfConnections.get(subOfMessageSender);
        if (apiForChats != null) {
            for (ApiForChat apiForChat : apiForChats) {
                if (apiChatsUtil.channelsEquals(channelToConnect, apiForChat)) {
                    return Optional.of(apiForChat);
                }
            }
        }
        Optional<UserTwitch> userTwitch = usersTwitchRepository.findBySub(subOfMessageSender);
        return userTwitch.flatMap(twitch -> chatCreator.createTwitchChat(channelToConnect, subOfMessageSender, apiForChats, twitch, twitchSubToListOfConnections));
    }

    public Optional<ApiForChat> getTwitchChatOwner(String webSiteChannel) {
        return apiChatsUtil.getChatsByOwner(webSiteChannel, TwitchChat.class);
    }

    public void sendMessageToTwitch(Message message, String webSiteChannel, Object apiChatForExclude, User userMessageSender) {
        List<ApiForChat> chatList = apiWebsiteChats.getChatByChannel(webSiteChannel);

        if (userMessageSender != null && userMessageSender.getUserTwitch() != null) {
            String sub = userMessageSender.getUserTwitch().getSub();
            String channelToConnect;
            User userByNickname = null;
            try {
                userByNickname = usersRepositoryHandler.getUserByNickname(webSiteChannel);
                if (userByNickname == null
                        || userByNickname.getUserTwitch() == null
                        || userByNickname.getUserTwitch().getNick() == null)
                    return;
                channelToConnect = userByNickname.getUserTwitch().getNick();
            } catch (NullPointerException e) {
                log.debug("can't find channel to connect");
                return;
            } finally {
                usersRepositoryHandler.free(userByNickname);
            }
            if (chatList != null) {
                List<ApiForChat> collect = chatList
                        .stream()
                        .filter(apiForChat -> apiForChat instanceof TwitchChat)
                        .filter(apiForChat -> apiForChat != apiChatForExclude)
                        .filter(apiForChat -> channelToConnect.equalsIgnoreCase(apiForChat.getChannelToConnect()))
                        .collect(Collectors.toList());

                collect.forEach(apiForChat -> {
                    Optional<ApiForChat> twitchChatByChannelAndName = getTwitchChatByChannelAndSub(channelToConnect, sub);
                    //с сайта хозяин чата
                    //с сайта есть твич.
                    if (twitchChatByChannelAndName.isPresent()) {
                        twitchChatByChannelAndName.get().sendMessage(message, true);
                    } else {
                        apiForChat.sendMessage(message, false);
                    }
                });
            }
        } else {
            Optional<ApiForChat> twitchChatOwner = getTwitchChatOwner(webSiteChannel);
            if (twitchChatOwner.isPresent() && apiChatForExclude != twitchChatOwner.get()) {
                //с сайта, нет твича.
                //с твича.
                twitchChatOwner.get().sendMessage(message, false);
            }
        }
    }
}