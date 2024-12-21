package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

public class ExchangeSite implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "site1", "site2");

        final String site1 = FieldUtils.getString(effectObject.get("site1"), "site1");
        final String site2 = FieldUtils.getString(effectObject.get("site2"), "site2");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(site1, "_temp1", "you", "Choose a site to exchange", environment));
        result.addEffectAppender(
                CardResolver.resolveCard(site2, "_temp2", "you", "Choose a site to exchange", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        PhysicalCard card1 = actionContext.getCardFromMemory("_temp1");
                        PhysicalCard card2 = actionContext.getCardFromMemory("_temp2");
                        if (card1 != null && card2 != null && card1 != card2) {
                            return new UnrespondableEffect() {
                                @Override
                                protected void doPlayEffect(LotroGame game) {
                                    int card1Number = card1.getSiteNumber();
                                    int card2Number = card2.getSiteNumber();
                                    card1.setSiteNumber(card2Number);
                                    card2.setSiteNumber(card1Number);
                                    String card1Controller = card1.getCardController();
                                    String card2Controller = card2.getCardController();
                                    if (card1Controller != null) {
                                        if (card2Controller != null) {
                                            game.getGameState().loseControlOfCard(card2, Zone.ADVENTURE_PATH);
                                        }
                                        game.getGameState().takeControlOfCard(card1Controller, game, card2, Zone.SUPPORT);
                                    }
                                    if (card2Controller != null) {
                                        if (card1Controller != null) {
                                            game.getGameState().loseControlOfCard(card1, Zone.ADVENTURE_PATH);
                                        }
                                        game.getGameState().takeControlOfCard(card2Controller, game, card1, Zone.SUPPORT);
                                    }
                                }
                            };
                        }
                        return null;
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        PhysicalCard card1 = actionContext.getCardFromMemory("_temp1");
                        PhysicalCard card2 = actionContext.getCardFromMemory("_temp2");
                        return card1 != null && card2 != null && card1 != card2;
                    }
                }
        );

        return result;
    }
}
