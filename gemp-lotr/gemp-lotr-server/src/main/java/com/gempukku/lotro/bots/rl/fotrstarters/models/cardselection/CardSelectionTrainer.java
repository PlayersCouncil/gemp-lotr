package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStartersRLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.Trainer;
import com.gempukku.lotro.bots.rl.semanticaction.CardSelectionAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.*;

public class CardSelectionTrainer implements Trainer {
    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();
        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            CardSelectionAction action = (CardSelectionAction) step.action;
            Set<String> selectedBlueprints = new HashSet<>(action.getChosenBlueprintIds());

            List<String> blueprintList = FotrStartersRLGameStateFeatures.getBlueprints();

            for (int i = 0; i < blueprintList.size(); i++) {
                String blueprintId = blueprintList.get(i);
                double blueprintPresent = step.state[934 + i + 1]; // card selection decision offset; +1 to skip the "is card decision" flag

                // Skip blueprint if it's not part of the current card selection decision
                if (blueprintPresent == 0.0)
                    continue;

                double[] blueprintState = Arrays.copyOf(step.state, step.state.length + 1);
                blueprintState[step.state.length] = i;

                if (selectedBlueprints.contains(blueprintId)) {
                    int label = step.reward > 0 ? 1 : 0;
                    data.add(new LabeledPoint(label, blueprintState));
                }
            }
        }
        return data;
    }

    @Override
    public SoftClassifier<double[]> trainWithPoints(List<LabeledPoint> points) {
        double[][] x = points.stream().map(LabeledPoint::x).toArray(double[][]::new);
        int[] y = points.stream().mapToInt(LabeledPoint::y).toArray();

        return LogisticRegression.fit(x, y);
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
            return step.action instanceof CardSelectionAction;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(CardSelectionTrainer.class);
    }
}
