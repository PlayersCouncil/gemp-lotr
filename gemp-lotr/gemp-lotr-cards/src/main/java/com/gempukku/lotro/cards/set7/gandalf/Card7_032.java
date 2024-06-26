package com.gempukku.lotro.cards.set7.gandalf;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractResponseEvent;
import com.gempukku.lotro.logic.effects.CancelEventEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.PlayEventResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 0
 * Type: Event • Response
 * Game Text: Spell. If an event is played, exert Gandalf to make that opponent remove (2) or cancel that event.
 */
public class Card7_032 extends AbstractResponseEvent {
    public Card7_032() {
        super(Side.FREE_PEOPLE, 0, Culture.GANDALF, "The Board Is Set");
        addKeyword(Keyword.SPELL);
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, CardType.EVENT)
                && PlayConditions.canExert(self, game, Filters.gandalf)) {
            PlayEventResult playEventResult = (PlayEventResult) effectResult;

            PlayEventAction action = new PlayEventAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.gandalf));
            List<Effect> possibleEffects = new LinkedList<>();
            possibleEffects.add(
                    new RemoveTwilightEffect(2));
            possibleEffects.add(
                    new CancelEventEffect(self, playEventResult));
            action.appendEffect(
                    new ChoiceEffect(action, playEventResult.getPlayedCard().getOwner(), possibleEffects));
            return Collections.singletonList(action);
        }
        return null;
    }
}
