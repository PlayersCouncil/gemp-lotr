package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

import java.util.Map;
import java.util.Set;

/**
 * A cost the Free Peoples player pays to assign a minion to a skirmish: "the Free Peoples player must exert a
 * companion to assign this minion to a skirmish" (Morgul Spawn 7_200), "... must add a burden to assign Úlairë
 * Nertëa to skirmish a companion of the named race" (69_38).  The twin, for an assignment, of
 * AbstractExtraPlayCostModifier (Q-0221).
 *
 * It affects the minions it charges for, and may name the characters against whom it charges.  The cost is paid
 * ahead of the assignment, and the minion is then marked paid for (GameState.isAssignmentPaidFor; Q-0283): at
 * the start of the Free Peoples player's own assigning, where he is offered to pay for each such minion in turn
 * (AssignmentCostRule), and where a card has him assign, once the pair is chosen (AssignFpCharacterToSkirmish).
 * The game asks it two things.  Is the minion his to assign (isValidAssignments is the one check every
 * assignment passes): at his own assigning only if it was paid for ahead, and by a card's assignment only while
 * the cost can be paid.  And the cost itself, appended to the action that pays it
 * (ModifiersQuerying.appendAssignmentCosts).  The Shadow player's assignments are never charged.
 */
public abstract class ExtraAssignmentCostModifier extends AbstractModifier {
    // Null: against any character
    private final Filter _fpCharacterFilter;

    protected ExtraAssignmentCostModifier(PhysicalCard source, Filterable minionFilter, Condition condition, Filterable fpCharacterFilter) {
        super(source, "Has a cost to be assigned to skirmish", minionFilter, condition, ModifierEffect.ASSIGNMENT_MODIFIER);
        _fpCharacterFilter = (fpCharacterFilter != null) ? Filters.changeToFilter(fpCharacterFilter) : null;
    }

    /** Whether the cost could be paid as the game stands. */
    protected abstract boolean canPay(LotroGame game);

    /** Appends the cost, as costs, to the action that pays it. */
    protected abstract void appendCosts(LotroGame game, CostToEffectAction action);

    private boolean chargesAgainst(LotroGame game, PhysicalCard fpCharacter) {
        return _fpCharacterFilter == null || _fpCharacterFilter.accepts(game, fpCharacter);
    }

    // Not paid for, and not his to pay for now.  A minion marked paid for is assignable whatever the cost would
    // now be, since what paid it may not be there to pay again.  At his own assigning nothing is paid any more
    // (Q-0283): a minion not paid for ahead is not his.  Elsewhere (a card has him assign) the cost is paid as the
    // assignment is made, so the minion is his while the cost can be paid.
    private boolean cannotBePaidFor(LotroGame game, PhysicalCard minion) {
        if (game.getGameState().isAssignmentPaidFor(minion))
            return false;
        if (game.getGameState().isFreePeoplesPlayerAssigning())
            return true;
        return !canPay(game);
    }

    @Override
    public boolean appendExtraAssignmentCosts(LotroGame game, CostToEffectAction action, PhysicalCard fpCharacter, PhysicalCard minion) {
        if (!chargesAgainst(game, fpCharacter))
            return false;
        appendCosts(game, action);
        return true;
    }

    @Override
    public boolean isValidAssignments(LotroGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments) {
        if (side != Side.FREE_PEOPLE)
            return true;
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> assignment : assignments.entrySet()) {
            if (chargesAgainst(game, assignment.getKey())) {
                for (PhysicalCard minion : assignment.getValue()) {
                    if (affectsCard(game, minion) && cannotBePaidFor(game, minion))
                        return false;
                }
            }
        }
        return true;
    }

    // A minion charged for against every character is not the Free Peoples player's to assign at all while it
    // is not paid for and cannot be, so it is not listed among the minions he may assign.
    @Override
    public boolean isPreventedFromBeingAssignedToSkirmish(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return sidePlayer == Side.FREE_PEOPLE && _fpCharacterFilter == null && cannotBePaidFor(game, card);
    }
}
