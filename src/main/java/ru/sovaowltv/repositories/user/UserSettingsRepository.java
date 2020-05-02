package ru.sovaowltv.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.user.UserSettings;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
}
