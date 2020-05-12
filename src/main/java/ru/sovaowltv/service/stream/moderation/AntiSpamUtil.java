package ru.sovaowltv.service.stream.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.user.User;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AntiSpamUtil {
    private static final Map<Long, UserToTime> map = new ConcurrentHashMap<>();

    private static final long TIME_TO_REMOVE = TimeUnit.SECONDS.toMillis(3);
    private static final long THOUSAND_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long ALLOWED_MESSAGES = 10;

    private static class UserToTime {
        final long id;
        final List<Long> times;
        int timesBlocked;
        static final long TIME_BLOCK = 3000;

        public UserToTime(User user) {
            this.id = user.getId();
            times = new LinkedList<>();
            times.add(System.currentTimeMillis());
        }

        public long block() {
            return TIME_BLOCK * ++timesBlocked;
        }
    }

    public boolean isAntiSpamOk(User user) {
        long id = user.getId();
        UserToTime userToTime = map.get(id);

        if (userToTime == null) {
            map.put(user.getId(), new UserToTime(user));
            return true;
        }

        addAndClear(userToTime);

        return userToTime.times.size() < ALLOWED_MESSAGES;
    }


    public String getTimeUntilUnblock(User user) {
        long id = user.getId();
        UserToTime userToTime = map.get(id);

        return String.valueOf(userToTime.block() / THOUSAND_MS);
    }

    private void addAndClear(UserToTime userToTime) {
        Iterator<Long> iterator = userToTime.times.iterator();
        while (iterator.hasNext()) {
            long timeHasPassed = System.currentTimeMillis() - iterator.next();
            if (timeHasPassed > TIME_TO_REMOVE) {
                iterator.remove();
            } else {
                break;
            }
        }
        userToTime.times.add(System.currentTimeMillis());
    }

    @Scheduled(fixedRate = 1000 * 60)
    private void clearMap() {
        map.entrySet().removeIf(entry -> entry.getValue().times.isEmpty());
    }
}