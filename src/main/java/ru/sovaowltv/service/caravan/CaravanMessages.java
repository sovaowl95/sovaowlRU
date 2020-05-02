package ru.sovaowltv.service.caravan;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.service.messages.MessageDeliver;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:constants.yml")
public class CaravanMessages {
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final MessagesUtil messagesUtil;
    private final MessageDeliver messageDeliver;

    @Value("${sovaowlRuHttps}")
    private String sovaowlRuHttps;

    void sendToAllStreamsCaravanStartRobbery(Rarity caravanRarity, int timeToSleep, int currentCaravanPrice) {
        MessageStatus caravanStartMessage = prepareCaravanStartMessage(caravanRarity, timeToSleep, currentCaravanPrice);
        Message message = prepareCaravanStartMessageForApiChats();

        streamRepositoryHandler.getAll().forEach(stream -> {
            if (stream.isLive()) {
                messagesUtil.convertAndSend(stream.getUser().getNickname(), caravanStartMessage);
                messageDeliver.sendMessageToAllApiChats(message, stream.getUser().getNickname(), null, stream.getUser());
            }
        });
    }

    void sendToAllStreamsCaravanEnd() {
        MessageStatus caravanEndMessage = new MessageStatus();
        caravanEndMessage.setType("caravanEnd");
        streamRepositoryHandler.getAll().forEach(stream -> {
            if (stream.isLive()) {
                messagesUtil.convertAndSend(stream.getUser().getNickname(), caravanEndMessage);
            }
        });
    }

    void sendToAllStreamsCaravanReward(String caravanRewards) {
        streamRepositoryHandler.getAll().forEach(stream -> {
            if (stream.isLive()) {
                messagesUtil.convertAndSend(stream.getUser().getNickname(), caravanRewards);
            }
        });
    }

    private Message prepareCaravanStartMessageForApiChats() {
        Message message = new Message();
        message.setType("caravanStart");
        message.setText("Caravan started! Join website to rob it! " + sovaowlRuHttps);
        message.setOriginalMessage("Caravan started! Join website to rob it!");
        return message;
    }

    MessageStatus prepareCaravanStartMessage(Rarity rarity, int timeToSleep, int price) {
        MessageStatus ms = new MessageStatus();
        ms.setType("caravanStart");
        Map<String, Object> map = new HashMap<>();
        map.put("rarity", rarity.name());
        map.put("time", timeToSleep);
        map.put("price", price);
        ms.setInfo(new Gson().toJson(map));
        return ms;
    }
}
