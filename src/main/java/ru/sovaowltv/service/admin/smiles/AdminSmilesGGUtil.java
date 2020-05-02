package ru.sovaowltv.service.admin.smiles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.smiles.GGSmiles;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSmilesGGUtil {
    private final GGSmiles ggSmilesUtil;

    public void reloadGGSmiles() {
        loadSmilesFromGG();
        parseGGSmiles();
        initGGSmiles();
    }

    public void loadSmilesFromGG() {
        log.info("loadSmilesFromGG -> start");
        ggSmilesUtil.loadSmiles();
        log.info("loadSmilesFromGG -> end");
    }

    public void parseGGSmiles() {
        log.info("parseGGSmiles -> start");
        ggSmilesUtil.parseSmiles();
        log.info("parseGGSmiles -> end");
    }

    public void initGGSmiles() {
        log.info("initGGSmiles -> start");
        ggSmilesUtil.initSmiles();
        log.info("initGGSmiles -> end");
    }
}
