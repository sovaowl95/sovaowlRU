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
