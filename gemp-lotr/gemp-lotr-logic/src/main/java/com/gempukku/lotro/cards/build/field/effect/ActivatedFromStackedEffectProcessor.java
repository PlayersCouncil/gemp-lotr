package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class ActivatedFromStackedEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase", "requires", "noEffectUnless", "stackedOn", "cost", "effect", "limitPerPhase", "limitPerTurn", "limitPerSite", "effectLimitPerPhase", "effectLimitPerTurn", "text");

        final String text = FieldUtils.getString(value.get("text"), "text");
        final String[] phaseArray = FieldUtils.getStringArray(value.get("phase"), "phase");

        final String stackedOn = FieldUtils.getString(value.get("stackedOn"), "stackedOn", "any");

        final FilterableSource stackedOnFilterableSource = environment.getFilterFactory().generateFilter(stackedOn, environment);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            actionSource.setSkirmishAbility(phase == Phase.SKIRMISH);
            EffectUtils.processLimits(value, actionSource, phase, true);
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