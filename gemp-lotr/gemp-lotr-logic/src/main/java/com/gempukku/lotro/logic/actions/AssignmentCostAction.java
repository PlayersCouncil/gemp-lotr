package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Effect;

/**
 * The payment of a cost of assigning a minion to a skirmish (ExtraAssignmentCostModifier; built by
 * ModifiersQuerying.getAssignmentCosts).  Its costs are what the Free Peoples player pays; where a card makes the
 * assignment, the assignment is its one effect, and is not made unless every cost was carried out.
 *
 * Its source is the card that charges, whatever is making the assignment: a companion exerted to assign Morgul
 * Spawn is exerted by Morgul Spawn, not by the card whose action assigns it.  It is no trigger and no special
 * ability.
 */
public class AssignmentCostAction extends AbstractCostToEffectAction {
    private final PhysicalCard _chargedBy;

    public AssignmentCostAction(PhysicalCard chargedBy, String payingPlayer) {
        _chargedBy = chargedBy;
        setPerformingPlayer(payingPlayer);
    }

    @Override
    public Type getType() {
        return Type.OTHER;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _chargedBy;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _chargedBy;
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            Effect effect = getNextEffect();
            if (effect != null)
                return effect;
        }
        return null;
    }
}
