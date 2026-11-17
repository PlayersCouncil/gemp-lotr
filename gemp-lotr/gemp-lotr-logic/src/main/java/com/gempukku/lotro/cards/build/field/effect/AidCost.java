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

        final Integer twilightCost = getTwilightCost(costArray);

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

                    @Override
                    public Integer getTwilightCost() {
                        return twilightCost;
                    }
                });
    }

    /**
     * The N of "Aid - (N)", read off the definition: see AidCostSource.getTwilightCost.  The Aid has to be one
     * cost, an AddTwilight or a RemoveTwilight, whose amount is a number written out (or left out: both effects
     * then take 1).  An amount that is worked out in play (a range, a count, a memory) is no number to keep.
     */
    private static Integer getTwilightCost(JSONObject[] costArray) throws InvalidCardDefinitionException {
        if (costArray.length != 1)
            return null;

        final String type = FieldUtils.getString(costArray[0].get("type"), "type");
        if (!"addtwilight".equalsIgnoreCase(type) && !"removetwilight".equalsIgnoreCase(type))
            return null;

        final Object amount = costArray[0].get("amount");
        if (amount == null)
            return 1;
        if (amount instanceof Number number)
            return number.intValue();
        if (amount instanceof String text && text.matches("\\d+"))
            return Integer.parseInt(text);
        return null;
    }
}
