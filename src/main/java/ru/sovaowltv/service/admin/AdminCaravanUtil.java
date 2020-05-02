package ru.sovaowltv.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.admin.AdminSettings;
import ru.sovaowltv.repositories.admin.AdminSettingsRepository;
import ru.sovaowltv.service.caravan.CaravanUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCaravanUtil {
    private final AdminSettingsRepository adminSettingsRepository;
    private final CaravanUtil caravanUtil;

    public void enableCaravan() {
        AdminSettings caravanIsWork = adminSettingsRepository.findByKeyWord("caravanIsWork");
        caravanIsWork.setText("true");
        adminSettingsRepository.save(caravanIsWork);
        caravanUtil.setWork(true);
    }

    public void disableCaravan() {
        AdminSettings caravanIsWork = adminSettingsRepository.findByKeyWord("caravanIsWork");
        caravanIsWork.setText("false");
        adminSettingsRepository.save(caravanIsWork);
        caravanUtil.setWork(false);
    }

    public void nextStepCaravan() {
        caravanUtil.interrupt();
    }
}