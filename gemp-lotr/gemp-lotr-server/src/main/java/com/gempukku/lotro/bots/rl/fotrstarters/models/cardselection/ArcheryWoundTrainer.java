package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;


public class ArcheryWoundTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "assign archery wound to";
    }
}
