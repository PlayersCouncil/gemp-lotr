package com.gempukku.lotro.cards.set11.wraith;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Names;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 6
 * Type: Minion • Nazgul
 * Strength: 12
 * Vitality: 3
 * Site: 3
 * Game Text: Fierce. Each time Úlairë Attëa is assigned to a fierce skirmish involving a character who has resistance
 * 4 or less, you may exert Úlairë Attëa to add a burden.
 */
public class Card11_219 extends AbstractMinion {
    public Card11_219() {
        super(6, 12, 3, 3, Race.NAZGUL, Culture.WRAITH, Names.attea, "Second of the Nine Riders", true);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.assignedAgainst(game, effectResult, null, Filters.and(Filters.character, Filters.maxResistance(4)), self)
                && game.getGameState().isFierceSkirmishes()
                && PlayConditions.canSelfExert(self, game)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new AddBurdenEffect(playerId, self, 1));
            return Collections.singletonList(action);
        }
        return null;
    }
}
