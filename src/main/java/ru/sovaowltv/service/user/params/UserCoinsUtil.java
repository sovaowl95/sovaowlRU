package ru.sovaowltv.service.user.params;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Currency;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCoinsUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;

    public synchronized void addCoinsFromDonation(User user, double number, Currency currency) {
        try {
            long value;
            switch (currency.getCurrencyCode()) {
                case "RUB":
                    value = (long) number;
                    break;
                case "EUR":
                    value = (long) (number * 70);
                    break;
                case "USD":
                    value = (long) (number * 63);
                    break;
                default:
                    return;
            }
            addCoins(user, value);
        } catch (Exception e) {
            log.error("add coins from donation error " + user.getId() + " " + number + " " + currency, e);
        }
    }


    public synchronized double calcCoinsRewardNoModificator(User user, double value) {
        if (user.isPremiumUser()) value = value * 2;
        return value;
    }

    public synchronized double calcCoinsRewardWithLevelModificator(User user, double value) {
        if (user.isPremiumUser()) value = value * 2;
        return value * user.getLevel();
    }

    public synchronized void addCoins(User user, double value) {
        try {
            double coins = user.getCoins() + value;
            user.setCoins(coins);
        } catch (Exception e) {
            log.error("can't addCoinsNoModificators to userId " + user.getId(), e);
        }
    }


    public synchronized void addCoins(String userId, double value) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserById(userId);
            addCoins(user, value);
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    synchronized boolean reserveMoney(User user, int value) {
        if (user.getCoins() >= value) {
            user.setCoins(user.getCoins() - value);
            return true;
        } else {
            log.warn("user have not enough money " + user.getNickname() + " " + user.getCoins() + " need:" + value);
        }
        return false;
    }

    public synchronized boolean withdrawMoney(Long id, int value) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserById(id);
            return withdrawMoney(user, value);
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    public synchronized boolean withdrawMoney(User user, int value) {
        try {
            if (user.getCoins() < value) return false;
            if (!reserveMoney(user, value)) {
                log.warn("user have not enough money " + user.getNickname() + " " + user.getCoins());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("withdrawMoney " + user.getNickname() + " " + value, e);
            return false;
        }
    }
}
