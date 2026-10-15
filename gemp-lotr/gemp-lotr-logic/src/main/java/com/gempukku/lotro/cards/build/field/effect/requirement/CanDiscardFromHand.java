package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.game.state.LotroGame;
import org.json.simple.JSONObject;

/**
 * Whether this card may make a forced discard from the given hand (by default the acting player's) now: false while
 * a bar on discarding from that hand reaches the card ("Shadow cards may not discard cards from your hand", Frodo,
 * Courteous Halfling 4_301 and Frodo, Little Master 19_28), true otherwise.  It is the question a forced
 * `discardFromHand` asks as its choice comes up (ModifiersQuerying.canDiscardCardsFromHand); a discard the player
 * chooses to make (`forced: false`) is never barred, so the word stands beside a forced discard only.  For a "you
 * may" inside an action whose yes is that discard (Alive and Unspoiled 1_120: F-0515; F-0379), which the rule of
 * Q-0224 never withholds: an optional trigger or ability whose whole effect is such a discard needs no requirement,
 * since the discard's own forecast says it will do nothing under the bar.
 */
public class CanDiscardFromHand implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "hand");

        final String hand = FieldUtils.getString(object.get("hand"), "hand", "you");
        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

        return (actionContext) -> {
            LotroGame game = actionContext.getGame();
            return game.getModifiersQuerying().canDiscardCardsFromHand(game, handSource.getPlayer(actionContext), actionContext.getSource());
        };
    }
}
