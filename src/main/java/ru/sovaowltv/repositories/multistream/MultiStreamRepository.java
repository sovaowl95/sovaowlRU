package ru.sovaowltv.repositories.multistream;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.multistream.MultiStream;

import java.util.Optional;

public interface MultiStreamRepository extends JpaRepository<MultiStream, Long> {
    Optional<MultiStream> findByUserId(long id);
}
