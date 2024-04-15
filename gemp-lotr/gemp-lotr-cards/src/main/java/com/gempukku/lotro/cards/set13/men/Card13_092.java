package com.gempukku.lotro.cards.set13.men;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 2
 * Type: Minion • Man
 * Strength: 4
 * Vitality: 3
 * Site: 3
 * Game Text: Lurker. (Skirmishes involving lurker minions must be resolved after any others.) Each time a Free Peoples
 * character is killed, you may spot another [MEN] minion to exert each companion.
 */
public class Card13_092 extends AbstractMinion {
    public Card13_092() {
        super(2, 4, 3, 3, Race.MAN, Culture.MEN, "Gríma", "Footman of Saruman", true);
        addKeyword(Keyword.LURKER);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.forEachKilled(game, effectResult, Side.FREE_PEOPLE)
                && PlayConditions.canSpot(game, Filters.not(self), Culture.MEN, CardType.MINION)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ExertCharactersEffect(action, self, CardType.COMPANION));
            return Collections.singletonList(action);
        }
        return null;
    }
}
