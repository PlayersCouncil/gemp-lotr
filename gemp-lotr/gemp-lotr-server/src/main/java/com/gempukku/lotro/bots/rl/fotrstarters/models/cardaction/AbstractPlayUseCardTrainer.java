package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.AbstractTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.LabeledPoint;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.CardActionChoiceAction;
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

public abstract class AbstractPlayUseCardTrainer extends AbstractTrainer {
    private static final String PLAY = "Play";
    private static final String USE = "Use";
    private static final String HEAL = "Heal";
    private static final String TRANSFER = "Transfer";

    protected String getAllowedActionText() {
        if (isPlayTrainer()) {
            return PLAY;
        } else if (isUseTrainer()) {
            return USE;
        } else if (isHealTrainer()) {
            return HEAL;
        } else if (isTransferTrainer()) {
            return TRANSFER;
        } else {
            throw new IllegalStateException("Trainer is of unknown type.");
        }
    }

    protected abstract String getTextTrigger();
    protected abstract boolean isPlayTrainer();
    protected abstract boolean isUseTrainer();
    protected abstract boolean isTransferTrainer();
    protected abstract boolean isHealTrainer();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        String[] actionIds = decision.getDecisionParameters().get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            // No actions available: degenerate decision
            return false;
        }

        if (decision.getDecisionType() != AwaitingDecisionType.CARD_ACTION_CHOICE)
            return false;

        if (!decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase())) {
            return false;
        }

        return Arrays.stream(decision.getDecisionParameters().get("actionText")).anyMatch(s -> s.startsWith(getAllowedActionText()));
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.CARD_ACTION_CHOICE)
            return false;

        if (!(step.action instanceof CardActionChoiceAction caca))
            return false;

        String[] actionIds = step.decision.getDecisionParameters().get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            // No actions available: degenerate decision
            return false;
        }

        if (!step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase())) {
            return false;
        }

        return Arrays.stream(step.decision.getDecisionParameters().get("actionText")).anyMatch(s -> s.startsWith(getAllowedActionText())) && (caca.getActionText() == null || caca.getActionText().startsWith(getAllowedActionText()));
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();
        List<String> actions = Arrays.stream(decision.getDecisionParameters().get("actionText")).toList();

        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (String physicalId : cardIds) {
            try {
                if (!actions.get(cardIds.indexOf(physicalId)).startsWith(getAllowedActionText())) {
                    continue;
                }

                String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                int wounds = 0;
                for (PhysicalCard physicalCard : gameState.getInPlay()) {
                    if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                        wounds = gameState.getWounds(physicalCard);
                    }
                }
                double[] cardVector;
                if (!isTransferTrainer()) {
                    cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                } else {
                    cardVector = null;
                    for (PhysicalCard physicalCard : gameState.getInPlay()) {
                        if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                            cardVector = CardFeatures.getItemCardFeatures(blueprintId, physicalCard.getAttachedTo().getBlueprintId(), wounds);
                            break;
                        }
                    }
                    if (cardVector == null) {
                        continue;
                    }
                }
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                scoredCards.add(new ScoredCard(physicalId, probs[1]));
            } catch (CardNotFoundException ignored) {

            }
        }

        // Add pass option
        try {
            double[] cardVector;
            if (!isTransferTrainer()) {
                cardVector = CardFeatures.getCardFeatures(CardFeatures.PASS, 0);
            } else {
                cardVector = CardFeatures.getItemCardFeatures(CardFeatures.PASS, CardFeatures.PASS, 0);
            }
            double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
            System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

            double[] probs = new double[2];
            model.predict(extended, probs);
            scoredCards.add(new ScoredCard(CardFeatures.PASS, probs[1]));
        } catch (CardNotFoundException e) {
            e.printStackTrace();
        }

        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        if (scoredCards.get(0).cardId.equals(CardFeatures.PASS)) {
            return "";
        }
        int chosenIndex = cardIds.indexOf(scoredCards.get(0).cardId);
        return String.valueOf(chosenIndex);
    }



    @Override
    public List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            CardActionChoiceAction action = (CardActionChoiceAction) step.action;

            if (step.reward > 0) {
                // Chosen: good
                if (action.getSourceBlueprint() != null) {
                    addLabeledPoints(data, action.getSourceBlueprint(), action.getWoundsOnSource(), action.getHolderBlueprint(), step.state, 1);
                } else {
                    // Passed and it was good
                    addLabeledPoints(data, CardFeatures.PASS, action.getWoundsOnSource(), CardFeatures.PASS, step.state, 1);
                }
            } else {
                // Chosen: bad
                if (action.getSourceBlueprint() != null) {
                    addLabeledPoints(data, action.getSourceBlueprint(), action.getWoundsOnSource(), action.getHolderBlueprint(), step.state, 0);
                } else {
                    // Passed and it was bad, should have played
                    addLabeledPoints(data, CardFeatures.PASS, action.getWoundsOnSource(), CardFeatures.PASS, step.state, 0);
                }
            }
        }

        return data;
    }

    protected void addLabeledPoints(List<LabeledPoint> data, String blueprintId, int wounds, String holderBlueprintId,
                                    double[] state, int label) {
        try {
            double[] cardVector;
            if (!isTransferTrainer()) {
                cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
            } else {
                cardVector = CardFeatures.getItemCardFeatures(blueprintId, holderBlueprintId, wounds);
            }
            double[] extended = Arrays.copyOf(state, state.length + cardVector.length);
            System.arraycopy(cardVector, 0, extended, state.length, cardVector.length);
            data.add(new LabeledPoint(label, extended));
        } catch (CardNotFoundException ignore) {
        }

    }
}
