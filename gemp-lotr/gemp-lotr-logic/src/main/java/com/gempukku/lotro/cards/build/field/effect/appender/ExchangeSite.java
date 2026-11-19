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
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.PlaySiteEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
                                    GameState gameState = game.getGameState();
                                    String card1Controller = card1.getCardController();
                                    String card2Controller = card2.getCardController();
                                    //What is on a controlled site goes with its controller to the site received,
                                    //as when a controlled site is replaced (Ketura, Q-0404).  Taken before anything
                                    //moves, so that what is on the one site is not confused with what is on the other
                                    List<PhysicalCard> card1Stacked = card1Controller != null ? new LinkedList<>(gameState.getStackedCards(card1)) : Collections.emptyList();
                                    List<PhysicalCard> card1PlayedOn = card1Controller != null ? new LinkedList<>(gameState.getAttachedCards(card1)) : Collections.emptyList();
                                    List<PhysicalCard> card2Stacked = card2Controller != null ? new LinkedList<>(gameState.getStackedCards(card2)) : Collections.emptyList();
                                    List<PhysicalCard> card2PlayedOn = card2Controller != null ? new LinkedList<>(gameState.getAttachedCards(card2)) : Collections.emptyList();

                                    //The fellowship's current site may be exchanged (Ketura, Q-0403): the fellowship
                                    //is then at the site given, whose text is started in place of the other's
                                    gameState.exchangeSiteNumbers(game, card1, card2);
                                    //An exchange gives up the site handed over: each site that had a controller
                                    //first goes back onto the adventure path, nobody's, in the other's place; only
                                    //then does each controller take the site received (F-0720: the site given was
                                    //left controlled in the support area, so its controller held two sites)
                                    if (card1Controller != null)
                                        gameState.loseControlOfCard(card1, Zone.ADVENTURE_PATH);
                                    if (card2Controller != null)
                                        gameState.loseControlOfCard(card2, Zone.ADVENTURE_PATH);
                                    if (card1Controller != null)
                                        gameState.takeControlOfCard(card1Controller, game, card2, Zone.SUPPORT);
                                    if (card2Controller != null)
                                        gameState.takeControlOfCard(card2Controller, game, card1, Zone.SUPPORT);

                                    String performingPlayer = actionContext.getPerformingPlayer();
                                    PlaySiteEffect.moveCardsOnSite(game, performingPlayer, card1Stacked, card1PlayedOn, card2);
                                    PlaySiteEffect.moveCardsOnSite(game, performingPlayer, card2Stacked, card2PlayedOn, card1);
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
