package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.common.Zone;

public class DiscardFromPlayTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "discard";
    }

    @Override
    protected boolean useNotChosen() {
        return true;
    }

    @Override
    protected String getZoneString() {
        return Zone.FREE_CHARACTERS.getHumanReadable();
    }
}
