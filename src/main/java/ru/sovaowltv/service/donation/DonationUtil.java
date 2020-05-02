package ru.sovaowltv.service.donation;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.qiwi.billpayments.sdk.client.BillPaymentClient;
import com.qiwi.billpayments.sdk.client.BillPaymentClientFactory;
import com.qiwi.billpayments.sdk.model.Bill;
import com.qiwi.billpayments.sdk.model.BillStatus;
import com.qiwi.billpayments.sdk.model.MoneyAmount;
import com.qiwi.billpayments.sdk.model.Notification;
import com.qiwi.billpayments.sdk.model.in.CreateBillInfo;
import com.qiwi.billpayments.sdk.model.in.Customer;
import com.qiwi.billpayments.sdk.model.out.BillResponse;
import com.qiwi.billpayments.sdk.model.out.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.donate.Donation;
import ru.sovaowltv.model.donate.DonationStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.Achievements;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageStatusRepository;
import ru.sovaowltv.repositories.website.DonationRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.factorys.DonationFactory;
import ru.sovaowltv.service.icons.IconsUtil;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.unclassified.Constants;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserCoinsUtil;
import ru.sovaowltv.service.user.params.UserExpUtil;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/qiwi.yml")
public class DonationUtil {
    private final DonationRepository donationRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final UserUtil userUtil;
    private final UserExpUtil userExpUtil;
    private final UserCoinsUtil userCoinsUtil;
    private final StreamUtil streamUtil;
    private final MessagesUtil messagesUtil;
    private final IconsUtil iconsUtil;

    private final DonationFactory donationFactory;

    private final HtmlTagsClear htmlTagsClear;
    private final Constants constants;
    private final MessageStatusRepository messageStatusRepository;
    private final DataExtractor dataExtractor;

    @Value("${qiwi_clientSecret}")
    public String secretKey;

    @Value("${qiwi_clientId}")
    public String clientId;

    private void saveBill(String uid) {
        User userFrom = null;
        User user = null;
        try {
            BillPaymentClient client = BillPaymentClientFactory.createDefault(secretKey);
            BillResponse billInfo = client.getBillInfo(uid);
            ResponseStatus status = billInfo.getStatus();
            BillStatus value = status.getValue();

            if (value == BillStatus.PAID) {
                Donation donation = donationRepository.findByUuid(uid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find donation"));
                donation.setDonationStatuses(DonationStatus.PAID);
                donationRepository.save(donation);

                if (donation.getStreamTo() == null) {
                    user = usersRepositoryHandler.getUserByEmail("x-men-max@yandex.ru");
                    Stream stream = streamUtil.getStreamByUserNickname(user.getNickname());
                    donation.setStreamTo(stream);
                }

                if (donation.getStreamTo() != null) {
                    MessageStatus messageStatus = new MessageStatus();
                    messageStatus.setType("donation");

                    userFrom = donation.getUserFrom();
                    userFrom = usersRepositoryHandler.getUserById(userFrom.getId());

                    Map<String, String> map = new HashMap<>();
                    map.put("nick", donation.isAnonymously() ? "null" : userFrom.getNickname());
                    map.put("text", donation.getText());
                    map.put("sum", String.valueOf(donation.getValue()));
                    map.put("currency", donation.getCurrency().toString());
                    String json = new Gson().toJson(map);

                    messageStatus.setInfo(json);
                    messageStatus.setStreamId(donation.getStreamTo().getId());
                    messageStatus.setTime(LocalDateTime.now());
                    messageStatusRepository.save(messageStatus);

                    log.info("messageStatus = " + messageStatus);

                    messagesUtil.convertAndSend(donation.getStreamTo().getUser().getNickname(), messageStatus);
                    double number = donation.getValue() * constants.getDonationMultiplier();
                    userCoinsUtil.addCoinsFromDonation(userFrom, number, donation.getCurrency());
                    userExpUtil.addExp("", (long) number, userFrom);
                    if (!userFrom.getAchievements().contains(Achievements.DONATOR)) {
                        iconsUtil.addIconAchievement(userFrom, Achievements.DONATOR);
                        userFrom.getAchievements().add(Achievements.DONATOR);
                    }

                    donation.setDonationPaidStatus(DonationStatus.PAID);
                    donationRepository.save(donation);
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not paid");
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
            usersRepositoryHandler.saveAndFree(userFrom);
        }
    }


    public Notification getNotification(Map<String, Object> map) {
        LinkedTreeMap bill = (LinkedTreeMap) map.get("bill");
        return new Notification(
                new Bill(
                        String.valueOf(bill.get("siteId")),
                        String.valueOf(bill.get("billId")),
                        new MoneyAmount(
                                new BigDecimal(String.valueOf(((LinkedTreeMap) bill.get("amount")).get("value"))),
                                Currency.getInstance(String.valueOf(((LinkedTreeMap) bill.get("amount")).get("currency")))
                        ),
                        BillStatus.PAID
                ),
                "3"
        );
    }

    @ResponseBody
    public String solveDonation(Notification notification) {
        try {
            saveBill(notification.getBill().getBillId());
            return "{\"error\":\"0\"}";
        } catch (Exception e) {
            log.error("QIWI Verify failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't verify");
        }
    }

    public String createBill(String json) throws URISyntaxException {
        User userFrom = null;
        try {
            Map<String, Object> map = dataExtractor.extractMapFromString(json);
            boolean anonymously = Boolean.parseBoolean(String.valueOf(map.get("anonymously")));
            Currency currency = Currency.getInstance(map.get("currency").toString());
            String text = String.valueOf(map.get("text"));
            text = htmlTagsClear.removeTags(text);
            Double value = Double.parseDouble(String.valueOf(map.get("value")));
            try {
                userFrom = userUtil.getUser();
            } catch (UserNotFoundException e) {
                userFrom = usersRepositoryHandler.getUserByEmail("x-men-max@yandex.ru");
            }
            String donationForWhom = String.valueOf(map.get("forWhom"));

            Stream streamTo = forMeOrAnotherStreamer(donationForWhom);

            Donation donation = donationFactory.createDonation(anonymously, currency, text, value, userFrom, streamTo);

            BillPaymentClient client = BillPaymentClientFactory.createDefault(secretKey);

            CreateBillInfo billInfo = new CreateBillInfo(
                    donation.getUuid(),
                    new MoneyAmount(BigDecimal.valueOf(donation.getValue()), donation.getCurrency()),
                    donation.getText(),
                    ZonedDateTime.now().plusDays(45),
                    new Customer(userFrom.getEmail(), String.valueOf(userFrom.getId()), "null"),
                    "https://sovaowl.ru/qiwi/success");
            BillResponse bill = client.createBill(billInfo);
            return bill.getPayUrl();
        } finally {
            usersRepositoryHandler.saveAndFree(userFrom);
        }
    }

    private Stream forMeOrAnotherStreamer(String donationForWhom) {
        try {
            long id = Long.parseLong(donationForWhom);
            Optional<Stream> streamOptional = streamRepositoryHandler.getByUserId(id);
            return streamOptional.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public void sendTestDonation(Stream stream) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType("donation");

        Map<String, String> map = new HashMap<>();
        map.put("nick", "nick");
        map.put("text", "text");
        map.put("sum", "100");
        map.put("currency", "RUB");
        String json = new Gson().toJson(map);

        messageStatus.setInfo(json);
        messageStatus.setStreamId(stream.getId());
        messageStatus.setTime(LocalDateTime.now());

        messagesUtil.convertAndSend(stream.getUser().getNickname(), messageStatus);
    }
}
