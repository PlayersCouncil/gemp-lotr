package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LimitRequirement;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

/**
 * The card has been used fewer than "limit" times at the site the fellowship is at, this turn ("limit once per
 * site").  Prefer the "limitPerSite" field of an ability, which checks and counts.
 */
public class PerSiteLimit implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "limit");

        final int limit = FieldUtils.getInteger(object.get("limit"), "limit", 1);

        return (LimitRequirement) (actionContext) -> PlayConditions.checkSiteLimit(actionContext.getGame(), actionContext.getSource(), limit);
    }
}
