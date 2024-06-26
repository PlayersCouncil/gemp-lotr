package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.LiberateASiteEffect;
import com.gempukku.lotro.logic.effects.SpotEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Event
 * Game Text: Spot 2 knights or exert 2 [GONDOR] Men to liberate a site or discard any number of cards from hand.
 */
public class Card7_117 extends AbstractEvent {
    public Card7_117() {
        super(Side.FREE_PEOPLE, 1, Culture.GONDOR, "Reckless Counter", Phase.REGROUP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return (PlayConditions.canSpot(game, 2, Keyword.KNIGHT) || PlayConditions.canExert(self, game, 1, 2, Culture.GONDOR, Race.MAN));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        List<Effect> possibleCosts = new LinkedList<>();
        possibleCosts.add(
                new SpotEffect(2, Keyword.KNIGHT) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Spot 2 knights";
                    }
                });
        possibleCosts.add(
                new ChooseAndExertCharactersEffect(action, playerId, 2, 2, Culture.GONDOR, Race.MAN) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Exert 2 GONDOR Men";
                    }
                });
        action.appendCost(
                new ChoiceEffect(action, playerId, possibleCosts));

        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new LiberateASiteEffect(self, playerId, null));
        possibleEffects.add(
                new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 0, Integer.MAX_VALUE, Filters.any) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Discard any number of cards from hand";
                    }
                });
        action.appendEffect(
                new ChoiceEffect(action, playerId, possibleEffects));
        return action;
    }
}
