package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.ExtraAssignmentCostModifier;
import org.json.simple.JSONObject;

/**
 * "The Free Peoples player must <cost> to assign <minion> to skirmish <fpCharacter>": the twin of ExtraCostToPlay
 * for an assignment (Q-0221).  The cost is the Free Peoples player's, so its effects say `player: fp`; it is run
 * in the context of the card that has the modifier.
 */
public class ExtraCostToAssign implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "cost", "minion", "fpCharacter");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String minion = FieldUtils.getString(object.get("minion"), "minion");
        final String fpCharacter = FieldUtils.getString(object.get("fpCharacter"), "fpCharacter");
        final JSONObject[] costArray = FieldUtils.getObjectArray(object.get("cost"), "cost");

        if (minion == null)
            throw new InvalidCardDefinitionException("minion is required on ExtraCostToAssign modifiers.");

        final FilterableSource minionSource = environment.getFilterFactory().generateFilter(minion, environment);
        final FilterableSource fpCharacterSource = (fpCharacter != null)
                ? environment.getFilterFactory().generateFilter(fpCharacter, environment) : null;
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(true, costArray, environment);

        if (costAppenders.length == 0)
            throw new InvalidCardDefinitionException("At least one cost is required on ExtraCostToAssign modifiers.");

        return (actionContext) -> {
            final Filterable minionFilterable = minionSource.getFilterable(actionContext);
            final Filterable fpCharacterFilterable = (fpCharacterSource != null) ? fpCharacterSource.getFilterable(actionContext) : null;

            return new ExtraAssignmentCostModifier(actionContext.getSource(), minionFilterable,
                    RequirementCondition.createCondition(requirements, actionContext), fpCharacterFilterable) {
                @Override
                protected boolean canPay(LotroGame game) {
                    for (EffectAppender costAppender : costAppenders) {
                        if (!costAppender.isPlayableInFull(actionContext))
                            return false;
                    }

                    return true;
                }

                @Override
                protected void appendCosts(LotroGame game, CostToEffectAction action) {
                    for (EffectAppender costAppender : costAppenders)
                        costAppender.appendEffect(true, action, actionContext);
                }
            };
        };
    }
}
