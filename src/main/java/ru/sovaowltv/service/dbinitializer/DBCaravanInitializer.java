package ru.sovaowltv.service.dbinitializer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.service.caravan.CaravanUtil;

@Component
@RequiredArgsConstructor
public class DBCaravanInitializer {
    private final CaravanUtil caravanUtil;

    void startCaravan() {
        caravanUtil.start();
    }
}
