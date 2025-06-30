package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAction;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.*;

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
            if (decision.getText().contains("another move")) {
                SoftClassifier<double[]> model = modelRegistry.getAnotherMoveModel();
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
        } else if (decision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)) {
            SoftClassifier<double[]> model = modelRegistry.getCardSelectionModel();
            if (model != null) {
                Map<String, String[]> params = decision.getDecisionParameters();
                int min = Integer.parseInt(params.get("min")[0]);
                int max = Integer.parseInt(params.get("max")[0]);
                String[] cardIds = params.get("cardId");

                double[] stateVector = features.extractFeatures(gameState, decision, getName());

                List<String> allBlueprints = FotrStartersRLGameStateFeatures.getBlueprints();
                List<ScoredBlueprint> candidates = new ArrayList<>();

                for (String cardId : cardIds) {
                    String blueprint = gameState.getBlueprintId(Integer.parseInt(cardId));
                    int index = allBlueprints.indexOf(blueprint);
                    if (index == -1)
                        continue;

                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + 1);
                    extended[stateVector.length] = index;

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    candidates.add(new ScoredBlueprint(blueprint, probs[1]));
                }

                // Sort by score descending
                candidates.sort(Comparator.comparingDouble(sb -> -sb.score));

                // Apply threshold (e.g. keep only cards with score >= 0.6)
                double threshold = 0.6;
                List<String> picked = candidates.stream()
                        .filter(c -> c.score >= threshold)
                        .limit(max)
                        .map(c -> c.blueprint)
                        .toList();

                // Enforce min bound
                if (picked.size() < min) {
                    picked = candidates.subList(0, Math.min(max, candidates.size()))
                            .stream().map(c -> c.blueprint).toList();
                }

                return new CardSelectionAction(picked.toArray(new String[0]))
                        .toDecisionString(decision, gameState);
            }
        }
        return super.chooseAction(gameState, decision);
    }

    private record ScoredBlueprint(String blueprint, double score) {
    }
}
