package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.RevealCardFromHandResult;

import java.util.Collection;
import java.util.Collections;

public class RevealCardsFromYourHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _handPlayerId;
    private final Collection<? extends PhysicalCard> _cards;

    public RevealCardsFromYourHandEffect(PhysicalCard source, String handPlayerId, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _handPlayerId = handPlayerId;
        _cards = cards;
    }

    @Override
    public String getText(LotroGame game) {
        return "Reveal cards from hand";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        for (PhysicalCard card : _cards) {
            if (card.getZone() != Zone.HAND)
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed " + _handPlayerId + " cards in hand - " + getAppendedNames(_cards));

        final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_handPlayerId, false);
        // Skip hand owner
        playerOrder.getNextPlayer();

        String nextPlayer;
        while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
            game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                    new ArbitraryCardsSelectionDecision(1, _handPlayerId + " revealed card(s) in hand", _cards, Collections.emptySet(), 0, 0) {
                        @Override
                        public void decisionMade(String result) {
                        }
                    });
        }

        for (PhysicalCard card : _cards) {
            game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _handPlayerId, card));
        }

        return new FullEffectResult(true);
    }
}
