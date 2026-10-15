package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The board as the loop step (CR 4.2 "loops") compares it: what a round of a loop must give back exactly for the
 * round to be one that can be repeated for ever.  Two fingerprints are equal when every card is in the same zone,
 * on the same bearer or stack, under the same controller, with the same tokens (wounds, burdens, culture tokens)
 * and hindered or not alike; the twilight pool, the threats, each player's place on the path, the order of each
 * draw deck and how often it was shuffled, the skirmish and the assignments are the same; and the phase, site and
 * move count are the same.  It leaves out the modifiers (the strength bonuses and the like that a loop piles up),
 * the limit counters (an ability with a limit is never part of a loop: LoopTracker), and the phase's and turn's
 * lists of what has happened.
 * <p>
 * It is taken only where an alternation of actions has repeated itself (LoopTracker), never on every action.
 */
public final class BoardFingerprint {
    private final String _value;

    private BoardFingerprint(String value) {
        _value = value;
    }

    /**
     * Takes the fingerprint of the board as it stands.
     * @param game The game.
     * @return The fingerprint.
     */
    public static BoardFingerprint of(LotroGame game) {
        final GameState gameState = game.getGameState();
        final StringBuilder sb = new StringBuilder();
        sb.append(gameState.getCurrentPhase()).append('|')
                .append(gameState.getCurrentSiteNumber()).append('|')
                .append(gameState.getMoveCount()).append('|')
                .append(gameState.isWearingRing()).append('|')
                .append(gameState.getTwilightPool()).append('|')
                .append(gameState.getThreats()).append('|');

        for (String player : gameState.getPlayerOrder().getAllPlayers()) {
            sb.append('P').append(player).append(':')
                    .append(gameState.getPlayerPosition(player)).append(':')
                    .append(gameState.getDeckShuffleCount(player)).append(":D");
            for (PhysicalCard card : gameState.getDeck(player))
                sb.append(card.getCardId()).append(',');
            sb.append('|');
        }

        final List<PhysicalCard> cards = new ArrayList<>();
        for (PhysicalCard card : gameState.getAllCards())
            cards.add(card);
        cards.sort(Comparator.comparingInt(PhysicalCard::getCardId));
        for (PhysicalCard card : cards) {
            sb.append('C').append(card.getCardId())
                    .append(':').append(card.getZone())
                    .append(':').append(card.getCardController())
                    .append(':').append(idOf(card.getAttachedTo()))
                    .append(':').append(idOf(card.getStackedOn()))
                    .append(':').append(card.getSiteNumber())
                    .append(':').append(card.isFlipped())
                    .append(':').append(gameState.isHindered(card));
            final Map<Token, Integer> tokens = gameState.getTokens(card);
            if (!tokens.isEmpty())
                sb.append(':').append(new TreeMap<>(tokens));
            final PhysicalCard.WhileInZoneData data = card.getWhileInZoneData();
            if (data != null)
                sb.append(':').append(data.getValue());
            sb.append('|');
        }

        final Skirmish skirmish = gameState.getSkirmish();
        if (skirmish != null) {
            sb.append("S:").append(idOf(skirmish.getFellowshipCharacter())).append(':')
                    .append(idsOf(skirmish.getShadowCharacters())).append(':')
                    .append(skirmish.isCancelled()).append('|');
        }
        for (Assignment assignment : gameState.getAssignments())
            sb.append("A:").append(idOf(assignment.getFellowshipCharacter())).append(':')
                    .append(idsOf(assignment.getShadowCharacters())).append('|');

        return new BoardFingerprint(sb.toString());
    }

    private static String idOf(PhysicalCard card) {
        return card == null ? "-" : String.valueOf(card.getCardId());
    }

    private static String idsOf(Iterable<? extends PhysicalCard> cards) {
        final List<Integer> ids = new ArrayList<>();
        for (PhysicalCard card : cards)
            ids.add(card.getCardId());
        ids.sort(null);
        return ids.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BoardFingerprint other && _value.equals(other._value);
    }

    @Override
    public int hashCode() {
        return _value.hashCode();
    }

    @Override
    public String toString() {
        return _value;
    }
}
