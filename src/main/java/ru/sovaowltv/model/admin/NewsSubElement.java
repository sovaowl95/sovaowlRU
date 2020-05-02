package ru.sovaowltv.model.admin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class NewsSubElement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    @Enumerated(EnumType.STRING)
    private NewsCategory category;

    @Column
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<String> text;
}
