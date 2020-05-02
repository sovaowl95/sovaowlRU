package ru.sovaowltv.service.chat.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.chat.realization.ApiForChat;
import ru.sovaowltv.service.chat.realization.ApiWebsiteChats;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiChatsUtil {
    private final ApiWebsiteChats apiWebsiteChats;

    void disconnectChats(List<ApiForChat> list, Class<? extends ApiForChat> target) {
        Iterator<ApiForChat> iterator = list.iterator();
        while (iterator.hasNext()) {
            ApiForChat ch = iterator.next();
            if (ch.getClass().equals(target)) {
                ch.disconnect();
                iterator.remove();
            }
        }
    }

    Optional<ApiForChat> getChatOwner(String webSiteChannel, Class<? extends ApiForChat> target) {
        List<ApiForChat> apiForChats = apiWebsiteChats.getChatByChannel(webSiteChannel);
        if (apiForChats != null) {
            for (ApiForChat apiForChat : apiForChats) {
                if (apiForChat.getClass().equals(target)) {
                    if (apiForChat.getApiUser().getUser().getNickname().equals(webSiteChannel)) {
                        return Optional.of(apiForChat);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public boolean channelsEquals(ApiForChat apiForAdd, ApiForChat chat) {
        return apiForAdd.getChannelToConnect().equalsIgnoreCase(chat.getChannelToConnect());
    }

    boolean channelsEquals(String channelToConnect, ApiForChat apiForChat) {
        return apiForChat.getChannelToConnect().equalsIgnoreCase(channelToConnect);
    }
}
