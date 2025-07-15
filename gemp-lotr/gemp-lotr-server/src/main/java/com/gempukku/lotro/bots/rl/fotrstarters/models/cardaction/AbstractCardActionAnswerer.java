package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

import com.gempukku.lotro.bots.rl.DecisionAnswerer;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCardActionAnswerer implements DecisionAnswerer {

    protected abstract String getTextTrigger();
    protected abstract AbstractPlayUseCardTrainer getPlayTrainer();
    protected abstract AbstractPlayUseCardTrainer getUseTrainer();
    protected abstract AbstractPlayUseCardTrainer getHealTrainer();
    protected abstract AbstractPlayUseCardTrainer getTransferTrainer();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return getPlayTrainer().appliesTo(gameState, decision, playerName) ||
                (getUseTrainer() != null && getUseTrainer().appliesTo(gameState, decision, playerName)) ||
                (getTransferTrainer() != null && getTransferTrainer().appliesTo(gameState, decision, playerName)) ||
                (getHealTrainer() != null && getHealTrainer().appliesTo(gameState, decision, playerName));
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        List<AbstractPlayUseCardTrainer> trainers = new ArrayList<>();
        trainers.add(getPlayTrainer());
        trainers.add(getUseTrainer());
        trainers.add(getHealTrainer());
        trainers.add(getTransferTrainer());
        List<String> answers = new ArrayList<>();
        for (AbstractPlayUseCardTrainer trainer : trainers) {
            if (trainer != null && trainer.appliesTo(gameState, decision, playerName)) {
                answers.add(trainer.getAnswer(gameState, decision, playerName, features, modelRegistry));
            }
        }

        return answers.stream().filter(s -> !s.isEmpty()).findFirst().orElse("");
    }
}
