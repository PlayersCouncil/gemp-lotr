package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.List;
import java.util.Map;

public class FotrStarterBot extends RandomDecisionBot implements BotPlayer {
    private final RLGameStateFeatures features;
    private final ModelRegistry modelRegistry;

    public FotrStarterBot(RLGameStateFeatures features, String playerId, ModelRegistry modelRegistry) {
        super(playerId);
        this.features = features;
        this.modelRegistry = modelRegistry;
    }

    @Override
    public String chooseAction(GameState gameState, AwaitingDecision decision) {
        if (decision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE)) {
            String[] options = decision.getDecisionParameters().get("results");
            if (List.of(options).contains("Go first")) {
                SoftClassifier<double[]> model = modelRegistry.getGoFirstModel();
                double[] stateVector = features.extractFeatures(gameState, decision, getName());
                if (model != null) {
                    double[] probs = new double[2];
                    model.predict(stateVector, probs);
                    return probs[1] > probs[0] ? new MultipleChoiceAction("Go first").toDecisionString(decision, gameState) : new MultipleChoiceAction("Go second").toDecisionString(decision, gameState);
                }
            }
            if (decision.getText().contains("mulligan")) {
                SoftClassifier<double[]> model = modelRegistry.getMulliganModel();
                double[] stateVector = features.extractFeatures(gameState, decision, getName());
                if (model != null) {
                    double[] probs = new double[2];
                    model.predict(stateVector, probs);
                    return probs[1] > probs[0] ? new MultipleChoiceAction("Yes").toDecisionString(decision, gameState) : new MultipleChoiceAction("No").toDecisionString(decision, gameState);
                }
            }
        } else if (decision.getDecisionType().equals(AwaitingDecisionType.INTEGER)) {
            SoftClassifier<double[]> model = modelRegistry.getIntegerModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            if (model != null) {
                Map<String, String[]> params = decision.getDecisionParameters();
                int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
                int predictedValue = model.predict(stateVector);
                int value = predictedValue + min;
                if (decision.getDecisionParameters().containsKey("max")) {
                    int max = Integer.parseInt(params.get("max")[0]);
                    value = Math.min(value, max);
                }
                return String.valueOf(value);
            }
        }
        return super.chooseAction(gameState, decision);
    }
}
