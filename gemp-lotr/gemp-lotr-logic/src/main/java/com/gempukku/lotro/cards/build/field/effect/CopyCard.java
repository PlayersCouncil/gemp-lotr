package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

public class CopyCard implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        final String filter = FieldUtils.getString(value.get("filter"), "filter");

        if (StringUtils.isEmpty(filter))
            throw new InvalidCardDefinitionException("Filter is required on CopyCard effects.");

        final String autoRefreshTriggerString = """
                {
                    type: trigger
                    trigger: {
                        type: played
                        filter: """ + filter + """
                    
                    }
                    effect: {
                        type: RefreshCard
                    }
                }
                """;

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        blueprint.appendCopiedFilter(filterableSource);

        // The copied card can also stop matching without anything being played, most obviously when a copied site
        // is replaced by an opponent's copy (#1054): the copied modifiers would otherwise stick until the next refresh.
        final String siteReplacedRefreshTriggerString = """
                {
                    type: trigger
                    trigger: {
                        type: replacesSite
                    }
                    effect: {
                        type: RefreshCard
                    }
                }
                """;

        //Now we add automatic triggers that unapply/reapply this card so that
        // modifiers etc do not get stuck.

        for (String triggerString : new String[] { autoRefreshTriggerString, siteReplacedRefreshTriggerString }) {
            try {
                var action = FieldUtils.parseSubObject(triggerString);
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
}