package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: When you play this condition, place a [GONDOR] token here for each of the following characters you can
 * spot: Aragorn, Boromir, Denethor or Faramir. Skirmish: Remove a token from here or discard this condition to make
 * a [GONDOR] companion strength +1 and damage +1.
 */
public class Card7_112 extends AbstractPermanent {
    public Card7_112() {
        super(Side.FREE_PEOPLE, 1, CardType.CONDITION, Culture.GONDOR, "Noble Leaders", null, true);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            int countActive = Filters.countActive(game, Filters.or(Filters.aragorn, Filters.boromir, Filters.name("Denethor"), Filters.name("Faramir")));
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddTokenEffect(self, self, Token.GONDOR, countActive));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<>();
            possibleCosts.add(
                    new RemoveTokenEffect(self, self, Token.GONDOR) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Remove a token from here";
                        }
                    });
            possibleCosts.add(
                    new SelfDiscardEffect(self) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard this condition";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a GONDOR companion", Culture.GONDOR, CardType.COMPANION) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            game.getModifiersEnvironment().addUntilEndOfPhaseModifier(
                                    new StrengthModifier(self, card, 1), Phase.SKIRMISH);
                            game.getModifiersEnvironment().addUntilEndOfPhaseModifier(
                                    new KeywordModifier(self, card, Keyword.DAMAGE, 1), Phase.SKIRMISH);
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
