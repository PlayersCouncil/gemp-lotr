package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
import org.json.simple.JSONObject;

public class PlayedInOtherPhase implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase", "requires", "cost");

        final Phase phase = FieldUtils.getEnum(Phase.class, value.get("phase"), "phase");
        if (phase == null)
            throw new InvalidCardDefinitionException("Phase is required on PlayedInOtherPhase effects.");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(value.get("requires"), "requires");

        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);

        blueprint.appendPlayInOtherPhaseCondition(
                new Requirement() {
                    @Override
                    public boolean accepts(ActionContext actionContext) {
                        if (actionContext.getGame().getGameState().getCurrentPhase() != phase)
                            return false;

                        for (Requirement condition : conditions) {
                            if (!condition.accepts(actionContext))
                                return false;
                        }

                        return true;
                    }
                },
                costAppenders);
    }
}
