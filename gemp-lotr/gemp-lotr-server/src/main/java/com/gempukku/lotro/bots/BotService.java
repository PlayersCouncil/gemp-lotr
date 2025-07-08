package com.gempukku.lotro.bots;

import com.gempukku.lotro.bots.random.RandomLearningBot;
import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.ReplayBuffer;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStarterBot;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStartersLearningStepsPersistence;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStartersRLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.CardFromDiscardTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.StartingFellowshipTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.assignment.FpAssignmentTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.assignment.ShadowAssignmentTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction.*;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection.*;
import com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice.BurdenTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.AnotherMoveTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.GoFirstTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.MulliganTrainer;
import com.gempukku.lotro.bots.simulation.FotrStartersSimulation;
import com.gempukku.lotro.bots.simulation.SimpleBatchSimulationRunner;
import com.gempukku.lotro.bots.simulation.SimulationRunner;
import com.gempukku.lotro.bots.simulation.SimulationStats;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.DeckSerialization;
import com.gempukku.lotro.db.LoginInvalidException;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import smile.classification.SoftClassifier;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public class BotService {
    private static final boolean START_SIMULATIONS_AT_STARTUP = true;

    private final LotroCardBlueprintLibrary library;
    private final LotroFormatLibrary formatLibrary;
    private final PlayerDAO playerDAO;

    private final Random random = new Random();
    private final Map<String, List<BotWithDeck>> formatBotsMap = new HashMap<>();

    private final ReplayBuffer replayBuffer = new ReplayBuffer(100_000);
    private final ModelRegistry modelRegistry = new ModelRegistry();

    public BotService(LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary, PlayerDAO playerDAO) {
        this.library = library;
        CardFeatures.init(library);
        this.formatLibrary = formatLibrary;
        this.playerDAO = playerDAO;

        fillBotParticipantList();


        if (START_SIMULATIONS_AT_STARTUP) {
            runSelfPlayTrainingLoop(5, 1000);
        } else {
            System.out.println("training bot models at startup");

            // List of all trainer instances
            List<Trainer> trainers = List.of(
                    new GoFirstTrainer(),
                    new MulliganTrainer(),
                    new AnotherMoveTrainer(),
                    new BurdenTrainer(),
                    new ReconcileTrainer(),
                    new SanctuaryTrainer(),
                    new ArcheryWoundTrainer(),
                    new AttachItemTrainer(),
                    new SkirmishOrderTrainer(),
                    new HealTrainer(),
                    new DiscardFromHandTrainer(),
                    new ExertTrainer(),
                    new DiscardFromPlayTrainer(),
                    new PlayFromHandTrainer(),
                    new FallBackCardSelectionTrainer(),
                    new CardFromDiscardTrainer(),
                    new StartingFellowshipTrainer(),
                    new FellowshipCardActionTrainer(),
                    new ShadowCardActionTrainer(),
                    new ManeuverCardActionTrainer(),
                    new SkirmishCardActionTrainer(),
                    new RegroupCardActionTrainer(),
                    new OptionalResponsesCardActionTrainer(),
                    new ShadowAssignmentTrainer(),
                    new FpAssignmentTrainer()
            );

            FotrStartersLearningStepsPersistence persistence = new FotrStartersLearningStepsPersistence();

            for (Trainer trainer : trainers) {
                System.out.println("training '" + trainer.getClass().getSimpleName() + "' model");
                List<LearningStep> steps = persistence.load(trainer);
                SoftClassifier<double[]> model = trainer.train(steps);
                modelRegistry.registerModel(trainer.getClass(), model);
            }

            System.out.println("training done");
        }
    }

    private void startFotrStartersSimulation(BotPlayer b1, BotPlayer b2, int games) {
        System.out.println(games + " simulation started");
        SimulationRunner simulationRunner = new SimpleBatchSimulationRunner(
                new FotrStartersSimulation(library, formatLibrary), b1, b2, games);

        ZonedDateTime start = DateUtils.Now();
        SimulationStats simulationStats = simulationRunner.run();
        System.out.println(DateUtils.HumanDuration(Duration.between(DateUtils.Now(), start).abs()));

        System.out.println(simulationStats);
    }

    private void runSelfPlayTrainingLoop(int generations, int gamesPerGeneration) {
        System.out.println("Starting self-play training loop for " + generations + " generations");

        FotrStartersLearningStepsPersistence persistence = new FotrStartersLearningStepsPersistence();

        replayBuffer.addListener(10_000, buffer -> {
            persistence.save(buffer.sampleBatch(buffer.size()));
            replayBuffer.clear();
        });

        for (int generation = 0; generation <= generations; generation++) {
            System.out.println("=== Generation " + generation + " ===");

            // Step 1: Run simulations with current model
            if (generation == 0) {
                startFotrStartersSimulation(
                        new RandomLearningBot(new FotrStartersRLGameStateFeatures(), "~randomBot1", replayBuffer),
                        new RandomLearningBot(new FotrStartersRLGameStateFeatures(), "~randomBot2", replayBuffer),
                        gamesPerGeneration);
            } else {
                startFotrStartersSimulation(
                        new FotrStarterBot(new FotrStartersRLGameStateFeatures(), "~trainBot" + generation, modelRegistry, replayBuffer),
                        new RandomLearningBot(new FotrStartersRLGameStateFeatures(), "~randomBot", replayBuffer),
                        gamesPerGeneration / 4
                );
                startFotrStartersSimulation(
                        new FotrStarterBot(new FotrStartersRLGameStateFeatures(), "~trainBotOne" + generation, modelRegistry, replayBuffer),
                        new FotrStarterBot(new FotrStartersRLGameStateFeatures(), "~trainBotTwo" + generation, modelRegistry, replayBuffer),
                        gamesPerGeneration * 3 / 4
                );
            }

            // Step 2: Save data
            persistence.save(replayBuffer.sampleBatch(replayBuffer.size()));
            replayBuffer.clear();

            // Step 3: Aggregate all historical data and retrain
            // List of all trainer instances
            List<Trainer> trainers = List.of(
                    new GoFirstTrainer(),
                    new MulliganTrainer(),
                    new AnotherMoveTrainer(),
                    new BurdenTrainer(),
                    new ReconcileTrainer(),
                    new SanctuaryTrainer(),
                    new ArcheryWoundTrainer(),
                    new AttachItemTrainer(),
                    new SkirmishOrderTrainer(),
                    new HealTrainer(),
                    new DiscardFromHandTrainer(),
                    new ExertTrainer(),
                    new DiscardFromPlayTrainer(),
                    new PlayFromHandTrainer(),
                    new FallBackCardSelectionTrainer(),
                    new CardFromDiscardTrainer(),
                    new StartingFellowshipTrainer(),
                    new FellowshipCardActionTrainer(),
                    new ShadowCardActionTrainer(),
                    new ManeuverCardActionTrainer(),
                    new SkirmishCardActionTrainer(),
                    new RegroupCardActionTrainer(),
                    new OptionalResponsesCardActionTrainer(),
                    new ShadowAssignmentTrainer(),
                    new FpAssignmentTrainer()
            );

            for (Trainer trainer : trainers) {
                List<LearningStep> steps = persistence.load(trainer);
                SoftClassifier<double[]> model = trainer.train(steps);
                modelRegistry.registerModel(trainer.getClass(), model);
            }
        }

        System.out.println("Self-play training loop complete.");
    }

    public BotWithDeck getBotParticipant(LotroFormat lotroFormat) {
        if (!formatBotsMap.containsKey(lotroFormat.getCode())) {
            throw new IllegalArgumentException("This format is not supported.");
        }

        List<BotWithDeck> botList = formatBotsMap.get(lotroFormat.getCode());
        return botList.get(random.nextInt(botList.size()));
    }

    private void fillBotParticipantList() {
        List<BotWithDeck> fotrBots = new ArrayList<>();
        String aragornBotName = "~AragornBot";
        LotroDeck aragornStarter = DeckSerialization.buildDeckFromContents("Aragorn Starter",
                "1_290|1_2|1_320,1_327,1_340,1_346,1_349,1_351,1_355,1_358,1_361|1_365,1_365,1_92,1_94,1_94,1_97," +
                        "1_97,1_101,1_104,1_104,1_106,1_107,1_107,1_296,1_296,1_299,1_299,1_51,1_108,1_108,1_110,1_110,1_309," +
                        "1_311,1_116,1_116,1_116,1_117,1_117,1_117,1_121,1_121,1_121,1_133,1_133,1_141,1_141,1_145,1_150," +
                        "1_150,1_150,1_150,1_151,1_151,1_151,1_152,1_152,1_152,1_152,1_153,1_153,1_153,1_153,1_154,1_154," +
                        "1_154,1_157,1_157,1_158,1_158", "fotr_block", "Aragorn Starter");
        fotrBots.add(new BotWithDeck(new FotrStarterBot(new FotrStartersRLGameStateFeatures(), aragornBotName, modelRegistry, null), aragornStarter));

        String gandalfBotName = "~GandalfBot";
        LotroDeck gandalfStarter = DeckSerialization.buildDeckFromContents("Gandalf Starter",
                "1_290|1_2|1_326,1_331,1_337,1_345,1_349,1_350,1_353,1_359,1_360|1_70,1_97,1_286,1_286,1_286,1_37," +
                        "1_37,1_364,1_364,1_12,1_12,1_296,1_296,1_299,1_76,1_76,1_76,1_51,1_51,1_78,1_78,1_78,1_78,1_304," +
                        "1_304,1_84,1_312,1_26,1_26,1_86,1_168,1_168,1_168,1_176,1_176,1_176,1_176,1_177,1_178,1_178,1_178" +
                        ",1_178,1_179,1_179,1_179,1_180,1_180,1_180,1_180,1_181,1_181,1_181,1_187,1_187,1_187,1_191," +
                        "1_191,1_191,1_196,1_196", "fotr_block", "Gandalf Starter");
        fotrBots.add(new BotWithDeck(new FotrStarterBot(new FotrStartersRLGameStateFeatures(), gandalfBotName, modelRegistry, null), gandalfStarter));

        try {
            playerDAO.registerBot(aragornBotName);
            playerDAO.registerBot(gandalfBotName);

            formatBotsMap.put("fotr_block", fotrBots);
        } catch (LoginInvalidException e) {
            throw new IllegalStateException("Wrong bot name - must start with # character.");
        }
    }

    public class BotWithDeck {
        private BotPlayer botPlayer;
        private LotroDeck lotroDeck;

        public BotWithDeck(BotPlayer botPlayer, LotroDeck lotroDeck) {
            this.botPlayer = botPlayer;
            this.lotroDeck = lotroDeck;
        }

        public BotPlayer getBotPlayer() {
            return botPlayer;
        }

        public LotroDeck getLotroDeck() {
            return lotroDeck;
        }
    }
}
