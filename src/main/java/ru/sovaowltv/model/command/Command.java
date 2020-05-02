package ru.sovaowltv.model.command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.sovaowltv.model.stream.Stream;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class Command {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private boolean enabled;

    @ManyToOne
    private Stream stream;

    @Column(nullable = false)
    private String keyWord;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<String> alias;

    @Column
    private String action;

    @Column
    private boolean forPublicShown;

    @Column
    private boolean needArgs;

    @Column
    private int argsCount;

    @Column
    private int cooldown;

    @Column
    private int cost;
}
