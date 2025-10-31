package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

public class ReconcileTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "reconcile";  // or "reconcile" if you want case-insensitive matching
    }

    @Override
    protected boolean useNotChosen() {
        return true;
    }
}
