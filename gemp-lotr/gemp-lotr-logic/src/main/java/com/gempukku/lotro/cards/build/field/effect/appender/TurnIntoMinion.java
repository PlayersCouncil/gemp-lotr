package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TurnIntoMinion implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "count", "strength", "vitality", "keywords", "until", "memorize");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final ValueSource strength = ValueResolver.resolveEvaluator(effectObject.get("strength"), environment);
        final ValueSource vitality = ValueResolver.resolveEvaluator(effectObject.get("vitality"), environment);
        String keywords = FieldUtils.getString(effectObject.get("keywords"), "keywords");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        List<Keyword> keywordsList = new ArrayList<>();
        if (keywords != null) {
            for (String key : keywords.split(",")) {
                keywordsList.add(FieldUtils.getEnum(Keyword.class, key, "keyword"));
            }
        }

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCards(filter, valueSource, memory, "you", "Choose cards to turn into minions", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        List<Effect> effects = new ArrayList<>();
                        Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory(memory);
                        for (PhysicalCard card : cards) {
                            int strengthValue = strength.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), card);
                            int vitalityValue = vitality.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), card);
                            effects.add(
                                    new DiscardCardsFromPlayEffect(action.getPerformingPlayer(), action.getActionSource(), Filters.attachedTo(card)));
                            effects.add(
                                    new TransferToShadowEffect(card) {
                                        @Override
                                        protected void cardTransferredCallback() {
                                            addModifier(action, new StrengthModifier(card, card, strengthValue), until);
                                            addModifier(action, new VitalityModifier(card, card, vitalityValue), until);
                                            addModifier(action, new IsAdditionalCardTypeModifier(card, card, CardType.MINION), until);
                                            if (keywords != null) {
                                                for (Keyword keyword : keywordsList) {
                                                    addModifier(action, new KeywordModifier(card, card, keyword), until);
                                                }
                                            }
                                            addModifier(action, new MayNotBearModifier(card, card, Filters.any), until);
                                        }
                                    });
                            Effect cleanupEffect = createCleanupEffect(card, until);
                            if (cleanupEffect != null) {
                                effects.add(cleanupEffect);
                            }
                        }

                        return effects;
                    }
                });
        return result;
    }

    private Effect createCleanupEffect(PhysicalCard card, TimeResolver.Time until) {
        if (until.isPermanent()) {
            return null;
        } else if (until.isEndOfTurn()) {
            return new AddUntilEndOfTurnActionProxyEffect(
                    new AbstractActionProxy() {
                        @Override
                        public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                            if (TriggerConditions.endOfTurn(game, effectResult) && card.getZone() == Zone.SHADOW_CHARACTERS) {
                                return createCleanupTrigger(card);
                            }
                            return null;
                        }
                    }
            );
        } else if (until.isStart()) {
            return new AddUntilStartOfPhaseActionProxyEffect(
                    new AbstractActionProxy() {
                        @Override
                        public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                            if (TriggerConditions.startOfPhase(game, effectResult, until.getPhase()) && card.getZone() == Zone.SHADOW_CHARACTERS) {
                                return createCleanupTrigger(card);
                            }
                            return null;
                        }
                    }, until.getPhase()
            );
        } else {
            return new AddUntilEndOfPhaseActionProxyEffect(
                    new AbstractActionProxy() {
                        @Override
                        public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                            if (TriggerConditions.endOfPhase(game, effectResult, until.getPhase()) && card.getZone() == Zone.SHADOW_CHARACTERS) {
                                return createCleanupTrigger(card);
                            }
                            return null;
                        }
                    }, until.getPhase()
            );
        }
    }

    private static List<RequiredTriggerAction> createCleanupTrigger(PhysicalCard card) {
        RequiredTriggerAction action = new RequiredTriggerAction(card);
        action.appendEffect(
                new TransferToSupportEffect(card));
        return Collections.singletonList(action);
    }

    private void addModifier(CostToEffectAction action, Modifier modifier, TimeResolver.Time until) {
        if (until.isPermanent()) {
            //TODO: alter this if we need an actual end-of-game modifiers
        } else if (until.isEndOfTurn()) {
            action.appendEffect(new AddUntilEndOfTurnModifierEffect(modifier));
        } else if (until.isStart()) {
            action.appendEffect(new AddUntilStartOfPhaseModifierEffect(modifier, until.getPhase()));
        } else {
            action.appendEffect(new AddUntilEndOfPhaseModifierEffect(modifier, until.getPhase()));
        }
    }
}
