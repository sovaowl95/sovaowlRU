package ru.sovaowltv.repositories.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.spammer.Spammer;

public interface SpammerRepository extends JpaRepository<Spammer, Long> {
}
