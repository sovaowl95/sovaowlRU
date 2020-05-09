package ru.sovaowltv.service.user.params;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.unclassified.Constants;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserExpUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final UserCoinsUtil userCoinsUtil;
    private final MessagesUtil messagesUtil;

    private final Constants constants;

    public void buyOneLevel() {
        User user = null;
        try {
            user = userUtil.getUser();
            log.info("user {} trying buy lvl", user.getLogin());
            if (userCoinsUtil.withdrawMoney(user, constants.getLevelPrice())) {
                user.setExp(0);
                int levelBefore = user.getLevel();
                user.setLevel(levelBefore + 1);
                log.info("user {} bought lvl. now - {} was: {}", user.getLogin(), user.getLevel(), levelBefore);
            } else {
                log.info("user {} trying buy lvl - not enough money: {} price: {}", user.getLogin(), user.getCoins(), constants.getLevelPrice());
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void addExp(String userId, long value, User user) {
        boolean userWasNull = false;
        try {
            if (user == null) {
                userWasNull = true;
                user = usersRepositoryHandler.getUserById(userId);
            }
            int levelExpMultiplier = constants.getLevelExpMultiplier();
            long exp = user.getExp();
            if (user.isPremiumUser()) value = value * 2;
            exp += value;
            user.setExp(exp);
            while (true) {
                int needExpForRankUp = user.getLevel() * levelExpMultiplier;
                if (exp < needExpForRankUp) break;
                exp = exp - needExpForRankUp;
                user.setLevel(user.getLevel() + 1);
                user.setExp(exp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (userWasNull)
                usersRepositoryHandler.saveAndFree(user);
        }
    }

    public void addExpAndPrint(String userId, long value, String channel) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserById(userId);
            int level = user.getLevel();
            addExp(userId, value, user);
            if (level != user.getLevel()) {
                messagesUtil.sendLvlUpMessage(user, channel);
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }


    public int calcExpRewardForUser(User user, int value) {
        return user.isPremiumUser() ? value * 2 : value;
    }
}
