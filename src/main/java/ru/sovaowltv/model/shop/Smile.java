package ru.sovaowltv.model.shop;

import lombok.Getter;
import lombok.Setter;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
public class Smile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private User userOwner;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String link;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Smile smile = (Smile) o;
        return Objects.equals(name, smile.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
