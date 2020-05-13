package ru.sovaowltv.service.unclassified;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.admin.AdminSettings;
import ru.sovaowltv.repositories.admin.AdminSettingsRepository;

@Component
@PropertySource("classpath:constants.yml")
@RequiredArgsConstructor
@Getter
@Setter
public class Constants {
    private final AdminSettingsRepository adminSettingsRepository;

    public static final String MESSAGE = "message";
    public static final String MOD_ACTION = "modAction";
    public static final String INFO_BAN = "infoBan";
    public static final String INFO_TIMEOUT = "infoTimeout";
    public static final String INFO_SPAM = "infoSpam";
    public static final String INFO_PREMIUM_EXPIRED_IN = "premiumExpiredIn";
    public static final String INFO_HELP = "help";
    public static final String DONATION = "donation";
    public static final String RANK_UP = "rankUp";
    public static final String SPAM = "spam";
    public static final String CLEAR_ALL = "clearAll";
    public static final String COMMAND_ANSWER_OK = "commandAnswerOk";
    public static final String COMMAND_ANSWER_ERROR = "commandAnswerError";

    public static final String SLOT_START = "slotStart";
    public static final String SLOT_RES = "slotRes";
    public static final String SLOT_NOT_ENOUGH_MONEY = "slotNotEnoughMoney";

    public static final String CARAVAN_START = "caravanStart";
    public static final String CARAVAN_END = "caravanEnd";
    public static final String CARAVAN_REWARD = "caravanReward";
    public static final String CARAVAN_JOIN = "caravanJoin";
    public static final String CARAVAN_JOIN_NOT_ENOUGH_MONEY = "caravanJoinNotEnoughMoney";
    public static final String CARAVAN_JOIN_ERR_ALREADY_IN_JOIN = "caravanErrAlreadyInJoin";
    public static final String CARAVAN_JOIN_ERR_STATUS_JOIN = "caravanErrStatusJoin";
    public static final String CARAVAN_JOIN_ERR_STATUS_JOIN_ANON = "caravanErrStatusJoinAnon";

    public static final String ACC_REJOIN = "ACC_REJOIN";
    public static final String ACC_REJOIN_OK = "ACC_REJOIN_OK";
    public static final String ACC_REJOIN_ASK = "ACC_REJOIN_ASK";
    public static final String API_MOTIVATION = "API_MOTIVATION";

    @Value("${premiumPrice}")
    private int premiumPrice;

    @Value("${levelPrice}")
    private int levelPrice;

    @Value("${levelExpMultiplier}")
    private int levelExpMultiplier;

    @Value("${donationMultiplier}")
    private int donationMultiplier;

    private String adminTopMenuInfo;

    public void initAdminMessage() {
        AdminSettings admin = adminSettingsRepository.findByKeyWord("adminTopMenuInfo");
        if (admin == null) return;
        adminTopMenuInfo = admin.getText();
    }
}
