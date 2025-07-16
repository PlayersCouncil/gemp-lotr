package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;


public class AttachItemTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Choose target to attach to";
    }
}
