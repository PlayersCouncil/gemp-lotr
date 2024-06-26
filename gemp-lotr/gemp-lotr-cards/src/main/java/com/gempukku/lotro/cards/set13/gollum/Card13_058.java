package com.gempukku.lotro.cards.set13.gollum;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 1
 * Type: Event • Shadow
 * Game Text: Spot Gollum to make the Free Peoples player choose to either add a burden or exert a companion.
 */
public class Card13_058 extends AbstractEvent {
    public Card13_058() {
        super(Side.SHADOW, 1, Culture.GOLLUM, "Wild Light of Madness", Phase.SHADOW);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.gollum);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new AddBurdenEffect(game.getGameState().getCurrentPlayerId(), self, 1));
        possibleEffects.add(
                new ChooseAndExertCharactersEffect(action, game.getGameState().getCurrentPlayerId(), 1, 1, CardType.COMPANION) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Exert a companion";
                    }
                });
        action.appendEffect(
                new ChoiceEffect(action, game.getGameState().getCurrentPlayerId(), possibleEffects));
        return action;
    }
}
