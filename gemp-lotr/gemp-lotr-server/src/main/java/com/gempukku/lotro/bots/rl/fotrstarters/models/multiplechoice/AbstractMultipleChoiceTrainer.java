package com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;

// Maybe will need AbstractBinaryChoiceTrainer in the future
public abstract class AbstractMultipleChoiceTrainer extends AbstractTrainer {
    protected abstract String getTextTrigger();         // e.g. "mulligan", "another move"
    protected abstract String getPositiveOption();      // e.g. "Yes", "Go first"
    protected abstract String getNegativeOption();      // e.g. "No", "Go second"

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        double[] probs = new double[2];
        model.predict(stateVector, probs);
        return probs[1] > probs[0] ? new MultipleChoiceAction(getPositiveOption()).toDecisionString(decision, gameState) : new MultipleChoiceAction(getNegativeOption()).toDecisionString(decision, gameState);
    }

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
