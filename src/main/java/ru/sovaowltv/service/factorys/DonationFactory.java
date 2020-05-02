package ru.sovaowltv.service.factorys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.donate.Donation;
import ru.sovaowltv.model.donate.DonationStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.DonationRepository;

import java.util.Currency;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DonationFactory {
    private final DonationRepository donationRepository;

    public Donation createDonation(boolean anonymously, Currency currency, String text, Double value, User userFrom, Stream streamTo) {
        Donation donation = new Donation();
        donation.setUuid(UUID.randomUUID().toString());
        donation.setAnonymously(anonymously);
        donation.setCurrency(currency);
        donation.setText(text);
        donation.setValue(value);
        donation.setDonationStatuses(DonationStatus.WAITING);
        donation.setDonationPaidStatus(DonationStatus.WAITING);

        donation.setUserFrom(userFrom);
        donation.setStreamTo(streamTo);

        donationRepository.save(donation);
        return donation;
    }
}
