package ru.sovaowltv.contoller.website;

import com.qiwi.billpayments.sdk.model.Notification;
import com.qiwi.billpayments.sdk.utils.BillPaymentsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.donation.DonationUtil;

import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/qiwi.yml")
public class QiwiController {
    private final DonationUtil donationUtil;

    private final DataExtractor dataExtractor;

    @Value("${qiwi_clientSecret}")
    public String secretKey;

    @GetMapping("/qiwi/success")
    public String getQiwiSuccess() {
        return "redirect:/pay";
    }

    @PostMapping("/qiwi/success")
    @ResponseBody
    public String some(@RequestHeader("X-Api-Signature-SHA256") String validSignature, @RequestBody String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        Notification notification = donationUtil.getNotification(map);
        boolean check = BillPaymentsUtils.checkNotificationSignature(validSignature, notification, secretKey);
        if (!check) {
            log.error("Bad QIWI Validation");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad QIWI Validation");
        } else {
            return donationUtil.solveDonation(notification);
        }
    }

    @PostMapping("/qiwi/createBill")
    @ResponseBody
    public String createBillPost(@RequestBody String json) {
        try {
            return donationUtil.createBill(json);
        } catch (Exception e) {
            log.error("can't create bill", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something goes wrong");
        }
    }
}