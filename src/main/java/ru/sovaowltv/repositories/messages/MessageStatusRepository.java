package ru.sovaowltv.repositories.messages;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.chat.MessageStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {
    List<MessageStatus> findByStreamIdAndTimeAfterOrderByIdDesc(long id, LocalDateTime time);
}
