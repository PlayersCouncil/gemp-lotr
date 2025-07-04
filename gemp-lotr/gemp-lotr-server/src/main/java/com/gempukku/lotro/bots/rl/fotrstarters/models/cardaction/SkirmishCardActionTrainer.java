package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

public class SkirmishCardActionTrainer extends AbstractCardActionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Choose action to play or Pass";
    }
}
