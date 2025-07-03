package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAction;

import java.util.*;

public class SanctuaryTrainer extends AbstractCardSelectionTrainer {
    private static final String LAST_HEAL = "remaining heals: 1";

    @Override
    protected String getTextTrigger() {
        return "sanctuary healing";
    }

    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;
            if (step.reward <= 0) continue;

            CardSelectionAction action = (CardSelectionAction) step.action;

            // Add positives for chosen cards
            addLabeledPoints(data, action.getChosenBlueprintIds(), action.getWoundsOnChosen(), step.state, 1);

            // Add negatives for not chosen only if this is the last heal decision
            if (step.decision.getText().contains(LAST_HEAL)) {
                addLabeledPoints(data, action.getNotChosenBlueprintIds(), action.getWoundsOnNotChosen(), step.state, 0);
            }
        }

        return data;
    }
}
