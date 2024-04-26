package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerChecker;
import org.json.simple.JSONObject;

public class CopyCard implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        final String filter = FieldUtils.getString(value.get("filter"), "filter");

        final String autoRefreshTriggerString = """
                {
                    type: trigger
                    trigger: {
                        type: played
                        filter: """ + filter + """
                    
                    }
                    effect: {
                        type: RefreshSelf
                    }
                }
                """;

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        blueprint.appendCopiedFilter(filterableSource);

        //Now we add an automatic trigger that unapplies/reapplies this card so that
        // modifiers etc do not get stuck.

        try {
            var action = FieldUtils.parseSubObject(autoRefreshTriggerString);
            var trigger = (JSONObject) action.get("trigger");
            final var triggerChecker = environment
                    .getTriggerCheckerFactory().getTriggerChecker(trigger, environment);

            var triggerActionSource = new DefaultActionSource();
            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.processRequirementsCostsAndEffects(action, environment, triggerActionSource);

            blueprint.appendRequiredAfterTrigger(triggerActionSource);
        }
        catch(Exception ex) {
            throw new InvalidCardDefinitionException("CopyCard could not create auto refresh trigger.", ex);
        }

    }
}