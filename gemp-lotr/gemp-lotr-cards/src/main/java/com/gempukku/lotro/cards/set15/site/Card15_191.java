package com.gempukku.lotro.cards.set15.site;

import com.gempukku.lotro.logic.cardtype.AbstractShadowsSite;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.modifiers.evaluator.CountCulturesEvaluator;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Twilight Cost: 0
 * Type: Site
 * Game Text: Battleground. Each time the fellowship moves to this site add (1) for each culture you can spot.
 */
public class Card15_191 extends AbstractShadowsSite {
    public Card15_191() {
        super("Gate of Mordor", 0, Direction.LEFT);
        addKeyword(Keyword.BATTLEGROUND);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.movesTo(game, effectResult, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddTwilightEffect(self, new CountCulturesEvaluator(Filters.any).evaluateExpression(game, null)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
