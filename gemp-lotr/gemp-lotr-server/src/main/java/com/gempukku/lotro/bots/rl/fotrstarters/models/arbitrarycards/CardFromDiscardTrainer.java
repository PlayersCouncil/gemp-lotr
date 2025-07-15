package com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards;

public class CardFromDiscardTrainer extends AbstractArbitraryCardsTrainer {
    @Override
    protected String getTextTrigger() {
        return "choose card from discard";
    }

    @Override
    protected boolean useNotChosen() {
        return true;
    }
}
