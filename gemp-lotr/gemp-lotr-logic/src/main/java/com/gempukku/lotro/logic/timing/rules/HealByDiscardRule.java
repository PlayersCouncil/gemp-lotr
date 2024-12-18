package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.DiscardToHealAction;
import com.gempukku.lotro.logic.effects.DiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.HealCharactersEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HealByDiscardRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public HealByDiscardRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
                        if (game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP
                                && GameUtils.isFP(game, playerId)) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(game, game.getGameState().getHand(playerId), Filters.or(CardType.COMPANION, CardType.ALLY), Filters.unique)) {
                                PhysicalCard active = Filters.findFirstActive(game, Filters.name(card.getBlueprint().getTitle()));
                                if (active != null && game.getGameState().getWounds(active) > 0) {
                                    var action = new DiscardToHealAction(card);
                                    action.appendCost(new DiscardCardsFromHandEffect(null, card.getOwner(), Collections.singleton(card), false));
                                    action.appendCost(
                                            new UnrespondableEffect() {
                                                @Override
                                                protected void doPlayEffect(LotroGame game) {
                                                    game.getGameState().eventPlayed(card);
                                                }
                                            });

                                    action.appendEffect(new HealCharactersEffect(card, card.getOwner(), active));
                                    result.add(action);
                                }
                            }
                            return result;
                        }
                        return null;
                    }
                });
    }
}
