package com.gempukku.lotro.cards.set10.shire;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.RemoveBurdenEffect;
import com.gempukku.lotro.logic.effects.RemoveThreatsEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 0
 * Type: Event • Skirmish
 * Game Text: Tale. Exert a [SHIRE] character to remove a burden or 2 threats.
 */
public class Card10_112 extends AbstractEvent {
    public Card10_112() {
        super(Side.FREE_PEOPLE, 0, Culture.SHIRE, "Nine-fingered Frodo and the Ring of Doom", Phase.SKIRMISH);
        addKeyword(Keyword.TALE);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Culture.SHIRE, Filters.character);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Culture.SHIRE, Filters.character));
        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new RemoveBurdenEffect(playerId, self));
        if (game.getGameState().getThreats() > 1) {
            possibleEffects.add(
                    new RemoveThreatsEffect(self, 2) {
                @Override
                public String getText(LotroGame game) {
                    return "Remove 2 threats";
                }
            });
        } else if (game.getGameState().getBurdens() == 0) {
            possibleEffects.add(
                    new RemoveThreatsEffect(self, 1));
        }
        action.appendEffect(
                new ChoiceEffect(action, playerId, possibleEffects));
        return action;
    }
}
