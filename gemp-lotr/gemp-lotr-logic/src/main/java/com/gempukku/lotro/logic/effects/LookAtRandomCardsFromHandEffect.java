package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collections;
import java.util.List;

public abstract class LookAtRandomCardsFromHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final int _count;
    private final String _actingPlayer;
    private final String _playerHand;

    public LookAtRandomCardsFromHandEffect(String actingPlayer, String handOfPlayer, PhysicalCard source, int count) {
        _source = source;
        _count = count;
        _actingPlayer = actingPlayer;
        _playerHand = handOfPlayer;
    }

    @Override
    public String getText(LotroGame game) {
        return "Look at random cards from hand";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        if (game.getGameState().getHand(_playerHand).size() < _count)
            return false;
        return _actingPlayer.equals(_playerHand) || game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _playerHand, _actingPlayer);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _playerHand, _actingPlayer)) {
            List<PhysicalCard> randomCards = GameUtils.getRandomCards(game, _playerHand, Zone.HAND, game.getGameState().getHand(_playerHand), _count);

            if (randomCards.size() > 0) {
                game.getUserFeedback().sendAwaitingDecision(_actingPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Random cards from opponent's hand", randomCards, Collections.emptyList(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });

                game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " looked at " + randomCards.size() + " cards from " + _playerHand + " hand at random");
            }
            else {
                game.getGameState().sendMessage("No cards in " + _playerHand + " hand to look at");
            }

            cardsSeen(randomCards);

            return new FullEffectResult(randomCards.size() == _count);
        }
        return new FullEffectResult(false);
    }

    protected abstract void cardsSeen(List<PhysicalCard> revealedCards);
}
