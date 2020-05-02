package ru.sovaowltv.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.admin.News;

public interface NewsRepository extends JpaRepository<News, Long> {
}
