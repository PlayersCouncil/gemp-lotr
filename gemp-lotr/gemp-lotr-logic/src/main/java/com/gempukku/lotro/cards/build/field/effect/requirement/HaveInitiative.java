package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Side;
import org.json.simple.JSONObject;

public class HaveInitiative implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "side");

        Side side = FieldUtils.getSide(object.get("side"), "side");

        // A card being played from the Free Peoples player's hand is not counted for its own requirement (CR 4.2
        // "playing a card"; F-0464): an event that says "If you have initiative, ..." wants four cards beside it.
        return (actionContext) -> actionContext.getGame().getModifiersQuerying()
                .hasInitiativeNotCounting(actionContext.getGame(), actionContext.getSource()) == side;
    }
}
