package com.gempukku.lotro.cards.set18.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddTokenEffect;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.RemoveTokenEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 0
 * Type: Condition • Support Area
 * Game Text: Each time you play a [ROHAN] possession from hand, add a [ROHAN] token here. Skirmish: Remove 3 [ROHAN]
 * tokens from here to make a [ROHAN] companion strength +1 and damage +1.
 */
public class Card18_094 extends AbstractPermanent {
    public Card18_094() {
        super(Side.FREE_PEOPLE, 0, CardType.CONDITION, Culture.ROHAN, "Cast from the Hall");
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, Filters.owner(self.getOwner()), Culture.ROHAN, CardType.POSSESSION)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddTokenEffect(self, self, Token.ROHAN));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canRemoveTokens(game, self, Token.ROHAN, 3)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTokenEffect(self, self, Token.ROHAN, 3));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a ROHAN companion", CardType.COMPANION, Culture.ROHAN) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.appendEffect(
                                    new AddUntilEndOfPhaseModifierEffect(
                                            new StrengthModifier(self, card, 1)));
                            action.appendEffect(
                                    new AddUntilEndOfPhaseModifierEffect(
                                            new AddKeywordModifier(self, card, Keyword.DAMAGE, 1)));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
