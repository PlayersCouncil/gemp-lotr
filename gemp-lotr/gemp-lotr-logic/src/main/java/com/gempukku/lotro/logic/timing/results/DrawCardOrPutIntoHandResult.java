package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.logic.timing.EffectResult;

/**
 * A card has reached a player's hand from a draw deck, a discard pile, a stack or the removed pile.  isDraw() is
 * true for a card drawn, and for a card taken into hand in place of a draw (Q-0205: B); for every other card
 * taken into hand it is false.  The definition word playerDrawsCard answers the first kind alone unless it says
 * orTakesIntoHand (F-0376).
 */
public class DrawCardOrPutIntoHandResult extends EffectResult {
    private final String _playerId;
    private final boolean _draw;

    public DrawCardOrPutIntoHandResult(String playerId) {
        this(playerId, false);
    }

    public DrawCardOrPutIntoHandResult(String playerId, boolean draw) {
        super(EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND);
        _playerId = playerId;
        _draw = draw;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public boolean isDraw() {
        return _draw;
    }
}
