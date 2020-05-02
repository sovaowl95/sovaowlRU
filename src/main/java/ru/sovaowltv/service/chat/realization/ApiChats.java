package ru.sovaowltv.service.chat.realization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.chat.util.ApiChatsUtil;
import ru.sovaowltv.service.chat.util.GGChatUtil;
import ru.sovaowltv.service.chat.util.TwitchChatUtil;
import ru.sovaowltv.service.chat.util.YTChatUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiChats {
    private final ApiChatsUtil apiChatsUtil;
    private final TwitchChatUtil twitchChatUtil;
    private final GGChatUtil ggChatUtil;
    private final YTChatUtil ytChatUtil;

    private final ApiWebsiteChats apiWebsiteChats;

    //todo: ANOTHER API SERVICE
    public void addStreamChatConnection(String siteName, ApiForChat apiForAdd) {
        boolean found = false;
        List<ApiForChat> listOfChats = apiWebsiteChats.getChatByChannel(siteName);
        for (ApiForChat chat : listOfChats) {
            if (classEquals(apiForAdd, chat) && apiChatsUtil.channelsEquals(apiForAdd, chat)) {
                found = true;
                break;
            }
        }

        if (!found)
            addAndStart(apiForAdd, listOfChats);

        if (apiForAdd instanceof TwitchChat) {
            twitchChatUtil.addTwitch((TwitchChat) apiForAdd);
        } else if (apiForAdd instanceof GGChat) {
            ggChatUtil.addGG((GGChat) apiForAdd);
        } else if (apiForAdd instanceof YTChat) {
            ytChatUtil.addYT((YTChat) apiForAdd);
        }
    }

    private void addAndStart(ApiForChat apiForAdd, List<ApiForChat> listOfChats) {
        listOfChats.add(apiForAdd);
        apiForAdd.start();
    }

    private boolean classEquals(ApiForChat apiForChat, ApiForChat v) {
        return v.getClass().equals(apiForChat.getClass());
    }

    //todo: все ли чаты убиваются?...
    public void deleteStreamChatConnection(Stream stream) {
        apiWebsiteChats.deleteStreamChatAllConnection(stream);
        twitchChatUtil.deleteStreamChatTWITCHConnection(stream);
        ggChatUtil.deleteStreamChatGOODGAMEConnection(stream);
        ytChatUtil.deleteStreamChatYOUTUBEConnection(stream);
    }
}