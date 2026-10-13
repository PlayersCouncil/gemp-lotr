package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.DrawCardOrPutIntoHandResult;
import org.json.simple.JSONObject;

/**
 * "Each time <player> draws a card": answers a card DRAWN, and a card taken into hand in place of a draw, which
 * is the card drawn (Race Text 94_28; a PutCardsFromDeckIntoHand that says inPlaceOfDraw).  A card taken into
 * hand in any other way (from a discard pile, from a draw deck by a search or a reveal, from a stack, from the
 * removed pile) is not a card drawn, and is answered only where the text says "(or takes a card into hand)":
 * orTakesIntoHand: true (F-0376).
 */
public class PlayerDrawsCard implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "player", "orTakesIntoHand");

        final String player = FieldUtils.getString(value.get("player"), "player");
        final boolean orTakesIntoHand = FieldUtils.getBoolean(value.get("orTakesIntoHand"), "orTakesIntoHand", false);

        PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                String playerId = playerSource.getPlayer(actionContext);

                EffectResult effectResult = actionContext.getEffectResult();
                if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
                    DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
                    return (orTakesIntoHand || drawResult.isDraw()) && drawResult.getPlayerId().equals(playerId);
                }
                return false;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
