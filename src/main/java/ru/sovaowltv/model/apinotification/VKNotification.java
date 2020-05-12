package ru.sovaowltv.model.apinotification;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class VKNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @Column
    private String groupId;

    @Column
    private String wallKey;

    @Column
    private String callbackResponseKey;

    @Column
    private String callbackSecretKey;

    @Column
    private String accessKey;

    @Column
    @Type(type = "text")
    private String text;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Long> vkIds;
}
