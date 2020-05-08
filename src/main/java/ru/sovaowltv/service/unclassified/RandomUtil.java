package ru.sovaowltv.service.unclassified;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class RandomUtil {
    private final Random random = new Random();

    public int getIntWithBounds(int low, int high) {
        return random.nextInt(high - low + 1) + low;
    }

    public double getDoubleWithBounds(int low, int high) {
        return low + (high - low) * random.nextDouble();
    }

    public boolean isChance(double chance) {
        return chance > getDoubleWithBounds(0, 100);
    }

    public int nextInt(int i) {
        return random.nextInt(i);
    }
}
