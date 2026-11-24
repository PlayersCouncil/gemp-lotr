package com.gempukku.lotro.bots;

import com.gempukku.lotro.bots.rl.fotrstarters.FotrStarterBot;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStartersRLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelIO;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.LiveScenario;
import smile.classification.SoftClassifier;

import java.io.File;
import java.util.Random;

/**
 * Gemp's bot answering for a scenario's opponent seat (opponent.fallback: bot), where no step, standing order or
 * author answers.  It is the bot of an ordinary bot game (the FotR starter bot and its models), with two differences
 * that a scenario needs:
 * <ul>
 *     <li>It plays as whatever player the seat is (the bot of an ordinary game is tied to the name ~bot), in whichever
 *     role the seat has at the time: the bot works out from the game whether it is the Free Peoples player.</li>
 *     <li>Its answer is a function of the game and the seed it is given, and nothing else: a fresh bot for each
 *     decision (so nothing carries over from the decisions before, which a game built again from its journal never
 *     asked it), its chance from that seed.  The models themselves choose without chance; chance comes in only
 *     where the bot has no model for a decision and answers at random.</li>
 * </ul>
 * The answer goes to the game the way a scripted opponent's does (LiveScenario's Host), so the journal, the pause
 * for the person to read a step, taking back and the authoring tool all see it as one of the opponent's answers.
 */
public final class ScenarioBot implements LiveScenario.Bot {
    private final ModelRegistry _models;

    public ScenarioBot(ModelRegistry models) {
        _models = models;
    }

    /**
     * The bot with the models in a folder (the repository's bot-models), for a test or a tool; a model that is not
     * there is left out, and the bot answers that kind of decision at random.  The card features the models read
     * have to be set up already (CardFeatures.init).
     */
    public static ScenarioBot fromFolder(File folder) {
        ModelRegistry models = new ModelRegistry();
        for (Trainer trainer : BotService.trainers()) {
            SoftClassifier<double[]> model = ModelIO.loadModel(folder, trainer.getClass());
            if (model != null)
                models.registerModel(trainer.getClass(), model);
        }
        return new ScenarioBot(models);
    }

    @Override
    public String answer(LotroGame game, String player, AwaitingDecision decision, long seed) {
        FotrStarterBot bot = new FotrStarterBot(new FotrStartersRLGameStateFeatures(), player, _models, null, new Random(seed));
        return bot.chooseAction(game.getGameState(), decision, new AssignmentLegality(game));
    }
}
