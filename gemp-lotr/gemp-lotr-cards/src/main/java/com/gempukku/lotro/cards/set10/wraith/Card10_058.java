package com.gempukku.lotro.cards.set10.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 1
 * Type: Event • Skirmish
 * Game Text: Heal an enduring [WRAITH] minion to add a burden or 2 threats.
 */
public class Card10_058 extends AbstractEvent {
    public Card10_058() {
        super(Side.SHADOW, 1, Culture.WRAITH, "Dark Swooping Shadows", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canHeal(self, game, Culture.WRAITH, CardType.MINION, Keyword.ENDURING);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndHealCharactersEffect(action, playerId, 1, 1, Culture.WRAITH, CardType.MINION, Keyword.ENDURING));
        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new AddBurdenEffect(playerId, self, 1));
        possibleEffects.add(
                new AddThreatsEffect(playerId, self, 2));
        action.appendEffect(
                new ChoiceEffect(action, playerId, possibleEffects));
        return action;
    }
}
