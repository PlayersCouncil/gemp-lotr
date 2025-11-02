package com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.ChooseFromArbitraryCardsAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractArbitraryCardsTrainer extends AbstractTrainer {

    protected abstract String getTextTrigger();
    protected boolean useNotChosen() { return false; }

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
            return false;
        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> selectableIds = new ArrayList<>();
        List<String> selectableBlueprints = new ArrayList<>();
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();
        List<String> blueprints = Arrays.stream(decision.getDecisionParameters().get("blueprintId")).toList();
        List<String> selectable = Arrays.stream(decision.getDecisionParameters().get("selectable")).toList();

        for (int i = 0; i < cardIds.size() && i < selectable.size(); i++) {
            if (Boolean.parseBoolean(selectable.get(i))) {
                selectableIds.add(cardIds.get(i));
                selectableBlueprints.add(blueprints.get(i));
            }
        }

        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (int i = 0; i < selectableBlueprints.size(); i++) {
            try {
                String blueprintId = selectableBlueprints.get(i);
                double[] cardVector = getCardVector(blueprintId);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                scoredCards.add(new ScoredCard(selectableIds.get(i), probs[1]));
            } catch (CardNotFoundException ignored) {

            }
        }

        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        List<String> sortedIds = new ArrayList<>();
        scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
        // Always chooses max cards, never passes
        return String.join(",", sortedIds.subList(0, max));
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
            return false;

        if (!(step.action instanceof ChooseFromArbitraryCardsAction))
            return false;

        return step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            ChooseFromArbitraryCardsAction action = (ChooseFromArbitraryCardsAction) step.action;

            if (step.reward > 0) {
                // Chosen: good
                addLabeledPoints(data, action.getChosenBlueprintIds(), step.state, 1);

                if (useNotChosen()) {
                    // Not chosen: bad
                    addLabeledPoints(data, action.getNotChosenBlueprintIds(), step.state, 0);
                }
            } else {
                // Chosen: bad
                addLabeledPoints(data, action.getChosenBlueprintIds(), step.state, 0);

                // Not chosen: ambiguous → skip
            }
        }

        return data;
    }

    protected void addLabeledPoints(List<LabeledPoint> data, List<String> blueprintIds,
                                    double[] state, int label) {
        for (String blueprintId : blueprintIds) {
            try {
                double[] cardVector = getCardVector(blueprintId);
                double[] extended = Arrays.copyOf(state, state.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, state.length, cardVector.length);
                data.add(new LabeledPoint(label, extended));
            } catch (CardNotFoundException ignore) {
                System.out.println(ignore.getMessage());
            }
        }
    }

    protected double[] getCardVector(String blueprintId) throws CardNotFoundException {
        return CardFeatures.getCardFeatures(blueprintId, 0);
    }
}
