package com.gempukku.lotro.cards.set12.orc;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 2
 * Type: Minion • Orc
 * Strength: 6
 * Vitality: 1
 * Site: 4
 * Game Text: Each time the Free Peoples player assigns this minion to skirmish an unwounded companion, he or she must
 * add a burden or discard a card from hand.
 */
public class Card12_098 extends AbstractMinion {
    public Card12_098() {
        super(2, 6, 1, 4, Race.ORC, Culture.ORC, "Orc Tormentor");
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.assignedAgainst(game, effectResult, Side.FREE_PEOPLE, Filters.and(CardType.COMPANION, Filters.unwounded), self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            List<Effect> possibleEffects = new LinkedList<>();
            possibleEffects.add(
                    new AddBurdenEffect(game.getGameState().getCurrentPlayerId(), self, 1));
            possibleEffects.add(
                    new ChooseAndDiscardCardsFromHandEffect(action, game.getGameState().getCurrentPlayerId(), false, 1) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard a card from hand";
                        }
                    });
            action.appendEffect(
                    new ChoiceEffect(action, game.getGameState().getCurrentPlayerId(), possibleEffects));
            return Collections.singletonList(action);
        }
        return null;
    }
}
