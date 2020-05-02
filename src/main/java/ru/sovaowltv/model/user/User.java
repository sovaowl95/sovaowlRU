package ru.sovaowltv.model.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.sovaowltv.model.apiauth.UserGG;
import ru.sovaowltv.model.apiauth.UserGoogle;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.apiauth.UserVK;
import ru.sovaowltv.model.item.Item;
import ru.sovaowltv.model.shop.Icons;
import ru.sovaowltv.model.shop.Smile;
import ru.sovaowltv.model.shop.Style;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity(name = "userDB")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @Column(nullable = false, length = 30, unique = true)
    private String login;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 60, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDateTime regDate;

    @Column(nullable = false, length = 60)
    private String recoveryToken;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private long exp;

    @Column(nullable = false)
    private double coins;

    @Column(nullable = false)
    private boolean male;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @Column(length = 36)
    private String emailVerification;

    @Column
    private String userAvatar;

    @OneToOne
    @PrimaryKeyJoinColumn
    private UserSettings userSettings;

    @OneToOne
    @PrimaryKeyJoinColumn
    private UserGoogle userGoogle;

    @OneToOne
    @PrimaryKeyJoinColumn
    private UserTwitch userTwitch;

    @OneToOne
    @PrimaryKeyJoinColumn
    private UserGG userGG;

    @OneToOne
    @PrimaryKeyJoinColumn
    private UserVK userVK;


    /**
     * PREMIUM
     */
    @Column(nullable = false)
    private boolean premiumUser;

    @Column
    private LocalDate premiumExpired;

    @Column(nullable = false)
    private int premiumTimes;

    /**
     * SHOP
     */
    @ManyToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Smile> smiles;

    @ManyToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Style> styles;

    @ManyToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Icons> icons;


    @ElementCollection(targetClass = Achievements.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Achievements> achievements;

    /**
     * CARAVAN
     **/
    @OneToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Item> items;

    @Column
    private int caravanRobbedTimes;
}
