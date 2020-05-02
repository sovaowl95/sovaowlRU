package ru.sovaowltv.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.admin.AdminSettings;

public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
    AdminSettings findByKeyWord(String key);
}
