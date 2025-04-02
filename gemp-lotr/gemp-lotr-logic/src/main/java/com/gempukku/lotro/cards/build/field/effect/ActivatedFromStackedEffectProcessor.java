package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.AbstractEffectAppender;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.IncrementPhaseLimitEffect;
import com.gempukku.lotro.logic.effects.IncrementTurnLimitEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class ActivatedFromStackedEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase", "requires", "stackedOn", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String text = FieldUtils.getString(value.get("text"), "text");
        final String[] phaseArray = FieldUtils.getStringArray(value.get("phase"), "phase");
        final int limitPerPhase = FieldUtils.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);

        final String stackedOn = FieldUtils.getString(value.get("stackedOn"), "stackedOn", "any");

        final FilterableSource stackedOnFilterableSource = environment.getFilterFactory().generateFilter(stackedOn, environment);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limitPerPhase));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, limitPerPhase);
                            }
                        });
            }
            if (limitPerTurn > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limitPerTurn));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementTurnLimitEffect(actionContext.getSource(), limitPerTurn);
                            }
                        });
            }
            actionSource.addPlayRequirement(
                    (actionContext) -> PlayConditions.isPhase(actionContext.getGame(), phase));
            actionSource.addPlayRequirement(
                    (actionContext) -> {
                        PhysicalCard stackedOnCard = actionContext.getSource().getStackedOn();
                        return stackedOnCard != null && Filters.accepts(actionContext.getGame(), stackedOnCard,
								stackedOnFilterableSource.getFilterable(actionContext));
                    }
            );
            EffectUtils.processRequirementsCostsAndEffects(value, environment, actionSource);

            blueprint.appendFromStackedPhaseAction(actionSource);
        }
    }
}