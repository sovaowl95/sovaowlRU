package ru.sovaowltv.model.shop;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
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
}
