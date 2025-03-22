package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.TransferPermanentAction;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.LinkedList;
import java.util.List;

public class TransferItemRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public TransferItemRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
                        if (GameUtils.isFP(game, playerId) && game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(game, game.getGameState().getInPlay(), getTransferableCardsFilter(playerId))) {
                                if (game.getModifiersQuerying().canBeTransferred(game, card)) {
                                    final Filter validTargetFilter = RuleUtils.getFullValidTargetFilter(card.getOwner(), game, card);
                                    if (Filters.countActive(game, validTargetFilter) > 0) {
                                        Filter validTransferFilter;

                                        PhysicalCard attachedToCard = card.getAttachedTo();
                                        LotroCardBlueprint attachedTo = attachedToCard.getBlueprint();
                                        if (attachedTo.getCardType() == CardType.COMPANION) {
                                            validTransferFilter = Filters.and(validTargetFilter,
                                                    Filters.or(
                                                            CardType.COMPANION,
                                                            Filters.allyAtHome));
                                        } else if (RuleUtils.isAllyAtHome(attachedToCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock())) {
                                            validTransferFilter = Filters.and(validTargetFilter,
                                                    Filters.or(
                                                            CardType.COMPANION,
                                                            Filters.allyWithSameHome(attachedToCard)));
                                        } else {
                                            validTransferFilter = Filters.and(validTargetFilter,
                                                    Filters.allyWithSameHome(attachedToCard));
                                        }

                                        validTransferFilter = Filters.and(validTransferFilter,
                                                Filters.not(card.getAttachedTo()),
                                                new Filter() {
                                                    @Override
                                                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                                        return game.getModifiersQuerying().canHaveTransferredOn(game, card, physicalCard);
                                                    }
                                                });

                                        if (Filters.countActive(game, validTransferFilter) > 0)
                                            result.add(new TransferPermanentAction(card, card.getOwner(), validTransferFilter));
                                    }
                                }
                            }
                            return result;
                        }
                        return null;
                    }
                });
    }

    private Filter getTransferableCardsFilter(String playerId) {
        return Filters.and(Side.FREE_PEOPLE, Filters.owner(playerId), Zone.ATTACHED, Filters.or(CardType.POSSESSION, CardType.ARTIFACT), Filters.active);
    }
}
