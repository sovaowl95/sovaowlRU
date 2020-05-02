package ru.sovaowltv.model.spammer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.sovaowltv.model.stream.Stream;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class Spammer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @ManyToOne
    private Stream stream;

    @Column
    private String text;

    @Column
    private int time;

    @Column
    private int delay;

    @Enumerated(EnumType.STRING)
    private SpammerStatus spammerStatus;
}
