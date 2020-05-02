package ru.sovaowltv.model.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class SavedSmile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String smileCode;

    @Column(nullable = false)
    private String smileName;

    @Column(nullable = false)
    private String service;
}
