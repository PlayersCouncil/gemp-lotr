package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.ExtraPlayCost;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.Condition;
import org.json.simple.JSONObject;

public class ExtraCost implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "cost", "skipValidate");

        boolean skipValidate = FieldUtils.getBoolean(value.get("skipValidate"), "skipValidate", false);

        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");
        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);

        blueprint.appendExtraPlayCost(
                (actionContext) -> new ExtraPlayCost() {
                    @Override
                    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
                        for (EffectAppender costAppender : costAppenders) {
                            costAppender.appendEffect(true, action, actionContext);
                        }
                    }

                    @Override
                    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
                        if (skipValidate)
                            return true;
                        for (EffectAppender appender : costAppenders) {
                            if (!appender.isPlayableInFull(actionContext))
                                return false;
                        }
                        return true;
                    }

                    @Override
                    public Condition getCondition() {
                        return null;
                    }
                });
    }
}
