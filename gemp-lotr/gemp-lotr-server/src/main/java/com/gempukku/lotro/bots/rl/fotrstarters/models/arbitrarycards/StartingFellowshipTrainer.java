package com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards;

public class StartingFellowshipTrainer extends AbstractArbitraryCardsTrainer {
    @Override
    protected String getTextTrigger() {
        return "starting fellowship";
    }

    @Override
    protected boolean useNotChosen() {
        return true;
    }
}
