package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.ResolveSkirmishEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Set;

public class AboutToBeOverwhelmed implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        String filter = FieldUtils.getString(value.get("filter"), "filter");

        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                LotroGame game = actionContext.getGame();
                Effect effect = actionContext.getEffect();
                if (effect.getType() == Effect.Type.BEFORE_SKIRMISH_RESOLVED) {
                    Filter filter = Filters.changeToFilter(affectedFilter.getFilterable(actionContext));
                    ResolveSkirmishEffect resolveEffect = (ResolveSkirmishEffect) effect;
                    ResolveSkirmishEffect.Result upcomingResult = resolveEffect.getUpcomingResult(game);
                    if (upcomingResult == ResolveSkirmishEffect.Result.FELLOWSHIP_OVERWHELMED
                            && filter.accepts(game, game.getGameState().getSkirmish().getFellowshipCharacter())) {
                        return true;
                    } else if (upcomingResult == ResolveSkirmishEffect.Result.SHADOW_OVERWHELMED) {
                        Set<PhysicalCard> shadowCharacters = game.getGameState().getSkirmish().getShadowCharacters();
                        for (PhysicalCard shadowCharacter : shadowCharacters) {
                            if (filter.accepts(game, shadowCharacter)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
