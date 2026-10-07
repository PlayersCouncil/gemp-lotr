package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

/**
 * A card that is being played has been placed and is in play; its play has not been announced yet (CR 4.2
 * "playing a card": "The Card is now in play", the step before "Respond to the playing of The Card").
 *
 * No trigger or response answers this by name.  It is published so that the texts that watch the table all the
 * time ("While you can spot Watcher in the Water, discard all other minions", "... is discarded if not at a
 * marsh") act on the card before anything answers its being played, and so that a card they remove is never
 * announced as played at all.  The play itself is published afterwards, as a PlayCardResult.
 */
public class EnteredPlayResult extends EffectResult {
    private final PhysicalCard _card;

    public EnteredPlayResult(PhysicalCard card) {
        super(Type.ENTERS_PLAY);
        _card = card;
    }

    public PhysicalCard getCard() {
        return _card;
    }
}
