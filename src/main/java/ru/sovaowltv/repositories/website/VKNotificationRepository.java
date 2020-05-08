package ru.sovaowltv.repositories.website;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.apinotification.VKNotification;

public interface VKNotificationRepository extends JpaRepository<VKNotification, Long> {
}
