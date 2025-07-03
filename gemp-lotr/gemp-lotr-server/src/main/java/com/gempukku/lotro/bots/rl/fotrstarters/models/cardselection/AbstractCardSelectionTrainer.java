package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractCardSelectionTrainer extends AbstractTrainer {

    protected abstract String getTextTrigger();
    protected boolean useNotChosen() { return false; }
    protected String getZoneString() { return null; }

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;
        if (!decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase()))
            return false;

        String requiredZone = getZoneString();
        if (requiredZone == null) {
            return true;
        }

        String zoneOfAllCards = null;
            String[] allChoices = decision.getDecisionParameters().get("cardId");
            for (String choice : allChoices) {
                for (PhysicalCard physicalCard : gameState.getAllCards()) {
                    if (physicalCard.getCardId() == Integer.parseInt(choice)) {
                        String zoneOfThisCard = physicalCard.getZone().getHumanReadable();
                        if (zoneOfAllCards == null) {
                            zoneOfAllCards = zoneOfThisCard;
                        }
                        if (!zoneOfAllCards.equals(zoneOfThisCard)) {
                            zoneOfAllCards = "zones mismatched";
                        }
                    }
                }
            }
        return requiredZone.equals(zoneOfAllCards);
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (String physicalId : cardIds) {
            try {
                String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                int wounds = 0;
                for (PhysicalCard physicalCard : gameState.getAllCards()) {
                    if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                        wounds = gameState.getWounds(physicalCard);
                    }
                }
                double[] cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                scoredCards.add(new ScoredCard(physicalId, probs[1]));
            } catch (CardNotFoundException ignored) {

            }
        }

        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        List<String> sortedIds = new ArrayList<>();
        scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
        return String.join(",", sortedIds.subList(0, max));
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;

        if (!(step.action instanceof CardSelectionAction csa))
            return false;

        if (!step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase()))
            return false;

        String requiredZone = getZoneString();
        return requiredZone == null || requiredZone.equals(csa.getZoneString());
    }

    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            CardSelectionAction action = (CardSelectionAction) step.action;

            if (step.reward > 0) {
                // Chosen: good
                addLabeledPoints(data, action.getChosenBlueprintIds(), action.getWoundsOnChosen(), step.state, 1);

                if (useNotChosen()) {
                    // Not chosen: bad
                    addLabeledPoints(data, action.getNotChosenBlueprintIds(), action.getWoundsOnNotChosen(), step.state, 0);
                }
            } else {
                // Chosen: bad
                addLabeledPoints(data, action.getChosenBlueprintIds(), action.getWoundsOnChosen(), step.state, 0);

                // Not chosen: ambiguous → skip
            }
        }

        return data;
    }

    protected void addLabeledPoints(List<LabeledPoint> data, List<String> blueprintIds, List<Integer> wounds,
                                  double[] state, int label) {
        for (int i = 0; i < blueprintIds.size(); i++) {
            try {
                double[] cardVector = getCardVector(blueprintIds.get(i), wounds.get(i));
                double[] extended = Arrays.copyOf(state, state.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, state.length, cardVector.length);
                data.add(new LabeledPoint(label, extended));
            } catch (CardNotFoundException ignore) {
            }
        }
    }

    protected double[] getCardVector(String blueprintId, int wounds) throws CardNotFoundException {
        return CardFeatures.getCardFeatures(blueprintId, wounds);
    }
}
