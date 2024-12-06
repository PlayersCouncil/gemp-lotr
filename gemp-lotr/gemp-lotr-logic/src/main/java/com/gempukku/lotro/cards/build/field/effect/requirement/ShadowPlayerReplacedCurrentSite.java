package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import org.json.simple.JSONObject;

public class ShadowPlayerReplacedCurrentSite implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object);

        return (actionContext) -> {
            final LotroGame game = actionContext.getGame();
            return game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.SHADOW_PLAYER_REPLACED_CURRENT_SITE);
        };
    }
}
