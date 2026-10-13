package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.results.RevealCardFromHandResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RevealHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _actingPlayer;
    private final String _handPlayerId;

    public RevealHandEffect(PhysicalCard source, String actingPlayer, String handPlayerId) {
        _source = source;
        _actingPlayer = actingPlayer;
        _handPlayerId = handPlayerId;
    }

    @Override
    public String getText(LotroGame game) {
        return "Reveal cards from hand";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _handPlayerId, _actingPlayer);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _handPlayerId, _actingPlayer)) {
            final List<? extends PhysicalCard> hand = game.getGameState().getHand(_handPlayerId);
            if (hand.isEmpty()) {
                // An empty hand is shown to nobody: the game log says there was nothing to reveal (Q-0144).
                game.getGameState().sendMessage(GameUtils.getNoCardsRevealedFromHandMessage(game, _source, _handPlayerId));
            } else {
                game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed " + _handPlayerId + " cards in hand - " + getAppendedNames(hand));

                final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_handPlayerId, false);
                // Skip hand owner
                playerOrder.getNextPlayer();

                String nextPlayer;
                while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                    game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, "Hand of " + _handPlayerId, hand, Collections.emptySet(), 0, 0) {
                                @Override
                                public void decisionMade(String result) {
                                }
                            });
                }
            }

            cardsRevealed(hand);

            for (PhysicalCard card : hand) {
                game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _handPlayerId, card));
            }

            return new FullEffectResult(true);
        } else {
            return new FullEffectResult(false);
        }
    }

    protected void cardsRevealed(Collection<? extends PhysicalCard> cards) {

    }
}
