package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ReplaceInSkirmishEffect;
import com.gempukku.lotro.logic.timing.DoNothingEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ReplaceInSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "with");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String with = FieldUtils.getString(effectObject.get("with"), "with");

        if (filter == null)
            throw new InvalidCardDefinitionException("'filter' is required for ReplaceInSkirmish effect.");

        if (with == null)
            throw new InvalidCardDefinitionException("'with' is required for ReplaceInSkirmish effect.");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCard(with, "_temp", "you", "Choose character to replace with in skirmish", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard card = actionContext.getCardFromMemory("_temp");
                        if (card == null)
                            return new DoNothingEffect();

                        final LotroGame game = actionContext.getGame();
                        final Filterable replaced = Filters.and(filterableSource.getFilterable(actionContext), Filters.inSkirmish);
                        // A skirmish has one Free Peoples character and may have several minions.  Where more than
                        // one minion in it could be the one replaced, the player performing the effect chooses.
                        if (card.getBlueprint().getSide() == Side.SHADOW && Filters.countActive(game, replaced) > 1) {
                            return new ChooseActiveCardEffect(actionContext.getSource(), actionContext.getPerformingPlayer(),
                                    "Choose character to be replaced in skirmish", replaced) {
                                @Override
                                protected void cardSelected(LotroGame game, PhysicalCard chosen) {
                                    action.insertEffect(new ReplaceInSkirmishEffect(card, chosen));
                                }
                            };
                        }

                        return new ReplaceInSkirmishEffect(card, filterableSource.getFilterable(actionContext),
                                Filters.inSkirmish);
                    }
                });

        return result;
    }

}
