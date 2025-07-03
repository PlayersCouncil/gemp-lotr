package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

// Maybe will need AbstractBinaryChoiceTrainer in the future
public abstract class AbstractMultipleChoiceTrainer extends AbstractTrainer {
    protected abstract String getTextTrigger();         // e.g. "mulligan", "another move"
    protected abstract String getPositiveOption();      // e.g. "Yes", "Go first"

    protected List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();
        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            String chosen = ((MultipleChoiceAction) step.action).getChosenOption();
            boolean chosePositive = chosen.equalsIgnoreCase(getPositiveOption());

            int label = step.reward > 0 ? (chosePositive ? 1 : 0) : (chosePositive ? 0 : 1);
            data.add(new LabeledPoint(label, step.state));
        }
        return data;
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;
        if (!(step.action instanceof MultipleChoiceAction))
            return false;

        return step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }
}
