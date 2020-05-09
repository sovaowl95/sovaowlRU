package ru.sovaowltv.service.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@Slf4j
public class LoggerUtil {
    public void logMessageFromChat(@DestinationVariable String channel, Principal principal, String messageText) {
        log.info("\n  to: {}\nfrom: {}\n{}", channel, principal.getName(), messageText);
    }
}
