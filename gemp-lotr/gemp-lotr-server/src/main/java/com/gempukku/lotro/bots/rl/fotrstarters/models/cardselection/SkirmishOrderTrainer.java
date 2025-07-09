package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStarterBot;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAssignedAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SkirmishOrderTrainer extends AbstractTrainer {
    private static final String SKIRMISH = "next skirmish to resolve";

    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            CardSelectionAssignedAction action = (CardSelectionAssignedAction) step.action;

            for (int i = 0; i < action.getChosenBlueprintIds().size(); i++) {
                try {
                    double[] blueprintVector = CardFeatures.getFpAssignedCardFeatures(action.getChosenBlueprintIds().get(i), action.getWoundsOnChosen().get(i), action.getMinionsOnChosen().get(i), action.getStrengthOfMinionsOnChosen().get(i));
                    double[] extended = Arrays.copyOf(step.state, step.state.length + blueprintVector.length);
                    System.arraycopy(blueprintVector, 0, extended, step.state.length, blueprintVector.length);

                    int label = step.reward > 0 ? 1 : 0;

                    data.add(new LabeledPoint(label, extended));
                } catch (CardNotFoundException ignore) {

                }
            }
        }

        return data;
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return step.decision.getDecisionType() == AwaitingDecisionType.CARD_SELECTION
                && step.decision.getText().toLowerCase().contains(SKIRMISH.toLowerCase())
                && step.action instanceof CardSelectionAssignedAction;
    }

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;
        return decision.getText().toLowerCase().contains(SKIRMISH.toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

        SoftClassifier<double[]> model = modelRegistry.getModel(SkirmishOrderTrainer.class);
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (String physicalId : cardIds) {
            try {
                for (Assignment assignment : gameState.getAssignments()) {
                    if (assignment.getFellowshipCharacter().getCardId() == Integer.parseInt(physicalId)) {
                        String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                        int wounds = 0;
                        for (PhysicalCard physicalCard : gameState.getInPlay()) {
                            if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                                wounds = gameState.getWounds(physicalCard);
                            }
                        }
                        double[] cardVector = CardFeatures.getFpAssignedCardFeatures(blueprintId, wounds, assignment.getShadowCharacters().size(), assignment.getShadowCharacters().stream().mapToInt(value -> value.getBlueprint().getStrength()).sum());
                        double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                        System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                        double[] probs = new double[2];
                        model.predict(extended, probs);
                        scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of choosing this card
                    }
                }

            } catch (CardNotFoundException ignored) {

            }
        }

        // Find the card with the highest probability
        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        ScoredCard best = scoredCards.get(0);
        return best.cardId;
    }
}
