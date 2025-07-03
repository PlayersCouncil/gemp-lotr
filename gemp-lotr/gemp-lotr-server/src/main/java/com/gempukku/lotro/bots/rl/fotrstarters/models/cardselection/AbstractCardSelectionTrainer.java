package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCardSelectionTrainer extends AbstractTrainer {

    protected abstract String getTextTrigger();
    protected boolean useNotChosen() { return false; }
    protected String getZoneString() { return null; }

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
