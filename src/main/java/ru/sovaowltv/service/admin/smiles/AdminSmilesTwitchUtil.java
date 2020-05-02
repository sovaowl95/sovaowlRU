package ru.sovaowltv.service.admin.smiles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.smiles.TwitchSmiles;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSmilesTwitchUtil {
    private final TwitchSmiles twitchSmilesUtil;

    public void reloadTwitchSmiles() {
        loadSmilesFromTwitch();
        parseTwitchSmiles();
        initTwitchSmiles();
    }

    public void loadSmilesFromTwitch() {
        log.info("loadSmilesFromTwitch -> start");
        twitchSmilesUtil.loadSmiles();
        log.info("loadSmilesFromTwitch -> end");
    }

    public void parseTwitchSmiles() {
        log.info("parseTwitchSmiles -> start");
        twitchSmilesUtil.parseSmiles();
        log.info("parseTwitchSmiles -> end");
    }

    public void initTwitchSmiles() {
        log.info("initTwitchSmiles -> start");
        twitchSmilesUtil.initSmiles();
        log.info("initTwitchSmiles -> end");
    }
}
