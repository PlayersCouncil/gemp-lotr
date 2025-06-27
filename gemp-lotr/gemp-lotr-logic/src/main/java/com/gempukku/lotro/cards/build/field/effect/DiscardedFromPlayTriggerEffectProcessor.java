package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.filters.Filters;
import org.json.simple.JSONObject;

public class DiscardedFromPlayTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "optional", "source", "requires", "cost", "effect");

        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);
        final String source = FieldUtils.getString(value.get("source"), "source", "any");

        final FilterableSource bySource = source != null ? environment.getFilterFactory().generateFilter(source, environment) : null;

        DefaultActionSource triggerActionSource = new DefaultActionSource();
        EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

        triggerActionSource.addPlayRequirement(
                actionContext -> Filters.accepts(actionContext.getGame(), actionContext.getSource(), bySource.getFilterable(actionContext)));

        if (optional)
            blueprint.setDiscardedFromPlayOptionalTriggerAction(triggerActionSource);
        else
            blueprint.setDiscardedFromPlayRequiredTriggerAction(triggerActionSource);
    }
}
