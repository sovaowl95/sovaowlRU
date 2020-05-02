package ru.sovaowltv.service.dbinitializer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class DBInitScheduler {
    private final DBStylesInitializer dbStylesInitializer;
    private final DBSmilesInitializer dbSmilesInitializer;
    private final DBRoadmapsInitializer dbRoadmapsInitializer;
    private final DBAdminParamInitializer dbAdminParamInitializer;
    private final DBCaravanInitializer dbCaravanInitializer;

    @PostConstruct
    public void initDb() {
        solveStyles();
        solveRoadmap();
        solveSmiles();
        solveAdminConstants();
        solveCaravan();
    }

    private void solveStyles() {
        dbStylesInitializer.initStyles();
    }

    private void solveRoadmap() {
        dbRoadmapsInitializer.initRoadmaps();
    }

    private void solveSmiles() {
        dbSmilesInitializer.initSmiles();
    }

    private void solveAdminConstants() {
        dbAdminParamInitializer.checkAllAdminSettings();
    }

    private void solveCaravan() {
        dbCaravanInitializer.startCaravan();
    }
}
