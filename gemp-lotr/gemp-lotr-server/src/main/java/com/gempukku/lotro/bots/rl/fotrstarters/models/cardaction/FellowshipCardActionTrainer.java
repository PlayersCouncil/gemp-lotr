package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

public class FellowshipCardActionTrainer extends AbstractCardActionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Play Fellowship action or Pass";
    }
}
