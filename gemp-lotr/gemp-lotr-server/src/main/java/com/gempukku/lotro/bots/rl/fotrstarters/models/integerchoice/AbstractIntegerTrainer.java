package com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.semanticaction.IntegerChoiceAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIntegerTrainer extends AbstractTrainer {

    protected abstract String getTextTrigger(); // e.g., "burdens to bid"
    public abstract int getMaxChoice();

    protected List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            if (step.reward <= 0) continue; // Only learn from positively rewarded steps

            int chosen = ((IntegerChoiceAction) step.action).getValue();
            data.add(new LabeledPoint(chosen, step.state));
        }

        return data;
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return step.decision.getDecisionType() == AwaitingDecisionType.INTEGER &&
                step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase()) &&
                step.action instanceof IntegerChoiceAction;
    }
}
