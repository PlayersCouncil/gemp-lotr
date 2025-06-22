package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class RevealedCardFromDeckResult extends EffectResult {
    private final String _deck;
    private final PhysicalCard _source;
    private final PhysicalCard _revealedCard;

    public RevealedCardFromDeckResult(String deck, PhysicalCard source, PhysicalCard revealedCard) {
        super(Type.FOR_EACH_REVEALED_FROM_DECK);
        _deck = deck;
        _source = source;
        _revealedCard = revealedCard;
    }

    public String getDeck() {
        return _deck;
    }
    public PhysicalCard getSource() {
        return _source;
    }

    public PhysicalCard getRevealedCard() {
        return _revealedCard;
    }
}
