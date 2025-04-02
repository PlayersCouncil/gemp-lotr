package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.PhysicalCard;
import org.json.simple.JSONObject;

import java.util.Collection;

public class HasCardInMemory implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "memory");

        final String memory = FieldUtils.getString(object.get("memory"), "memory");

        return actionContext -> {
			Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
			return !cardsFromMemory.isEmpty();
		};
    }
}
