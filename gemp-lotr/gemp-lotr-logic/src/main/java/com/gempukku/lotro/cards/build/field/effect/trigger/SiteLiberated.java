package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.SiteControlledResult;
import com.gempukku.lotro.logic.timing.results.SiteLiberatedResult;
import org.json.simple.JSONObject;

public class SiteLiberated implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                EffectResult effectResult = actionContext.getEffectResult();
                return effectResult.getType() == EffectResult.Type.LIBERATE_SITE;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
