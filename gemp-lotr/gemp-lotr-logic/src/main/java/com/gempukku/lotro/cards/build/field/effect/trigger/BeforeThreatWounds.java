package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.effects.ThreatWoundsEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.KilledResult;
import org.json.simple.JSONObject;

import java.util.Set;

public class BeforeThreatWounds implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "killedFilter", "deathCause");

        String filter = FieldUtils.getString(value.get("killedFilter"), "killedFilter");
        KillEffect.Cause deathCause = FieldUtils.getEnum(KillEffect.Cause.class, value.get("deathCause"), "deathCause");

        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                Effect effect = actionContext.getEffect();
                if (effect.getType() == Effect.Type.BEFORE_THREAT_WOUNDS) {
                    ThreatWoundsEffect threatWoundsEffect = (ThreatWoundsEffect) effect;
                    KilledResult killResult = threatWoundsEffect.getKillResult();
                    if (deathCause == null || killResult.getCause() == deathCause) {
                        Set<PhysicalCard> killedCards = killResult.getKilledCards();
                        return !Filters.filter(killedCards, actionContext.getGame(), affectedFilter.getFilterable(actionContext)).isEmpty();
                    }
                }
                return false;
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
