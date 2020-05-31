package ru.sovaowltv.service.messages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.repositories.messages.MessageRepository;

@Service
@RequiredArgsConstructor
public class MessageValidator {
    private final MessageRepository messageRepository;

    public MessageValidationStatus validateMessage(Message message) {
        if (message.getText() == null || message.getText().trim().isEmpty()) return MessageValidationStatus.EMPTY_TEXT;
        if (isSpam(message)) return MessageValidationStatus.SPAM;
        messageRepository.save(message);
        return MessageValidationStatus.OK;
    }

    private boolean isSpam(Message message) {
        String text = message.getText().toLowerCase();
        return text.contains("getviewers.pro")
                || text.contains("getviewers .pro")
                || text.contains("getviewers")

                || text.contains("streamhub.us")
                || text.contains("streamhub .us")
                || text.contains("streamhub")

                || text.contains("twitchviewer com")

                || text.contains("twitchviewbot")

                || text.contains("addviewers.com")
                || text.contains("addviewers. com")


                || text.contains("streamdetails")
                ;
    }
}
