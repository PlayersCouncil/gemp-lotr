package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

public class ExertTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "to exert";
    }

    @Override
    protected boolean useNotChosen() {
        return true;
    }
}
