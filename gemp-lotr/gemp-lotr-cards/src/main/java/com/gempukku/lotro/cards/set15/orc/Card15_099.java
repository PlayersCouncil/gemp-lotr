package com.gempukku.lotro.cards.set15.orc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.CountActiveEvaluator;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 5
 * Type: Minion • Orc
 * Strength: 12
 * Vitality: 3
 * Site: 4
 * Game Text: To play, spot an [ORC] minion. When you play Black Land Chieftain, you may add a threat for each
 * Free Peoples possession and each Free Peoples artifact you can spot.
 */
public class Card15_099 extends AbstractMinion {
    public Card15_099() {
        super(5, 12, 3, 4, Race.ORC, Culture.ORC, "Black Land Chieftain", null, true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.ORC, CardType.MINION);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new AddThreatsEffect(playerId, self, new CountActiveEvaluator(Side.FREE_PEOPLE, Filters.or(CardType.POSSESSION, CardType.ARTIFACT))));
            return Collections.singletonList(action);
        }
        return null;
    }
}
