package ru.sovaowltv.service.chat.realization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiWebsiteChats {
    private final Map<String, List<ApiForChat>> chats = new HashMap<>();

    public List<ApiForChat> getChatByChannel(String siteName) {
        chats.putIfAbsent(siteName, new ArrayList<>());
        return chats.get(siteName);
    }

    void deleteStreamChatAllConnection(Stream stream) {
        List<ApiForChat> apiForChats = getChatByChannel(stream.getUser().getNickname());
        apiForChats.forEach(ApiForChat::disconnect);
    }
}
