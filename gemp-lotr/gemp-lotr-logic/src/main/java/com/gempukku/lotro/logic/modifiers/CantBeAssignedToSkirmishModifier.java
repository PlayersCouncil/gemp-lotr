package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;

public class CantBeAssignedToSkirmishModifier extends AbstractModifier {
    private final String _bannedPlayer;

    public CantBeAssignedToSkirmishModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        this(source, condition, null, affectFilter);
    }

    public CantBeAssignedToSkirmishModifier(PhysicalCard source, Condition condition, String bannedPlayer, Filterable affectFilter) {
        super(source, "Can't be assigned to skirmish", affectFilter, condition, ModifierEffect.ASSIGNMENT_MODIFIER);
        this._bannedPlayer = bannedPlayer;
    }

    @Override
    public boolean isPreventedFromBeingAssignedToSkirmish(LotroGame game, Side sidePlayer, PhysicalCard card) {
        if(_bannedPlayer == null || GameUtils.getSide(game, _bannedPlayer) == sidePlayer)
            return true;

        return false;
    }
}
