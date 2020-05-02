package ru.sovaowltv.model.roadmap;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class Roadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private User user;

    @Column(nullable = false)
    private LocalDateTime dateInit;

    @Column
    private LocalDateTime dateComplete;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Type(type = "text")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoadmapStatus roadmapStatus;

    @ManyToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<User> down;

    @ManyToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<User> up;
}
