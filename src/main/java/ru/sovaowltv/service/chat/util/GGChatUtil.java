package ru.sovaowltv.service.chat.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersGGRepository;
import ru.sovaowltv.service.chat.ChatCreator;
import ru.sovaowltv.service.chat.realization.ApiForChat;
import ru.sovaowltv.service.chat.realization.ApiWebsiteChats;
import ru.sovaowltv.service.chat.realization.GGChat;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GGChatUtil {
    private final UsersGGRepository usersGGRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;
    private final ApiChatsUtil apiChatsUtil;

    private final ApiWebsiteChats apiWebsiteChats;
    private final ChatCreator chatCreator;

    private final Map<String, List<GGChat>> ggSubToListOfConnections = new HashMap<>();

    public void addGG(GGChat apiForChat) {
        String sub = apiForChat.getApiUser().getSub();
        ggSubToListOfConnections.putIfAbsent(sub, new ArrayList<>());
        List<GGChat> ggChatsList = ggSubToListOfConnections.get(sub);
        ggChatsList.add(apiForChat);
    }

    public void deleteStreamChatGOODGAMEConnection(Stream stream) {
        try {
            if (stream.getUser().getUserGG() == null) return;
            List<ApiForChat> list = apiWebsiteChats.getChatByChannel(stream.getUser().getNickname());
            if (list != null)
                apiChatsUtil.disconnectChats(list, GGChat.class);
            List<GGChat> apiForChats = ggSubToListOfConnections.get(stream.getUser().getUserGG().getSub());
            if (apiForChats != null) apiForChats.forEach(ApiForChat::disconnect);
            ggSubToListOfConnections.remove(stream.getUser().getUserGG().getSub());
        } catch (Exception e) {
            log.error("cant delete stream chat", e);
        }
    }

    private Optional<ApiForChat> getGGChatByChannelAndSub(String channelToConnect, String subOfMessageSender) {
        List<GGChat> apiForChats = ggSubToListOfConnections.get(subOfMessageSender);
        if (apiForChats != null) {
            for (ApiForChat apiForChat : apiForChats) {
                if (apiChatsUtil.channelsEquals(channelToConnect, apiForChat)) {
                    return Optional.of(apiForChat);
                }
            }
        }
        Optional<UserGG> userGG = usersGGRepository.findBySub(subOfMessageSender);
        return userGG.flatMap(gg -> chatCreator.createGGChat(channelToConnect, subOfMessageSender, apiForChats, gg, ggSubToListOfConnections));
    }

    public Optional<ApiForChat> getGGChatOwner(String webSiteChannel) {
        return apiChatsUtil.getChatOwner(webSiteChannel, GGChat.class);
    }

    public void sendMessageToGG(Message message, String webSiteChannel, Object apiChatForExclude, User userMessageSender) {
        List<ApiForChat> chatList = apiWebsiteChats.getChatByChannel(webSiteChannel);

        if (userMessageSender != null && userMessageSender.getUserGG() != null) {
            String sub = userMessageSender.getUserGG().getSub();
            String channelToConnect;
            User userByNickname = null;
            try {
                userByNickname = usersRepositoryHandler.getUserByNickname(webSiteChannel);
                if (userByNickname == null
                        || userByNickname.getUserGG() == null
                        || userByNickname.getUserGG().getNick() == null)
                    return;
                channelToConnect = userByNickname.getUserGG().getNick();
            } finally {
                usersRepositoryHandler.free(userByNickname);
            }
            if (chatList != null) {
                List<ApiForChat> collect = chatList
                        .stream()
                        .filter(apiForChat -> apiForChat instanceof GGChat)
                        .filter(apiForChat -> apiForChat != apiChatForExclude)
                        .filter(apiForChat -> channelToConnect.equalsIgnoreCase(apiForChat.getChannelToConnect()))
                        .collect(Collectors.toList());

                collect.forEach(apiForChat -> {
                    Optional<ApiForChat> ggChatByChannelAndName = getGGChatByChannelAndSub(channelToConnect, sub);
                    //с сайта хозяин чата
                    //с сайта есть твич.
                    if (ggChatByChannelAndName.isPresent()) {
                        ggChatByChannelAndName.get().sendMessage(message, true);
                    } else {
                        apiForChat.sendMessage(message, false);
                    }
                });
            }
        } else {
            Optional<ApiForChat> ggChatOwner = getGGChatOwner(webSiteChannel);
            if (ggChatOwner.isPresent() && apiChatForExclude != ggChatOwner.get()) {

                //с сайта, нет твича.
                //с твича.
                ggChatOwner.get().sendMessage(message, false);
            }
        }
    }
}