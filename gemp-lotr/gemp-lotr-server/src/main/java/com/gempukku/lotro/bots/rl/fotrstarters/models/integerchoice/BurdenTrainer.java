package com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice;


import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.Random;

public class BurdenTrainer extends AbstractIntegerTrainer {
    private static final int MAX_BURDENS_TO_BID = 9;

    @Override
    protected String getTextTrigger() {
        return "burdens to bid";
    }

    public int getMaxChoice() {
        return MAX_BURDENS_TO_BID;
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        // Bid randomly 0-4 since now we cannot guarantee frodo as ring bearer
        Random random = new Random();
        int randomNumber = random.nextInt(5);
        return String.valueOf(randomNumber);
    }
}
