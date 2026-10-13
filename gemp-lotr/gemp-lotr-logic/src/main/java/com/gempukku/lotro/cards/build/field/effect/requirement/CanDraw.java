package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.RuleUtils;
import org.json.simple.JSONObject;

/**
 * Whether the given player (by default the one acting) may draw a card now: false once the Rule of 4 is spent in
 * his own fellowship phase and while a card bars him from drawing (Race Text 94_30), true otherwise
 * (RuleUtils.mayDrawACard).  Whether his draw deck holds a card is not asked.  Ketura on Q-0249: "there should be a
 * requirement that checks to see if anyone is able to draw; this is relevant for Rule of 4 shenanigans but there's
 * also other restrictions."  Barrows of Edoras 4_337 reads it.
 */
public class CanDraw implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return (actionContext) -> RuleUtils.mayDrawACard(actionContext.getGame(), playerSource.getPlayer(actionContext));
    }
}
