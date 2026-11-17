package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class DiscardCardsFromPlayResult extends EffectResult {
    private final PhysicalCard _source;
    private final String _performingPlayer;
    private final PhysicalCard _card;
    private final boolean _killed;

    public DiscardCardsFromPlayResult(PhysicalCard source, String performingPlayer, PhysicalCard card) {
        this(source, performingPlayer, card, false);
    }

    /**
     * @param killed True for a character that was killed and placed in the discard pile (a minion, or a Free
     * Peoples character whose dead pile is the discard pile).  It has left play for the discard pile and is
     * reported here for that, but by rule it was killed and not discarded (CR 4.2 "killed": "cards that trigger
     * when a character is killed will not trigger when a character is discarded, and vice-versa").
     */
    public DiscardCardsFromPlayResult(PhysicalCard source, String performingPlayer, PhysicalCard card, boolean killed) {
        super(EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY);
        _source = source;
        _performingPlayer = performingPlayer;
        _card = card;
        _killed = killed;
    }

    public boolean isKilled() {
        return _killed;
    }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }

    public PhysicalCard getSource() {
        return _source;
    }
}
