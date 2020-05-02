package ru.sovaowltv.model.stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.sovaowltv.model.apinotification.DiscordNotification;
import ru.sovaowltv.model.apinotification.VKNotification;
import ru.sovaowltv.model.command.Command;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class Stream {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @OneToOne(cascade = CascadeType.DETACH)
    private User user;

    @Column(nullable = false)
    private String streamName;

    @Column(nullable = false)
    private String game;

    @Column(nullable = false)
    private String streamDescription;

    @Column
    private String chatDailyInfo;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "stream_id")
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Spammer> spammerSet;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "command_id")
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Command> commandSet;

    @ManyToMany(cascade = CascadeType.DETACH)
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<User> followersList;

    @ManyToMany(cascade = CascadeType.DETACH)
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<User> subscribersList;

    @ManyToMany(cascade = CascadeType.DETACH)
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<User> moderatorsList;

    @ManyToMany(cascade = CascadeType.DETACH)
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<User> bansList;

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    private DiscordNotification discordNotification;

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    private VKNotification vkNotification;

    @Column(nullable = false)
    private boolean live;

    @Column(nullable = false)
    private boolean ban;

    @Column(nullable = false)
    private boolean verified;
}
