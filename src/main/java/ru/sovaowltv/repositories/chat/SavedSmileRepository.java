package ru.sovaowltv.repositories.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.chat.SavedSmile;

import java.util.Optional;

public interface SavedSmileRepository extends JpaRepository<SavedSmile, Long> {
    Optional<SavedSmile> getByServiceAndSmileName(String service, String smileName);
}
