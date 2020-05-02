package ru.sovaowltv.model.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class Message implements ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private int level;

    @Column(nullable = false)
    private String nick;

    @Column(nullable = false)
    @Type(type = "text")
    private String text;

    /**
     * SMILES
     */
    //todo: ANOTHER API SERVICE
    @Column
    @Type(type = "text")
    private String twitchSmilesInfo;

    @Column
    @Type(type = "text")
    private String ggSmilesInfo;

    @Column
    @Type(type = "text")
    private String ytSmilesInfo;

    @Column
    @Type(type = "text")
    private String webSiteSmilesInfo;

    /**
     * SHOWED META INFO
     */
    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private String icons;

    @Column(nullable = false)
    private boolean premText;


    /**
     * HIDDEN META INFO
     */
    @Column
    private String messageSubId;

    @Column(nullable = false)
    private String issuerId;


    /**
     * SERVICE INFO
     */
    @Column
    private boolean moderator = false;

    @Column
    private boolean globalAdmin = false;

    @Column
    private boolean canControlMod = false;

    @Column
    private boolean premiumUser = false;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private long streamId;

    @Column(nullable = false)
    private String style;

    @Column(nullable = false)
    private boolean banned;

    @Column(columnDefinition = "boolean default false")
    private boolean highlighted = false;

    /**
     * TRANSIENT
     */
    @Transient
    private String originalMessage;

}