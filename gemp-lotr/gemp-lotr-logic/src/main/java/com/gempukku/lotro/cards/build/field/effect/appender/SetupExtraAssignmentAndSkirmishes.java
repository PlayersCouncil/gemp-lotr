package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.modifiers.CantBeAssignedToSkirmishModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.results.AfterAllSkirmishesResult;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SetupExtraAssignmentAndSkirmishes implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");

        FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
            @Override
            protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                List<Effect> effects = new ArrayList<>();
                // The additional phase is the named minions' alone (Q-0267), but a relentless minion has the round
                // after the fierce one by rule: with one in play the two are one round (EndSkirmishesGameProcess),
                // and it is not barred from it.
                effects.add(
                        new AddUntilEndOfPhaseModifierEffect(
                                new CantBeAssignedToSkirmishModifier(action.getActionSource(), null,
                                        Filters.and(CardType.MINION, Filters.not(Keyword.RELENTLESS), Filters.not(filterableSource.getFilterable(actionContext)))), Phase.ASSIGNMENT));
                action.appendEffect(
                        new UnrespondableEffect() {
                            @Override
                            protected void doPlayEffect(LotroGame game) {
                                ((AfterAllSkirmishesResult) actionContext.getEffectResult()).setCreateAnExtraAssignmentAndSkirmishPhases(true);
                            }
                        });
                return effects;
            }
        };
    }

}
