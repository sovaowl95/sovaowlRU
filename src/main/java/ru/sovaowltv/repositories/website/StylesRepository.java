package ru.sovaowltv.repositories.website;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Style;

import java.util.List;
import java.util.Optional;

public interface StylesRepository extends JpaRepository<Style, Long> {
    Optional<Style> findByName(String name);

    List<Style> findAllByRarity(Rarity rarity);
}
