package ru.sovaowltv.model.admin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String title;

    @Column
    @CreationTimestamp
    private LocalDateTime time;

    @ManyToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<NewsSubElement> elements;

    @Basic
    private boolean publicated;
}
