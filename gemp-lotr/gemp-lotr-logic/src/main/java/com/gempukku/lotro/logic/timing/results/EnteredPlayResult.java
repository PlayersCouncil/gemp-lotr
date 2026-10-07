package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

/**
 * A card that is being played has been placed and is in play; its play has not been announced yet (CR 4.2
 * "playing a card": "The Card is now in play", the step before "Respond to the playing of The Card").
 *
 * It is published twice for a play, and the two are the entering step of the play.
 *
 * The first is for the texts that watch the table all the time ("While you can spot Watcher in the Water, discard
 * all other minions", "... is discarded if not at a marsh"): they act on the card before anything answers its
 * being played, and a card they remove is never announced as played at all.  Nothing answers the first by name.
 *
 * The second, isOwnText(), is published if the card is still in play after the first, and is answered by the
 * card's own EntersPlay trigger and nothing else: its "When you play this ..., <do or pay something> or discard
 * it" (Dunlending Rampager, Stampeding Chief).  A card that its own text discards there was not played either
 * (Ketura, 2026-10-07, on F-0139: "a self-discarding card should not trigger Attea or other 'each time you play'
 * or 'when you play', that's pretty straightforwardly the same situation as Runner, the only difference being
 * where the reaction text is").
 *
 * The play itself is published afterwards, as a PlayCardResult, for a card that is still in play.
 */
public class EnteredPlayResult extends EffectResult {
    private final PhysicalCard _card;
    private final Zone _playedFrom;
    private final boolean _ownText;

    public EnteredPlayResult(PhysicalCard card, Zone playedFrom, boolean ownText) {
        super(Type.ENTERS_PLAY);
        _card = card;
        _playedFrom = playedFrom;
        _ownText = ownText;
    }

    public PhysicalCard getCard() {
        return _card;
    }

    public Zone getPlayedFrom() {
        return _playedFrom;
    }

    /**
     * Whether this is the second batch of the entering step, the one the card's own EntersPlay trigger answers.
     */
    public boolean isOwnText() {
        return _ownText;
    }
}
