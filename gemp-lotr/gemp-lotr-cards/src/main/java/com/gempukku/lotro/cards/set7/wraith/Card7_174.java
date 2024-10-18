package com.gempukku.lotro.cards.set7.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: Assignment: Remove 5 threats to assign a Nazgul to a companion (except the Ring-bearer). The Free Peoples
 * player may discard that companion.
 */
public class Card7_174 extends AbstractPermanent {
    public Card7_174() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.WRAITH, "Called");
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.ASSIGNMENT, self, 0)
                && PlayConditions.canRemoveThreat(game, self, 5)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveThreatsEffect(self, 5));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a Nazgul", Race.NAZGUL, Filters.assignableToSkirmishAgainst(Side.SHADOW, Filters.and(CardType.COMPANION, Filters.not(Filters.ringBearer)), true, false, false)) {
                        @Override
                        protected void cardSelected(LotroGame game, final PhysicalCard nazgul) {
                            action.insertEffect(
                                    new ChooseActiveCardEffect(self, playerId, "Choose a companion", CardType.COMPANION, Filters.not(Filters.ringBearer), Filters.assignableToSkirmishAgainst(Side.SHADOW, nazgul)) {
                                        @Override
                                        protected void cardSelected(final LotroGame game, final PhysicalCard card) {
                                            action.appendEffect(
                                                    new PreventableEffect(action,
                                                            new AssignmentEffect(playerId, card, nazgul),
                                                            game.getGameState().getCurrentPlayerId(),
                                                            new PreventableEffect.PreventionCost() {
                                                                @Override
                                                                public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                                                    return new DiscardCardsFromPlayEffect(game.getGameState().getCurrentPlayerId(), self, card);
                                                                }
                                                            }));
                                        }
                                    });
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
