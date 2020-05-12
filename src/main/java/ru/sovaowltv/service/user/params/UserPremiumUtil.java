package ru.sovaowltv.service.user.params;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.shop.NotEnoughMoneyException;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UserSettingsRepository;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.unclassified.Constants;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPremiumUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UserSettingsRepository userSettingsRepository;

    private final Constants constants;
    private final UserCoinsUtil userCoinsUtil;
    private final MessagesUtil messagesUtil;

    public void buyPremium(User user, int month) {
        if (userCoinsUtil.reserveMoney(user, constants.getPremiumPrice())) {
            givePremiumToUser(user, month);
        } else {
            throw new NotEnoughMoneyException(user.getLogin() + " have not enough money");
        }
    }

    private boolean giftPremium(User user) {
        return givePremiumToUser(user, 1);
    }

    private boolean givePremiumToUser(User user, int month) {
        int daysToAdd = 30 * month;
        user.setPremiumTimes(user.getPremiumTimes() + month);
        givePremiumForDaysToUser(user, daysToAdd);
        return true;
    }

    public void givePremiumForDaysToUser(User user, int days) {
        user.setPremiumUser(true);
        if (user.isPremiumUser() && user.getPremiumExpired() != null && user.getPremiumExpired().isAfter(LocalDate.now())) {
            user.setPremiumExpired(user.getPremiumExpired().plusDays(days));
        } else {
            user.setPremiumExpired(LocalDate.now().plusDays(days));
        }
    }

    public void revalidatePremiums() {
        log.info("validating premiums");
        List<User> userList = usersRepositoryHandler.findAllByPremiumExpiredBeforeAndPremiumUserTrue();
        userList.forEach(user -> {
            try {
                log.info("PREMIUM EXPIRED! {} {}", user.getNickname(), user.getPremiumExpired());
                user.setPremiumUser(false);
                user.setPremiumExpired(null);
                user.getUserSettings().setPremiumChat(false);
                userSettingsRepository.save(user.getUserSettings());
            } finally {
                usersRepositoryHandler.saveAndFree(user);
            }
        });
    }

    public String getPremiumReward(User user) {
        if (giftPremium(user)) {
            return convertPremiumToString(user);
        }
        log.error("can't gift premium to user: {}", user.getNickname());
        return null;
    }

    private String convertPremiumToString(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("rarity", Rarity.ANCIENT);
        map.put("type", "premium");
        map.put("premiumUser", user.isPremiumUser());
        map.put("nickname", user.getNickname());
        return new Gson().toJson(map);
    }

    public void solvePremiumExpiredSoon(String login, String channel, User userIssuer) {
        if (userIssuer.isPremiumUser()) {
            LocalDate premiumExpiredLD = userIssuer.getPremiumExpired();
            LocalTime premiumValidationTime = LocalTime.of(5, 0, 0); //время обновления премиума.
            LocalDateTime premiumExpired = LocalDateTime.of(premiumExpiredLD, premiumValidationTime);
            LocalDateTime now = LocalDateTime.now();
            if (now.plusDays(5).isAfter(premiumExpired)) {
                long daysBetween = ChronoUnit.DAYS.between(now, premiumExpired);
                long hoursBetween = ChronoUnit.HOURS.between(now, premiumExpired);
                long minutesBetween = ChronoUnit.MINUTES.between(now, premiumExpired);
                Map<String, Object> map = new HashMap<>();
                map.put("day", Math.abs(daysBetween));
                map.put("hour", Math.abs(hoursBetween));
                map.put("min", Math.abs(minutesBetween));
                MessageStatus message = messagesUtil.getOkMessageStatus("premiumExpiredIn", new Gson().toJson(map));
                messagesUtil.convertAndSendToUser(login, channel, message);
            }
        }
    }
}