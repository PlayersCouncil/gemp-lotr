package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.LinkedList;
import java.util.List;

/**
 * A cost of assigning ("the Free Peoples player must exert a companion to assign this minion to a skirmish",
 * ExtraAssignmentCostModifier) is paid ahead of the Free Peoples player's own assigning (Ketura on Q-0283).  As he
 * starts assigning (FreePlayerStartsAssigningResult), each minion with such a cost is offered to him as an
 * optional trigger of that minion, so that the client shows the minion to click: one whose cost is not yet paid,
 * can be paid in full (F-0425), and which he could then assign to a character it charges for.  He takes them in
 * any order, paying for each (choosing who exerts) as he takes it, and the rest are offered again, judged afresh,
 * until he passes.  A minion paid for is marked so (GameState.isAssignmentPaidFor) and is his to assign; at his
 * own assigning a charging minion with no mark is not (ExtraAssignmentCostModifier).  A mark not used by his
 * assigning is lost as it ends.
 *
 * Where a card has him assign (Éowyn 17_96 and the others), it acts before this, and the cost is paid then, as the
 * assignment is made (AssignFpCharacterToSkirmish).  The Shadow player's assignments are never charged.
 */
public class AssignmentCostRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public AssignmentCostRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() != EffectResult.Type.FREE_PEOPLE_PLAYER_STARTS_ASSIGNING
                                || !playerId.equals(game.getGameState().getCurrentPlayerId()))
                            return null;

                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard minion : Filters.filterActive(game, CardType.MINION)) {
                            if (game.getGameState().isAssignmentPaidFor(minion))
                                continue;
                            final PhysicalCard chargedAgainst = findCharacterToPayFor(game, playerId, minion);
                            if (chargedAgainst == null)
                                continue;

                            final OptionalTriggerAction action = new OptionalTriggerAction(minion);
                            action.setText("Pay to assign " + GameUtils.getFullName(minion));
                            action.setMessage(playerId + " pays to assign " + GameUtils.getCardLink(minion));
                            game.getModifiersQuerying().appendAssignmentCosts(game, action, chargedAgainst, minion);
                            action.appendEffect(
                                    new UnrespondableEffect() {
                                        @Override
                                        protected void doPlayEffect(LotroGame game) {
                                            game.getGameState().markAssignmentPaidFor(minion);
                                        }
                                    });
                            result.add(action);
                        }
                        return result;
                    }
                });
    }

    // A character this player could assign the minion to, as the game stands, that a cost of assigning charges
    // for; null if none.  The check of the assignment is the one every assignment passes, and it asks whether the
    // cost can be paid in full.
    private static PhysicalCard findCharacterToPayFor(LotroGame game, String playerId, PhysicalCard minion) {
        for (PhysicalCard character : Filters.filterActive(game, Filters.or(CardType.COMPANION, CardType.ALLY))) {
            if (game.getModifiersQuerying().getAssignmentCosts(game, playerId, character, minion) != null
                    && Filters.assignableToSkirmishAgainst(Side.FREE_PEOPLE, character).accepts(game, minion))
                return character;
        }
        return null;
    }
}
