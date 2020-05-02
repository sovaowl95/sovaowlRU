package ru.sovaowltv.model.donate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;

import javax.persistence.*;
import java.util.Currency;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Entity
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @Column
    private String uuid;

    @ManyToOne(optional = false)
    private User userFrom;

    @ManyToOne
    private Stream streamTo;

    @Column
    private String text;

    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private boolean anonymously;

    @Enumerated(EnumType.STRING)
    private DonationStatus donationStatuses;

    @Enumerated(EnumType.STRING)
    private DonationStatus donationPaidStatus;
}
