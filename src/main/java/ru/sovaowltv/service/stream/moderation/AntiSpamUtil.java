package ru.sovaowltv.service.stream.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.user.User;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AntiSpamUtil {
    private final HashMap<Long, Long> userTimeMap = new HashMap<>();
    private final HashMap<Long, Long> userCountMap = new HashMap<>();

    private void setLastTimeMessage(User user) {
        userTimeMap.put(user.getId(), System.currentTimeMillis());
    }

    private long getTimeForUser(User user) {
        if (userCountMap.containsKey(user.getId())) {
            return 1000 * userCountMap.get(user.getId());
        } else {
            return 500;
        }
    }

    private void incrementAntiSpamTime(User user) {
        long aLong = !userCountMap.containsKey(user.getId()) ? 1 : userCountMap.get(user.getId()) + 1;
        userCountMap.put(user.getId(), aLong);
    }

    private void clearAntiSpamTime(User user) {
        userCountMap.remove(user.getId());
    }

    public boolean lastTimeMessageOk(User userForCheck) {
        if (!userTimeMap.containsKey(userForCheck.getId())) {
            setLastTimeMessage(userForCheck);
            return true;
        } else {
            Long aLong = userTimeMap.get(userForCheck.getId());
            setLastTimeMessage(userForCheck);
            boolean canChat = System.currentTimeMillis() - aLong > getTimeForUser(userForCheck);
            if (!canChat) incrementAntiSpamTime(userForCheck);
            else clearAntiSpamTime(userForCheck);
            return canChat;
        }
    }

    public String getSpamTime(User userForCheck) {
        return String.valueOf(getTimeForUser(userForCheck) / 1000);
    }
}
