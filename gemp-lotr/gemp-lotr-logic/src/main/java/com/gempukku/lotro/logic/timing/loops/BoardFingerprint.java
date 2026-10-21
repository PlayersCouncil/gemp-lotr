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
 * It is taken only where an alternation of actions has repeated itself (LoopTracker), never on every action; for a
 * loop of one player's actions (OnePlayerLoopTracker), with each action chosen, and without the order of the decks.
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
        return of(game, true);
    }

    /**
     * Takes the fingerprint of the board as it stands, the order of the draw decks and how often they were shuffled
     * left out: which cards are in each deck is kept.  This is the board as the loop step compares it for a loop of
     * one player's actions (OnePlayerLoopTracker), where the CR 4.2 example, Sam, Faithful Companion with no Bill
     * the Pony to find, looks through the deck and shuffles it each time and is a loop all the same.
     * @param game The game.
     * @return The fingerprint.
     */
    public static BoardFingerprint ignoringDeckOrder(LotroGame game) {
        return of(game, false);
    }

    private static BoardFingerprint of(LotroGame game, boolean deckOrderCounts) {
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
                    .append(deckOrderCounts ? gameState.getDeckShuffleCount(player) : "-").append(":D");
            if (deckOrderCounts) {
                for (PhysicalCard card : gameState.getDeck(player))
                    sb.append(card.getCardId()).append(',');
            } else {
                sb.append(idsOf(gameState.getDeck(player)));
            }
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
            // A token counted 0 (a wound healed) is no token: the card is as it was before it had one.
            final Map<Token, Integer> tokens = new TreeMap<>();
            for (Map.Entry<Token, Integer> token : gameState.getTokens(card).entrySet()) {
                if (token.getValue() != null && token.getValue() != 0)
                    tokens.put(token.getKey(), token.getValue());
            }
            if (!tokens.isEmpty())
                sb.append(':').append(tokens);
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
