package com.gempukku.lotro.bots.random;

import com.gempukku.lotro.bots.rl.LearningBotPlayer;
import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.ReplayBuffer;
import com.gempukku.lotro.bots.rl.semanticaction.*;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public class RandomLearningBot extends RandomDecisionBot implements LearningBotPlayer {
    private final RLGameStateFeatures features;
    private final List<LearningStep> episodeSteps = new ArrayList<>();
    private final ReplayBuffer replayBuffer;


    public RandomLearningBot(RLGameStateFeatures features, String playerId, ReplayBuffer replayBuffer) {
        super(playerId);
        this.features = features;
        this.replayBuffer = replayBuffer;
    }

    @Override
    public String chooseAction(GameState gameState, AwaitingDecision decision) {
        double[] stateVector = features.extractFeatures(gameState, decision, getName());

        SemanticAction action = pickSemanticAction(decision, gameState);

        // Store temporarily — reward comes later
        episodeSteps.add(new LearningStep(stateVector, action, getName(), decision));

        return action.toDecisionString(decision, gameState);
    }

    private SemanticAction pickSemanticAction(AwaitingDecision decision, GameState gameState) {
        String action = super.chooseAction(gameState, decision);

        return switch (decision.getDecisionType()) {
            case INTEGER -> new IntegerChoiceAction(Integer.parseInt(action));
            case MULTIPLE_CHOICE -> new MultipleChoiceAction(action, decision);
            case ARBITRARY_CARDS -> new ChooseFromArbitraryCardsAction(action, decision);
            case CARD_ACTION_CHOICE -> new CardActionChoiceAction(action, decision);
            case ACTION_CHOICE -> new ActionChoiceAction(action, decision);
            case CARD_SELECTION -> new CardSelectionAction(action, decision, gameState);
            case ASSIGN_MINIONS -> new AssignMinionsAction(action, gameState);
        };
    }

    @Override
    public void observe(GameState gameState, AwaitingDecision decision, String playerId, String chosenAction, double reward, boolean terminal) {
        // At this stage, we assume `chosenAction` was already used to resolve game state,
        // and we just want to record the final outcome of this decision if needed.
        // Right now, we store full episode and use reward only in `endEpisode`.
    }

    @Override
    public void endEpisode(double reward) {
        episodeSteps.forEach(learningStep -> learningStep.reward = reward);
        replayBuffer.addEpisode(new ArrayList<>(episodeSteps));
        episodeSteps.clear();
    }
}
