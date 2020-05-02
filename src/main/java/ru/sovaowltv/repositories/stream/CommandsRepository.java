package ru.sovaowltv.repositories.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.command.Command;

public interface CommandsRepository extends JpaRepository<Command, Long> {

}
