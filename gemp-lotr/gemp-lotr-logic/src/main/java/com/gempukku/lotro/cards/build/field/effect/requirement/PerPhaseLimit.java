package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class PerPhaseLimit implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "limit", "phase", "perPlayer");

        Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");
        final ValueSource limitSource = ValueResolver.resolveEvaluator(object.get("limit"), 1, environment);
        final boolean perPlayer = FieldUtils.getBoolean(object.get("perPlayer"), "perPlayer", false);

        return (actionContext) -> {
            final int limit = limitSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            if (perPlayer)
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, actionContext.getPerformingPlayer() + "_", limit);
            else
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limit);
        };
    }
}
