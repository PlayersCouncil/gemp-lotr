package com.gempukku.lotro.cards.set12.gandalf;

import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.RemoveBurdenEffect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 2
 * Type: Companion • Man
 * Strength: 4
 * Vitality: 3
 * Resistance: 5
 * Game Text: At the start of each skirmish involving Jarnsmid, you may remove a burden (or 2 burdens if you can spot
 * another [GANDALF] companion).
 */
public class Card12_030 extends AbstractCompanion {
    public Card12_030() {
        super(2, 4, 3, 5, Culture.GANDALF, Race.MAN, null, "Járnsmid", "Barding Emissary", true);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.SKIRMISH)
                && Filters.inSkirmish.accepts(game, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            int count = PlayConditions.canSpot(game, Filters.not(self), Culture.GANDALF, CardType.COMPANION) ? 2 : 1;
            action.appendEffect(
                    new RemoveBurdenEffect(playerId, self, count));
            return Collections.singletonList(action);
        }
        return null;
    }
}
