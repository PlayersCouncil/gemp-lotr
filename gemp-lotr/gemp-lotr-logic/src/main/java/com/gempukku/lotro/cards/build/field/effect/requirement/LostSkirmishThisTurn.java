package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.CharacterLostSkirmishResult;
import org.json.simple.JSONObject;

public class LostSkirmishThisTurn implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            Filter filterable = Filters.changeToFilter(filterableSource.getFilterable(actionContext));
            LotroGame game = actionContext.getGame();

            for (EffectResult effectResult : game.getActionsEnvironment().getTurnEffectResults()) {
                if (effectResult.getType() == EffectResult.Type.CHARACTER_LOST_SKIRMISH) {
                    CharacterLostSkirmishResult lostResult = (CharacterLostSkirmishResult) effectResult;
                    if (filterable.accepts(game, lostResult.getLoser()))
                        return true;
                }
            }
            return false;
        };
    }
}
