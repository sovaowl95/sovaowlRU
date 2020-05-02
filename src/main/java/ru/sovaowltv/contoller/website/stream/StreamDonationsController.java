package ru.sovaowltv.contoller.website.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.donation.DonationUtil;
import ru.sovaowltv.service.stream.StreamUtil;

@Controller
@RequiredArgsConstructor
public class StreamDonationsController {
    private final StreamUtil streamUtil;
    private final DonationUtil donationUtil;

    @PostMapping("/stream/settings/sendTestDonation")
    @ResponseStatus(HttpStatus.OK)
    public void sendTestDonation() {
        Stream stream = streamUtil.getStreamByAuthContext();
        donationUtil.sendTestDonation(stream);
    }
}