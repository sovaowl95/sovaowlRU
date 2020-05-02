package ru.sovaowltv.repositories.website;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Smile;

import java.util.List;

public interface SmilesRepository extends JpaRepository<Smile, Long> {
    List<Smile> findAllByRarity(Rarity rarity);
}
