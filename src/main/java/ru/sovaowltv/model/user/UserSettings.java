package ru.sovaowltv.model.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.sovaowltv.model.chat.SavedSmile;
import ru.sovaowltv.model.shop.Icons;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class UserSettings implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;

    @MapsId
    @OneToOne
    private User user;

    @Column(nullable = false)
    private long styleId;

    @Column(nullable = false)
    private boolean premiumChat;

    @Column(nullable = false)
    private boolean showTime;

    @Column(nullable = false, columnDefinition = "int default 28")
    private int smileSize;

    @Column(nullable = false, columnDefinition = "int default 16")
    private int textSize;

    @ManyToMany
    @PrimaryKeyJoinColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Icons> activeIcons;

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<SavedSmile> savedSmiles;
}
