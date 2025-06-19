package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import org.json.simple.JSONObject;

public class CanMove implements RequirementProducer {
	@Override
	public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(object);

		return (actionContext) -> {
			LotroGame game = actionContext.getGame();
			return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
					&& game.getGameState().getMoveCount() < game.getModifiersQuerying().getMoveLimit(game, 2);
		};
	}
}