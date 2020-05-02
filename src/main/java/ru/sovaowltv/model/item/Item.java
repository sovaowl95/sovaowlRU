package ru.sovaowltv.model.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private User user;

    @Column
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer exp;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;
}
