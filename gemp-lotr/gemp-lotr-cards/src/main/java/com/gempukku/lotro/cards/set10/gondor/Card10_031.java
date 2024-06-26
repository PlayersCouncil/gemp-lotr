package com.gempukku.lotro.cards.set10.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardStackedCardsEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Event • Maneuver
 * Game Text: Spot a [GONDOR] Man to discard a stacked Shadow card or to wound a minion bearing a fortification.
 */
public class Card10_031 extends AbstractEvent {
    public Card10_031() {
        super(Side.FREE_PEOPLE, 1, Culture.GONDOR, "Every Little is a Gain", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.GONDOR, Race.MAN);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new ChooseAndDiscardStackedCardsEffect(action, playerId, 1, 1, Filters.any, Side.SHADOW) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Discard a stacked Shadow card";
                    }
                });
        possibleEffects.add(
                new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.MINION, Filters.hasAttached(Keyword.FORTIFICATION)) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Wound a minion bearing a fortification";
                    }
                });
        action.appendEffect(
                new ChoiceEffect(action, playerId, possibleEffects));
        return action;
    }
}
