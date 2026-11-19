package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.LiberateASiteEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

import java.util.Collection;

/**
 * "Liberate a site". With no {@code select}, the site is the rules' (CR 4.2 "liberate"): the highest-numbered one
 * controlled by {@code controller}, or with none named by a player other than the one whose turn it is, and nothing
 * is asked. With {@code select} (a choice of sites, e.g. {@code choose(controlledSite)}), the performing player
 * picks which of the sites it finds is liberated, as Ketura ruled for a Shadow card's "liberate a site (you control)"
 * (Q-0370: "they should not be able to reach over and liberate their opponent's shadow's controlled sites, but if
 * they themselves control one or more it doesn't really matter if they liberate site "3" before site "2""). The
 * filter says whose sites; {@code controller} is not written beside it.
 */
public class LiberateSite implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize", "controller", "select");

        String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        String controller = FieldUtils.getString(effectObject.get("controller"), "controller");
        String select = FieldUtils.getString(effectObject.get("select"), "select");

        if (select != null) {
            if (controller != null)
                throw new InvalidCardDefinitionException("liberateSite takes 'select' or 'controller', not both: the filter of 'select' says whose sites");
            return createChosenSiteAppender(select, memory != null ? memory : "_temp", environment);
        }

        PlayerSource controllerSource = controller != null ? PlayerResolver.resolvePlayer(controller) : null;

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                String controllerPlayerId = controllerSource != null ? controllerSource.getPlayer(actionContext) : null;
                return PlayConditions.canLiberateASite(actionContext.getGame(), actionContext.getPerformingPlayer(), actionContext.getSource(), controllerPlayerId);
            }

            // About to be carried out with no site that can be liberated, it does nothing.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                if (actionContext.isAtResolution() && !isPlayableInFull(actionContext))
                    return EffectForecast.NOTHING;
                return EffectForecast.UNKNOWN;
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

    private static EffectAppender createChosenSiteAppender(String select, String memory, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final EffectAppender siteResolver = CardResolver.resolveCards(select,
                actionContext -> (Filter) (game, card) -> PlayConditions.canLiberateChosenSite(game, actionContext.getPerformingPlayer(), actionContext.getSource(), card),
                ValueResolver.resolveEvaluator(1, environment), memory, "you", "Choose a site to liberate", environment);

        MultiEffectAppender result = new MultiEffectAppender() {
            // About to be carried out with no site of the filter's that can be liberated, it does nothing.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return EffectForecast.foundAtResolution(actionContext, siteResolver);
            }
        };
        result.addEffectAppender(siteResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> chosen = actionContext.getCardsFromMemory(memory);
                        if (chosen.isEmpty())
                            return null;
                        return new LiberateASiteEffect(action.getActionSource(), action.getPerformingPlayer(), chosen.iterator().next());
                    }
                });
        return result;
    }
}
