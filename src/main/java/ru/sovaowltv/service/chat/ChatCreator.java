package ru.sovaowltv.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.service.chat.realization.ApiForChat;
import ru.sovaowltv.service.chat.realization.GGChat;
import ru.sovaowltv.service.chat.realization.TwitchChat;
import ru.sovaowltv.service.chat.realization.YTChat;
import ru.sovaowltv.service.factorys.gg.GGChatFactory;
import ru.sovaowltv.service.factorys.google.YTChatFactory;
import ru.sovaowltv.service.factorys.twitch.TwitchChatFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatCreator {
    private final TwitchChatFactory twitchChatFactory;
    private final GGChatFactory ggChatFactory;
    private final YTChatFactory ytChatFactory;

    public Optional<ApiForChat> createTwitchChat(String channelToConnect, String subOfMessageSender,
                                                 List<TwitchChat> apiForChats,
                                                 UserTwitch userTwitch,
                                                 Map<String, List<TwitchChat>> twitchSubToListOfConnections) {
        TwitchChat twitchChat = twitchChatFactory.factoryTwitch(userTwitch.getUser(), userTwitch, channelToConnect);
        if (apiForChats == null) {
            apiForChats = new ArrayList<>();
            twitchSubToListOfConnections.put(subOfMessageSender, apiForChats);
        }
        apiForChats.add(twitchChat);
        twitchChat.start();
        return Optional.of(twitchChat);
    }

    public Optional<ApiForChat> createGGChat(String channelToConnect,
                                             String subOfMessageSender,
                                             List<GGChat> apiForChats,
                                             UserGG userGG,
                                             Map<String, List<GGChat>> ggSubToListOfConnections) {
        GGChat ggChat = ggChatFactory.factoryGG(userGG.getUser(), userGG, channelToConnect);
        if (apiForChats == null) {
            apiForChats = new ArrayList<>();
            ggSubToListOfConnections.put(subOfMessageSender, apiForChats);
        }
        apiForChats.add(ggChat);
        ggChat.start();
        return Optional.of(ggChat);
    }

    public Optional<ApiForChat> createYTChat(String channelToConnect,
                                             String subOfMessageSender,
                                             List<YTChat> apiForChats,
                                             UserGoogle userGoogle,
                                             Map<String, List<YTChat>> ytSubToListOfConnections) {
        YTChat ytChat = ytChatFactory.factoryYT(userGoogle.getUser(), userGoogle);
        if (apiForChats == null) {
            apiForChats = new ArrayList<>();
            ytSubToListOfConnections.put(subOfMessageSender, apiForChats);
        }
        apiForChats.add(ytChat);
        ytChat.start();
        return Optional.of(ytChat);
    }
}
