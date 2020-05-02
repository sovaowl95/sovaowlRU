package ru.sovaowltv.repositories.website;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.roadmap.Roadmap;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
}
