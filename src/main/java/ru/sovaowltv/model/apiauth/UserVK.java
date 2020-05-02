package ru.sovaowltv.model.apiauth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class UserVK implements ApiUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @Column
    private String sub;

    @MapsId
    @OneToOne
    private User user;

    @Column
    private String nick;

    @Column
    private String accessToken;

    @Column
    private String refreshToken;

    @Column
    private LocalDateTime expiresIn;

    @Column
    private String src;

    @Column
    private String scope;

    @Column(columnDefinition = "boolean default false")
    private boolean corrupted;

    @Column(columnDefinition = "boolean default false")
    private boolean expired;
}
