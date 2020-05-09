package ru.sovaowltv.service.caravan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.service.time.TimeUtil;
import ru.sovaowltv.service.unclassified.RandomUtil;
import ru.sovaowltv.service.user.params.UserCoinsUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * caravanPrepare          ждем каравана (30 мин)
 * caravanStart            набираем людей для грабежа. (1 мин)
 * caravanEnd              ограбление закончено. выдаются награды, залечиваются раны (0 сек)
 * <p>
 * <p>
 * <p>
 * caravanReward           награда человека за караван.
 * <p>
 * <p>
 * <p>
 * caravanJoin             успешное присоединение к каравану
 * caravanErrAlreadyInJoin вы уже участвуете в ограблении
 * caravanErrStatusJoin    караван в поиске
 * <p>
 * <p>
 * <p>
 * for ChatController
 * CARAVAN -> JOIN
 * CARAVAN -> ERRJOIN
 */
@Component
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
@PropertySource("classpath:constants.yml")
public class Caravan extends Thread {
    private final RarityUtil rarityUtil;
    private final CaravanRewardUtil caravanRewardUtil;
    private final RandomUtil randomUtil;
    private final TimeUtil timeUtil;
    private final UserCoinsUtil userCoinsUtil;

    private final CaravanMessages caravanMessages;

    @Value("${caravanIdleTimeMINInMin}")
    private int caravanIdleTimeMINInMin;

    @Value("${caravanIdleTimeMAXInMin}")
    private int caravanIdleTimeMAXInMin;

    @Value("${caravanJoinTimeMINInMin}")
    private int caravanJoinTimeMINInMin;

    @Value("${caravanJoinTimeMAXInMin}")
    private int caravanJoinTimeMAXInMin;

    private int caravanCounter;
    private boolean work;
    private int timeToSleep;
    private Rarity caravanRarity;
    private CaravanStatus caravanStatus;
    private Set<Long> robbers = new HashSet<>();

    synchronized String joinRobbery(Long id) {
        if (caravanStatus == CaravanStatus.GROUP_UP) {
            if (robbers.contains(id)) {
                return "caravanErrAlreadyInJoin";
            } else {
                return isEnoughMoneyToJoin(id);
            }
        } else {
            return "caravanErrStatusJoin";
        }
    }

    private String isEnoughMoneyToJoin(Long id) {
        if (userCoinsUtil.withdrawMoney(id, getCurrentCaravanPrice())) {
            robbers.add(id);
            return "caravanJoin";
        } else {
            return "caravanJoinNotEnoughMoney";
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                caravanInnerCycle();
                timeUtil.sleepMinutes(1); // if cycle suddenly stop :(
            } catch (Exception e) {
                log.error("CARAVAN CYCLE BREAK", e);
            }
        }
    }

    private void caravanInnerCycle() {
        while (work) {
            timeUtil.sleepMinutes(randomUtil.getIntWithBounds(caravanIdleTimeMINInMin, caravanIdleTimeMAXInMin));
            startRobberyPrepare();
            timeUtil.sleepMinutes(timeToSleep);
            finishRobbery();
        }
    }

    private void startRobberyPrepare() {
        caravanCounter++;
        timeToSleep = randomUtil.getIntWithBounds(caravanJoinTimeMINInMin, caravanJoinTimeMAXInMin);
        caravanStatus = CaravanStatus.GROUP_UP;
        caravanRarity = rarityUtil.generateRarity(Rarity.COMMON, Rarity.ANCIENT, 3);
        caravanMessages.sendToAllStreamsCaravanStartRobbery(caravanRarity, timeToSleep, getCurrentCaravanPrice(), caravanCounter);
    }

    private void finishRobbery() {
        caravanStatus = CaravanStatus.FINISHING;
        caravanMessages.sendToAllStreamsCaravanEnd(caravanCounter);
        caravanStatus = CaravanStatus.WAITING_NEXT;
        caravanRewardUtil.giveReward(robbers, caravanRarity, caravanCounter);
    }

    int getCurrentCaravanPrice() {
        return rarityUtil.changeRarityToPrice(caravanRarity);
    }
}
