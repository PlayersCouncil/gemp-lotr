package com.gempukku.lotro.simulation;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.time.Duration;
import java.time.ZonedDateTime;

public class SimulationService {
    private final LotroCardBlueprintLibrary library;
    private final LotroFormatLibrary formatLibrary;

    public SimulationService(LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary) {
        this.library = library;
        this.formatLibrary = formatLibrary;

        startRandomFotrStartersSimulation();
    }

    public void startRandomFotrStartersSimulation() {
        SimulationRunner simulationRunner = new SimpleBatchSimulationRunner(
                new FotrStartersSimulation(library, formatLibrary),
                new RandomDecisionBot("bot1"),
                new RandomDecisionBot("bot2"),
                1000);

        ZonedDateTime start = DateUtils.Now();
        SimulationStats simulationStats = simulationRunner.run();
        System.out.println(DateUtils.HumanDuration(Duration.between(DateUtils.Now(), start).abs()));

        System.out.println(simulationStats);
    }
}
