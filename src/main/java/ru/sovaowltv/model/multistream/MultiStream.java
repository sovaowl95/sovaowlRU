package ru.sovaowltv.model.multistream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class MultiStream {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @OneToOne(cascade = CascadeType.DETACH)
    private User user;

    @Column
    private String inviteCode;

    @OneToOne
    private Stream stream;

    @ManyToMany(cascade = CascadeType.DETACH)
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Stream> streamSet;
}
