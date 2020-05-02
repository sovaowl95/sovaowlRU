package ru.sovaowltv.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.apiauth.UserGG;

import java.util.Optional;

public interface UsersGGRepository extends JpaRepository<UserGG, Long> {
    Optional<UserGG> findBySub(String sub);
}
