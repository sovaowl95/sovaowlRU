package ru.sovaowltv.service.messages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.repositories.messages.MessageRepository;

@Service
@RequiredArgsConstructor
public class MessageValidator {
    private final MessageRepository messageRepository;

    public MessageValidationStatus validateMessage(Message message, String channel) {
        if (message.getText() == null || message.getText().trim().isEmpty()) return MessageValidationStatus.EMPTY_TEXT;
        if (isSpam(message)) return MessageValidationStatus.SPAM;
        messageRepository.save(message);
        return MessageValidationStatus.OK;
    }

    private boolean isSpam(Message message) {
        return message.getText().toLowerCase().contains("getviewers.pro")
                || message.getText().toLowerCase().contains("getviewers .pro")
                || message.getText().toLowerCase().contains("getviewers")

                || message.getText().toLowerCase().contains("streamhub.us")
                || message.getText().toLowerCase().contains("streamhub .us")
                || message.getText().toLowerCase().contains("streamhub");
    }
}