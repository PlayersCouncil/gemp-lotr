package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.PlayEventAction;

public class PlayEventResult extends PlayCardResult {
    private boolean _eventCancelled;
    private final PlayEventAction _action;
    private final boolean _requiresRanger;

    public PlayEventResult(String performingPlayerId, PlayEventAction action, Zone playedFrom, PhysicalCard playedCard, boolean requiresRanger, boolean paidToil) {
        super(performingPlayerId, playedFrom, playedCard, null, null, paidToil);
        _action = action;
        _requiresRanger = requiresRanger;
    }

    public boolean isRequiresRanger() {
        return _requiresRanger;
    }

    public void cancelEvent() {
        _eventCancelled = true;
    }

    public boolean isEventCancelled() {
        return _eventCancelled;
    }

    public PlayEventAction getPlayEventAction() {
        return _action;
    }
}
