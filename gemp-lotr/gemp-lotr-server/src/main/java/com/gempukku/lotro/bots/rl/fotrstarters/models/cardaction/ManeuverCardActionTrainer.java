package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

public class ManeuverCardActionTrainer extends AbstractCardActionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Play Maneuver action or Pass";
    }
}
