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

        // Whether the definition says what must have done the removing.
        final boolean anySource = value.get("source") == null;

        // The trigger is carried out as the removed card and its owner (BuiltLotroCardBlueprint), so the card
        // that did the removing is read from the result and not from the context.
        //
        // With no "source:", any discard from play sets the trigger off, the ones no card did included: the
        // end-of-turn discard of minions, and a card discarded with its bearer (Ketura on Q-0081: "I would
        // expect that end-of-turn discard to produce a triggerable response").  A "source:" is a condition on
        // the card that did the discarding, and a discard by no card does not meet it.  A character that was
        // killed was not discarded (CR 4.2 "killed"), though it is reported as having gone to the discard pile.
        triggerActionSource.addPlayRequirement(
                actionContext -> {
                    final EffectResult effectResult = actionContext.getEffectResult();
                    if (effectResult instanceof DiscardCardsFromPlayResult discardResult && discardResult.isKilled())
                        return false;

                    final PhysicalCard remover = getRemovingCard(effectResult);
                    if (remover == null)
                        return anySource && effectResult instanceof DiscardCardsFromPlayResult;

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
