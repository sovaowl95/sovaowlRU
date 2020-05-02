package ru.sovaowltv.service.time;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TimeUtil {
    public void sleepSeconds(int seconds) {
        sleep(TimeUnit.SECONDS, seconds);
    }

    public void sleepMinutes(int minutes) {
        sleep(TimeUnit.MINUTES, minutes);
    }

    private void sleep(TimeUnit timeUnit, int value) {
        try {
            timeUnit.sleep(value);
        } catch (InterruptedException ignored) {
            log.info("sleep interrupted");
        }
    }
}
