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
 * Whether the given player (by default the one acting) could add a burden by this card now: the question
 * AddBurdens asks before it adds one.  False while the Ring-bearer is hindered (CR 5.0 "hinder") and while a
 * card's "burdens cannot be added" reaches this card.  For a standing text that must know whether a burden it
 * will add later can be added ("must add a burden to ...": F-0308).
 */
public class CanAddBurdens implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return (actionContext) -> {
            LotroGame game = actionContext.getGame();
            return game.getModifiersQuerying().canAddBurden(game, playerSource.getPlayer(actionContext), actionContext.getSource());
        };
    }
}
