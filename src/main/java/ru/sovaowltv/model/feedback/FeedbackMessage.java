package ru.sovaowltv.model.feedback;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class FeedbackMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private User user;

    @Column
    private String theme;

    @Column
    @Type(type = "text")
    private String message;

    @Column(nullable = false, length = 30)
    private String time;

    @ElementCollection(targetClass = FeedbackStatus.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<FeedbackStatus> feedbackStatuses;
}
