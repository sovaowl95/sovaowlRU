package ru.sovaowltv.repositories.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;

import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, Long> {
    Optional<Stream> findByUserId(long id);

    Optional<Stream> findByUser(User user);
}
