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
import ru.sovaowltv.service.messages.MessageApiDeliver;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import java.util.HashMap;
import java.util.Map;

import static ru.sovaowltv.service.unclassified.Constants.CARAVAN_END;
import static ru.sovaowltv.service.unclassified.Constants.CARAVAN_START;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:constants.yml")
public class CaravanMessages {
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final MessagesUtil messagesUtil;
    private final MessageApiDeliver messageApiDeliver;

    @Value("${sovaowlRuHttps}")
    private String sovaowlRuHttps;

    void sendToAllStreamsCaravanStartRobbery(Rarity caravanRarity, int timeToSleep, int currentCaravanPrice, int caravanCounter) {
        MessageStatus caravanStartMessage = prepareCaravanStartMessage(caravanRarity, timeToSleep, currentCaravanPrice, caravanCounter);
        Message message = prepareCaravanStartMessageForApiChats();

        streamRepositoryHandler.getAll().forEach(stream -> {
            if (stream.isLive()) {
                messagesUtil.convertAndSend(stream.getUser().getNickname(), caravanStartMessage);
                messageApiDeliver.sendMessageToAllApiChats(message, stream.getUser().getNickname(), null, stream.getUser(), stream);
            }
        });
    }

    void sendToAllStreamsCaravanEnd(int caravanCounter) {
        MessageStatus caravanEndMessage = new MessageStatus();
        caravanEndMessage.setType(CARAVAN_END);
        caravanEndMessage.setInfo(String.valueOf(caravanCounter));
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

    MessageStatus prepareCaravanStartMessage(Rarity rarity, int timeToSleep, int price, int caravanCounter) {
        MessageStatus ms = new MessageStatus();
        ms.setType(CARAVAN_START);
        Map<String, Object> map = new HashMap<>();
        map.put("rarity", rarity.name());
        map.put("caravanCounter", caravanCounter);
        map.put("time", timeToSleep);
        map.put("price", price);
        ms.setInfo(new Gson().toJson(map));
        return ms;
    }

    private Message prepareCaravanStartMessageForApiChats() {
        Message message = new Message();
        message.setType(CARAVAN_START);
        message.setText("Caravan started! Join website to rob it! " + sovaowlRuHttps);
        message.setOriginalMessage("Caravan started! Join website to rob it!");
        return message;
    }
}
