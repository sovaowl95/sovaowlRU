package ru.sovaowltv.repositories.messages;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.chat.ChatMessage;
import ru.sovaowltv.model.chat.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<ChatMessage> findTop20ByStreamIdAndTimeAfterAndBannedIsFalseOrderByIdDesc(long id, LocalDateTime time);

    List<ChatMessage> findByStreamIdAndTimeAfterAndBannedIsFalseAndIssuerIdOrderByIdDesc(long id, LocalDateTime time, String issuerId);

    List<ChatMessage> findByStreamIdAndTimeAfterAndBannedIsFalseAndNickOrderByIdDesc(long id, LocalDateTime time, String nickName);

    Optional<Message> findTop1ByNickEqualsOrderByIdDesc(String nick);

    Optional<Message> findByIdAndSource(Long id, String source);

    List<Message> findByStreamIdAndTimeAfter(long id, LocalDateTime time);

    Optional<Message> findByMessageSubId(String messageSubId);
}
