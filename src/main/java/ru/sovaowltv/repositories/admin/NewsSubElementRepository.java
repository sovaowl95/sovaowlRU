package ru.sovaowltv.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.admin.NewsSubElement;

public interface NewsSubElementRepository extends JpaRepository<NewsSubElement, Long> {
}
