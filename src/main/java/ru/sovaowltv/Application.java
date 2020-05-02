package ru.sovaowltv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 1. если захочу подключать другой чат.
 * todo: ANOTHER API SERVICE
 * <p>
 * <p>
 * <p>
 * 2. порядок полей:
 * repositories
 * utils
 * factories
 * IO
 * other
 */

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties
public class Application {
    public static void main(String[] args) {
        if (!args[0].equals("SS")) System.exit(999);
        SpringApplication.run(Application.class, args);
    }
}

//todo: auto Update Principal when user get\\lost new ROLE (-moderator\\+moderator)