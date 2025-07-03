package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAssignedAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.Arrays;
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
                && step.decision.getText().contains(SKIRMISH)
                && step.action instanceof CardSelectionAssignedAction;
    }
}
