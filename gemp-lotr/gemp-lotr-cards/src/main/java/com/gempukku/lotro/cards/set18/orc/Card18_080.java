package com.gempukku.lotro.cards.set18.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.ExhaustCharacterEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 6
 * Type: Minion • Orc
 * Strength: 15
 * Vitality: 3
 * Site: 4
 * Game Text: Each the fellowship and minion archery totals are +3. Regroup: Remove (2) to play an [ORC] minion from
 * your discard pile. That minion comes into play exhausted.
 */
public class Card18_080 extends AbstractMinion {
    public Card18_080() {
        super(6, 15, 3, 4, Race.ORC, Culture.ORC, "Gothmog", "Morgul Leader", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new ArcheryTotalModifier(self, Side.FREE_PEOPLE, 3));
        modifiers.add(
                new ArcheryTotalModifier(self, Side.SHADOW, 3));
        return modifiers;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 2)
                && PlayConditions.canPlayFromDiscard(playerId, game, 2, 0, Culture.ORC, CardType.MINION)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(2));
            action.appendEffect(
                    new ChooseAndPlayCardFromDiscardEffect(playerId, game, Culture.ORC, CardType.MINION) {
                        @Override
                        protected void afterCardPlayed(PhysicalCard cardPlayed) {
                            action.appendEffect(
                                    new ExhaustCharacterEffect(self, action, cardPlayed));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
