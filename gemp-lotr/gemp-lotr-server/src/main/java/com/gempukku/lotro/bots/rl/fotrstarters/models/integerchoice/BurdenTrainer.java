package com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice;


public class BurdenTrainer extends AbstractIntegerTrainer {
    private static final int MAX_BURDENS_TO_BID = 9;

    @Override
    protected String getTextTrigger() {
        return "burdens to bid";
    }

    public int getMaxChoice() {
        return MAX_BURDENS_TO_BID;
    }
}
