package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Zone;
import org.json.simple.JSONObject;

public class WasPlayedFromZone implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "zone");

        final Zone zone = FieldUtils.getEnum(Zone.class, object.get("zone"), "zone");

        return (actionContext) -> actionContext.getSource().getPlayedFromZone() == zone;
    }
}
