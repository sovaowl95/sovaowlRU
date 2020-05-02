package ru.sovaowltv.model.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class MessageStatus implements ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private long streamId;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column
    private String type;

    @Column
    private String info;
}

