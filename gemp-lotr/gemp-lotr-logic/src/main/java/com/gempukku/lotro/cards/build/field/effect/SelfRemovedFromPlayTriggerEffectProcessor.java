package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import com.gempukku.lotro.logic.timing.results.HinderedResult;
import org.json.simple.JSONObject;

public class SelfRemovedFromPlayTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "optional", "source", "method", "requires", "cost", "effect");

        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);
        final String source = FieldUtils.getString(value.get("source"), "source", "any");
        final String method = FieldUtils.getString(value.get("method"), "method", "");

        final FilterableSource bySource = source != null ? environment.getFilterFactory().generateFilter(source, environment) : null;

        DefaultActionSource triggerActionSource = new DefaultActionSource();
        EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

        // The trigger is carried out as the removed card and its owner (BuiltLotroCardBlueprint), so the card
        // that did the removing is read from the result and not from the context.  A card removed by no card at
        // all (a rule's discard) does not set the trigger off.
        triggerActionSource.addPlayRequirement(
                actionContext -> {
                    if(bySource == null)
                        return true;

                    final PhysicalCard remover = getRemovingCard(actionContext.getEffectResult());
                    if(remover == null)
                        return false;

                    return Filters.accepts(actionContext.getGame(), remover, bySource.getFilterable(actionContext));
                });

        if(method.toLowerCase().contains("discard") || method.isEmpty()) {
            if (optional) {
                blueprint.setDiscardedFromPlayOptionalTriggerAction(triggerActionSource);
            }
            else {
                blueprint.setDiscardedFromPlayRequiredTriggerAction(triggerActionSource);
            }
        }

        if(method.toLowerCase().contains("hinder") || method.isEmpty()) {
            if (optional) {
                blueprint.setHinderedFromPlayOptionalTriggerAction(triggerActionSource);
            }
            else {
                blueprint.setHinderedFromPlayRequiredTriggerAction(triggerActionSource);
            }
        }
    }

    private static PhysicalCard getRemovingCard(EffectResult effectResult) {
        if (effectResult instanceof DiscardCardsFromPlayResult discardResult)
            return discardResult.getSource();
        if (effectResult instanceof HinderedResult hinderedResult)
            return hinderedResult.getSource();
        return null;
    }
}
