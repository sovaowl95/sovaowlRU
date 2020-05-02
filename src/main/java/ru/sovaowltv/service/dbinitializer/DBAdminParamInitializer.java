package ru.sovaowltv.service.dbinitializer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.admin.AdminSettings;
import ru.sovaowltv.repositories.admin.AdminSettingsRepository;
import ru.sovaowltv.service.unclassified.Constants;

@Component
@RequiredArgsConstructor
public class DBAdminParamInitializer {
    private final AdminSettingsRepository adminSettingsRepository;
    private final Constants constants;

    void checkAllAdminSettings() {
        checkOrSetAdminParam("caravanIsWork", "true");
        checkOrSetAdminParam("adminTopMenuInfo", "Sorry for all bugs :(");
        constants.initAdminMessage();
    }

    private void checkOrSetAdminParam(String keyWord, String value) {
        AdminSettings adminSettings = adminSettingsRepository.findByKeyWord(keyWord);
        if (adminSettings == null) {
            setDefaultValueInDB(keyWord, value);
        }
    }

    private void setDefaultValueInDB(String keyWord, String value) {
        AdminSettings admin = new AdminSettings();
        admin.setKeyWord(keyWord);
        admin.setText(value);
        adminSettingsRepository.save(admin);
    }
}
