package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

public class ShadowCardActionTrainer extends AbstractCardActionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Play Shadow action or Pass";
    }
}
