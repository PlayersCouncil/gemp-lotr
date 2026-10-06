package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collections;

public class ReplaceCardInPlayWithCardInDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "selectInPlay", "selectInDiscard");

        final String selectInPlay = FieldUtils.getString(effectObject.get("selectInPlay"), "selectInPlay");
        final String selectInDiscard = FieldUtils.getString(effectObject.get("selectInDiscard"), "selectInDiscard");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(selectInPlay, actionContext -> new ConstantEvaluator(1), "_tempHand", "you", "Choose card to replace", environment));
        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(selectInDiscard, actionContext -> new ConstantEvaluator(1), "_tempDiscard", "you", "Choose card from discard to replace the copy in play", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        String performingPlayer = actionContext.getPerformingPlayer();
                        var playMemory = actionContext.getCardsFromMemory("_tempHand").stream().findFirst();
                        var discardMemory = actionContext.getCardsFromMemory("_tempDiscard").stream().findFirst();

                        return new AbstractEffect() {
                            @Override
                            protected FullEffectResult playEffectReturningResult(LotroGame game) {
                                // Nothing chosen to replace, nothing chosen to replace it with, or either of them
                                // gone from where it was chosen: the effect does nothing.
                                if (!isPlayableInFull(game)) {
                                    return new FullEffectResult(false);
                                }

                                var gameState = game.getGameState();
                                var oldCharacter = playMemory.get();
                                var newCharacter = discardMemory.get();
                                var oldZone = oldCharacter.getZone();

                                gameState.replaceCharacterOnTable(game, oldCharacter, newCharacter);
                                // The old card waits in the void; it leaves there for the discard pile.
                                gameState.removeCardsFromZone(performingPlayer, Collections.singleton(oldCharacter));
                                gameState.addCardToZone(game, oldCharacter, Zone.DISCARD);
                                gameState.sendMessage(performingPlayer + " replaces " + GameUtils.getCardLink(oldCharacter) + " with " + GameUtils.getCardLink(newCharacter));

                                //game.getActionsEnvironment().emitEffectResult(new CharacterReplacedResult(_playerPerforming, oldCharacter, newCharacter));

                                return new FullEffectResult(newCharacter.getZone() == oldZone);
                            }

                            @Override
                            public boolean isPlayableInFull(LotroGame game) {
                                if(playMemory.isEmpty() || discardMemory.isEmpty()) {
                                    return false;
                                }

                                var oldZone = playMemory.get().getZone();
                                return oldZone != null && oldZone.isInPlay() && discardMemory.get().getZone() == Zone.DISCARD;
                            }
                        };
                    }
                });


        return result;
    }
}
