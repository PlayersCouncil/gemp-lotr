package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

public class AidCost implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "cost");

        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");

        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(true, costArray, environment);

        if (costAppenders.length == 0)
            throw new InvalidCardDefinitionException("At least one cost is required on AidCost effects.");

        blueprint.setAidCostSource(
                new AidCostSource() {
                    @Override
                    public boolean canPayAidCost(DefaultActionContext actionContext) {
                        for (EffectAppender costAppender : costAppenders) {
                            if (!costAppender.isPlayableInFull(actionContext))
                                return false;
                        }

                        return true;
                    }

                    @Override
                    public void appendAidCost(CostToEffectAction action, DefaultActionContext actionContext) {
                        for (EffectAppender costAppender : costAppenders)
                            costAppender.appendEffect(true, action, actionContext);
                    }
                });
    }
}
