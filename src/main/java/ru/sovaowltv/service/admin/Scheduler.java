package ru.sovaowltv.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Scheduler {
    private static final long MB = 1024L * 1024L;

    @Scheduled(cron = "*/60 * * * * *") //every 60 sec
    public void memoryLeak() {
        // get Runtime instance
        Runtime instance = Runtime.getRuntime();

        // free memory
        log.info("Free Memory: {}", instance.freeMemory() / MB);

        // used memory
        log.info("Used Memory: {}", (instance.totalMemory() - instance.freeMemory()) / MB);

        // Maximum available memory
        log.info("Max Memory: {}", instance.maxMemory() / MB);
    }
}
