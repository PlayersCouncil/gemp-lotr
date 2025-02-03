package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.CharacterWonSkirmishResult;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class WinsSkirmish implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "against", "memorize", "memorizeInvolving", "memorizeLoser");

        String winner = FieldUtils.getString(value.get("filter"), "filter", "any");
        String against = FieldUtils.getString(value.get("against"), "against", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final String memorizeInvolving = FieldUtils.getString(value.get("memorizeInvolving"), "memorizeInvolving");
        final String memorizeLoser = FieldUtils.getString(value.get("memorizeLoser"), "memorizeLoser");

        final FilterableSource winnerFilter = environment.getFilterFactory().generateFilter(winner, environment);
        final FilterableSource againstFilter = environment.getFilterFactory().generateFilter(against, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final boolean result = checkResult(actionContext.getEffectResult(), actionContext.getGame(),
                        winnerFilter.getFilterable(actionContext), againstFilter.getFilterable(actionContext));
                if (result && memorize != null) {
                    CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) actionContext.getEffectResult();
                    actionContext.setCardMemory(memorize, wonResult.getWinner());
                }
                if (result && memorizeInvolving != null) {
                    CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) actionContext.getEffectResult();
                    actionContext.setCardMemory(memorizeInvolving, wonResult.getInvolving());
                }
                if (result && memorizeLoser != null) {
                    CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) actionContext.getEffectResult();
                    Set<PhysicalCard> losers = new HashSet<>(wonResult.getInvolving());
                    PhysicalCard winner = wonResult.getWinner();
                    if (winner != null) {
                        losers.remove(winner);
                    }
                    actionContext.setCardMemory(memorizeLoser, losers);
                }
                return result;
            }

            private boolean checkResult(EffectResult effectResult, LotroGame game, Filterable winnerFilter, Filterable againstFilter) {
                if (effectResult.getType() == EffectResult.Type.CHARACTER_WON_SKIRMISH) {
                    CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) effectResult;
                    return Filters.accepts(game, winnerFilter, wonResult.getWinner())
                            && Filters.acceptsAny(game, wonResult.getInvolving(), againstFilter);
                }
                return false;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
