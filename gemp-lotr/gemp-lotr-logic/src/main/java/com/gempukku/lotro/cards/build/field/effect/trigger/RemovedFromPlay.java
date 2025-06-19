package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import com.gempukku.lotro.logic.timing.results.ForEachKilledResult;
import com.gempukku.lotro.logic.timing.results.ReturnCardsToHandResult;
import org.json.simple.JSONObject;

public class RemovedFromPlay implements TriggerCheckerProducer {
	@Override
	public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(value, "filter", "memorize");

		final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
		final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

		final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

		return new TriggerChecker() {
			@Override
			public boolean isBefore() {
				return false;
			}

			@Override
			public boolean accepts(ActionContext actionContext) {
				final Filterable filterable = filterableSource.getFilterable(actionContext);
				final boolean killResult = TriggerConditions.forEachKilled(actionContext.getGame(), actionContext.getEffectResult(), false, null, filterable);
				if (killResult && memorize != null) {
					final PhysicalCard killedCard = ((ForEachKilledResult) actionContext.getEffectResult()).getKilledCard();
					actionContext.setCardMemory(memorize, killedCard);
				}

				final boolean discardResult = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
				if (discardResult && memorize != null) {
					final PhysicalCard discardedCard = ((DiscardCardsFromPlayResult) actionContext.getEffectResult()).getDiscardedCard();
					actionContext.setCardMemory(memorize, discardedCard);
				}

				final boolean returnedResult = TriggerConditions.forEachReturnedToHand(actionContext.getGame(), actionContext.getEffectResult(), filterable);
				if (returnedResult && memorize != null) {
					final PhysicalCard returnedCard = ((ReturnCardsToHandResult) actionContext.getEffectResult()).getReturnedCard();
					actionContext.setCardMemory(memorize, returnedCard);
				}
				return killResult || discardResult || returnedResult;
			}
		};
	}
}