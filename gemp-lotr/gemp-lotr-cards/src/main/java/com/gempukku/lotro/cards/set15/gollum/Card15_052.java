package com.gempukku.lotro.cards.set15.gollum;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.effects.SnapshotAndApplyStrengthModifierUntilStartOfPhaseEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Gollum
 * Twilight Cost: 2
 * Type: Event • Maneuver
 * Game Text: Spot Frodo and Smeagol to discard a minion. That minion’s owner may make each minion strength -2 until
 * the end of the turn to prevent that.
 */
public class Card15_052 extends AbstractEvent {
    public Card15_052() {
        super(Side.FREE_PEOPLE, 2, Culture.GOLLUM, "Swear by the Precious", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.frodo)
                && PlayConditions.canSpot(game, Filters.smeagol);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose a minion to discard", CardType.MINION, Filters.canBeDiscarded(self)) {
                    @Override
                    protected void cardSelected(LotroGame game, final PhysicalCard card) {
                        action.appendEffect(
                                new PreventableEffect(action,
                                        new DiscardCardsFromPlayEffect(playerId, self, card), card.getOwner(),
                                        new PreventableEffect.PreventionCost() {

                                            @Override
                                            public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                                return new SnapshotAndApplyStrengthModifierUntilStartOfPhaseEffect(
                                                        self, new ConstantEvaluator(-2), Phase.FELLOWSHIP, CardType.MINION) {
                                                    @Override
                                                    public String getText(LotroGame game) {
                                                        return "Make each minion strength -2";
                                                    }
                                                };
                                            }
                                        }));
                    }
                });
        return action;
    }
}
