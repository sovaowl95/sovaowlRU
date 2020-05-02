package ru.sovaowltv.service.chat.realization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiTimeouts {
    private final Map<Long, Map<User, LocalDateTime>> timeouts = new HashMap<>();

    String getTimeForChannelAndUser(Stream stream, User userForCheck) {
        LocalDateTime localDateTime = getTimeoutsByStreamId(stream.getId()).get(userForCheck);
        long between = ChronoUnit.SECONDS.between(LocalDateTime.now(), localDateTime);
        return String.valueOf(between);
    }

    public Map<User, LocalDateTime> getTimeoutsByStreamId(long id) {
        return timeouts.get(id);
    }

    public Map<User, LocalDateTime> setTimeoutsByStreamId(long id) {
        HashMap<User, LocalDateTime> map = new HashMap<>();
        timeouts.put(id, map);
        return map;
    }
}
