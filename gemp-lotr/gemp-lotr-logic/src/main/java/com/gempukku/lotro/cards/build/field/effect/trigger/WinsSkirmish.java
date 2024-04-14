package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.CharacterWonSkirmishResult;
import org.json.simple.JSONObject;

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
                final boolean result = TriggerConditions.winsSkirmishInvolving(actionContext.getGame(), actionContext.getEffectResult(),
                        winnerFilter.getFilterable(actionContext),
                        againstFilter.getFilterable(actionContext));
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
                    Set<PhysicalCard> losers = wonResult.getInvolving();
                    PhysicalCard winner = wonResult.getWinner();
                    if (winner != null) {
                        losers.remove(winner);
                    }
                    actionContext.setCardMemory(memorizeInvolving, losers);
                }
                return result;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
