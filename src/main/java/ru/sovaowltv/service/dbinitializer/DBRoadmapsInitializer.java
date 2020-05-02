package ru.sovaowltv.service.dbinitializer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.roadmap.Roadmap;
import ru.sovaowltv.model.roadmap.RoadmapStatus;
import ru.sovaowltv.repositories.website.RoadmapRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DBRoadmapsInitializer {
    private final RoadmapRepository roadmapRepository;

    void initRoadmaps() {
        List<Roadmap> roadmaps = roadmapRepository.findAll();
        if (roadmaps.isEmpty()) {
            saveCompleteRoadmaps();
            saveInProgressRoadmaps();
            saveSoonRoadmaps();
            saveQuarterRoadmaps();
            saveHalfYearRoadmaps();
            saveYearRoadmaps();
            saveAdvisedRoadmaps();
        }
    }

    private void saveAdvisedRoadmaps() {
        saveRoadmap("", RoadmapStatus.ADVISED, "Предложение от LocateCat #1");
        saveRoadmap("", RoadmapStatus.ADVISED, "Предложение от LocateCat #2");
    }

    private void saveYearRoadmaps() {
        saveRoadmap("", RoadmapStatus.YEAR, "Информация под стримом");
        saveRoadmap("", RoadmapStatus.YEAR, "Собственный плеер");
        saveRoadmap("", RoadmapStatus.YEAR, "Аналог Donation Alerts");
        saveRoadmap("", RoadmapStatus.YEAR, "Голосования, Новости для чата");
    }

    private void saveHalfYearRoadmaps() {
        saveRoadmap("", RoadmapStatus.HALF_YEAR, "Интеграция дискорда");
        saveRoadmap("", RoadmapStatus.HALF_YEAR, "Адаптивный дизайн");
        saveRoadmap("", RoadmapStatus.HALF_YEAR, "Мобильное приложение");
    }

    private void saveQuarterRoadmaps() {
        saveRoadmap("Попап чат. Чат в отдельном окне", RoadmapStatus.QUARTER, "Отображение чата");
        saveRoadmap("Шрифт\\фон\\итд", RoadmapStatus.QUARTER, "Настройки для отображения чата");
    }

    private void saveSoonRoadmaps() {
        saveRoadmap("Добавляем контент", RoadmapStatus.SOON, "Новые смайлы");
        saveRoadmap("ачивменты для стримеров и зрителей", RoadmapStatus.SOON, "Ачивки");
        saveRoadmap("Добавляем контент", RoadmapStatus.SOON, "Новые стили");
    }

    private void saveInProgressRoadmaps() {
        saveRoadmap("твич\\гг\\ютуб стримы и плееры", RoadmapStatus.IN_PROGRESS, "Интеграция популярных платформ");
        saveRoadmap("После релиза много багов. Кто-то должен позаботиться о них :D", RoadmapStatus.IN_PROGRESS, "Баг фиксы");
        saveRoadmap("Вернуть старого бота на стрим", RoadmapStatus.IN_PROGRESS, "Чат бот");
    }

    private void saveCompleteRoadmaps() {
        saveRoadmap("Ура-ура. Релиз!", RoadmapStatus.COMPLETE, "Релиз");
    }

    private void saveRoadmap(String description, RoadmapStatus roadmapStatus, String title) {
        Roadmap roadmap = new Roadmap();
        roadmap.setDateInit(LocalDateTime.now());
        roadmap.setDateComplete(null);
        roadmap.setDescription(description);
        roadmap.setRoadmapStatus(roadmapStatus);
        roadmap.setTitle(title);
        roadmapRepository.save(roadmap);
    }
}
