package ru.sovaowltv.repositories.website;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.apinotification.DiscordNotification;

import java.util.Optional;

public interface DiscordNotificationRepository extends JpaRepository<DiscordNotification, Long> {
    Optional<DiscordNotification> findByServer(String server);
}
