package ru.sovaowltv.model.apinotification;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

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
    private String key;

    @Column
    @Type(type = "text")
    private String text;
}
