package ru.sovaowltv.repositories.website;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.item.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
