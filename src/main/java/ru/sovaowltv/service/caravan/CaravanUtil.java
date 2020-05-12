package ru.sovaowltv.service.caravan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.admin.AdminSettings;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.repositories.admin.AdminSettingsRepository;

@Service
@RequiredArgsConstructor
public class CaravanUtil {
    private final AdminSettingsRepository adminSettingsRepository;

    private final Caravan caravan;
    private final CaravanMessages caravanMessages;

    public String joinRobbery(Long id) {
        return caravan.joinRobbery(id);
    }

    public MessageStatus prepareCaravanStartMessage() {
        return caravanMessages.prepareCaravanStartMessage(
                caravan.getCaravanRarity(),
                caravan.getTimeToSleep(),
                caravan.getCurrentCaravanPrice(),
                caravan.getCaravanCounter()
        );
    }

    public void start() {
        AdminSettings caravanIsWork = adminSettingsRepository.findByKeyWord("caravanIsWork");
        if (caravanIsWork == null) return;
        boolean work = Boolean.parseBoolean(caravanIsWork.getText());
        setWork(work);
        caravan.start();
    }

    public void interrupt() {
        caravan.interrupt();
    }

    public void setWork(boolean work) {
        caravan.setWork(work);
    }

    public String getCaravanRarityName() {
        return caravan.getCaravanRarity().name();
    }

    public CaravanStatus getCaravanStatus() {
        return caravan.getCaravanStatus();
    }
}
