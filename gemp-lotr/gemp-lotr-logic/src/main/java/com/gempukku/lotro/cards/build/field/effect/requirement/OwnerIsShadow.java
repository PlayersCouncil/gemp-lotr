package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.logic.GameUtils;
import org.json.simple.JSONObject;

public class OwnerIsShadow implements RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object);

        return new Requirement() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return GameUtils.isSide(actionContext.getGame(), Side.SHADOW, actionContext.getSource().getOwner());
            }
        };
    }
}
