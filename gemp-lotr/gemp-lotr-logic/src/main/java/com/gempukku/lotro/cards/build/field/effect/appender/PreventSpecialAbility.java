package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.results.ActionSource;
import org.json.simple.JSONObject;

public class PreventSpecialAbility implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        return new UnrespondableEffect() {
                            @Override
                            protected void doPlayEffect(LotroGame game) {
                                EffectResult effectResult = actionContext.getEffectResult();
                                if (effectResult instanceof ActionSource) {
                                    var cardAction = ((ActionSource) effectResult).getAction();
                                    ActivateCardAction act = null;

                                    if(cardAction instanceof ActivateCardAction activate) {
                                        act = activate;
                                    }
                                    else if(cardAction instanceof SubAction sub) {
                                        var coreAction = sub.getAction();
                                        if(coreAction instanceof ActivateCardAction activate) {
                                            act = activate;
                                        }
                                    }
                                    if(act != null) {
                                        act.prevent();
                                    }
                                }
                            }
                        };
                    }
                });

        return result;
    }

}
