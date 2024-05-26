package com.gempukku.lotro.cards.set18.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.modifiers.condition.AndCondition;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: While you can spot an [ORC] minion and fewer minions than companions, each companion loses all defender
 * bonuses and cannot gain defender bonuses. Maneuver: Spot 2 [ORC] minions to make the fellowship's current site gain
 * battleground until the regroup phase.
 */
public class Card18_078 extends AbstractPermanent {
    public Card18_078() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.ORC, "Destroyers and Usurpers");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(new RemoveKeywordModifier(self, CardType.COMPANION,
                new AndCondition(
                        new SpotCondition(Culture.ORC, CardType.MINION),
                        new Condition() {
                            @Override
                            public boolean isFullfilled(LotroGame game) {
                                return Filters.countActive(game, CardType.MINION)
                                        < Filters.countActive(game, CardType.COMPANION);
                            }
                        }), Keyword.DEFENDER));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.MANEUVER, self, 0)
                && PlayConditions.canSpot(game, 2, Culture.ORC, CardType.MINION)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new AddKeywordModifier(self, game.getGameState().getCurrentSite(), Keyword.BATTLEGROUND), Phase.REGROUP));
            return Collections.singletonList(action);
        }
        return null;
    }
}
