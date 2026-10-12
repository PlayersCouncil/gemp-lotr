package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.EnteredPlayResult;
import org.json.simple.JSONObject;

/**
 * A card's own "When you play this ..., <do or pay something> or discard it" (Dunlending Rampager, Stampeding
 * Chief): answered as the card enters play, before its play is announced, so that a card its own text discards
 * was not played (see EnteredPlayResult and PlayCardEffect).  It answers the playing of the card it is on and of
 * no other; a "when you play" that does anything else is a Played trigger.
 */
public class EntersPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "fromZone");

        final Zone zone = FieldUtils.getEnum(Zone.class, value.get("fromZone"), "fromZone");

        if (zone != null && zone != Zone.HAND && zone != Zone.DISCARD && zone != Zone.DECK && zone != Zone.STACKED
                && zone != Zone.DEAD) {
            throw new InvalidCardDefinitionException("'fromZone' using an unsupported zone in EntersPlay trigger.");
        }

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final EffectResult effectResult = actionContext.getEffectResult();
                if (effectResult == null || effectResult.getType() != EffectResult.Type.ENTERS_PLAY)
                    return false;

                final EnteredPlayResult entered = (EnteredPlayResult) effectResult;
                if (!entered.isOwnText() || entered.getCard() != actionContext.getSource())
                    return false;

                return zone == null || entered.getPlayedFrom() == zone;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
