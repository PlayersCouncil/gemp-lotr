package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

public class RegroupCardActionTrainer extends AbstractCardActionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Play Regroup action or Pass";
    }
}
