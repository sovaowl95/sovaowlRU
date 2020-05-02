package ru.sovaowltv.service.admin.smiles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.smiles.YTSmiles;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSmilesYTUtil {
    private final YTSmiles ytSmilesUtil;

    public void reloadYTSmiles() {
        loadSmilesFromYT();
        parseYTSmiles();
        initYTSmiles();
    }

    public void loadSmilesFromYT() {
        log.info("loadSmilesFromYT -> start");
        ytSmilesUtil.loadSmiles();
        log.info("loadSmilesFromYT -> end");
    }

    public void parseYTSmiles() {
        log.info("parseYTSmiles -> start");
        ytSmilesUtil.parseSmiles();
        log.info("parseYTSmiles -> end");
    }

    public void initYTSmiles() {
        log.info("initYTSmiles -> start");
        ytSmilesUtil.initSmiles();
        log.info("initYTSmiles -> end");
    }
}
