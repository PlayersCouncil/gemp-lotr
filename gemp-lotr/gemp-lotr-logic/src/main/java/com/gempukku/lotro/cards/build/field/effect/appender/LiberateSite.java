package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.LiberateASiteEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class LiberateSite implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize", "controller");

        String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        String controller = FieldUtils.getString(effectObject.get("controller"), "controller");

        PlayerSource controllerSource = controller != null ? PlayerResolver.resolvePlayer(controller) : null;

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                String controllerPlayerId = controllerSource != null ? controllerSource.getPlayer(actionContext) : null;
                return PlayConditions.canLiberateASite(actionContext.getGame(), actionContext.getPerformingPlayer(), actionContext.getSource(), controllerPlayerId);
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                String controllerPlayerId = controllerSource != null ? controllerSource.getPlayer(actionContext) : null;

                return new LiberateASiteEffect(action.getActionSource(), action.getPerformingPlayer(), controllerPlayerId) {
                    @Override
                    public void liberatedSiteCallback(PhysicalCard liberatedSite) {
                        if (memory != null) {
                            actionContext.setCardMemory(memory, liberatedSite);
                        }
                    }
                };
            }
        };
    }
}
