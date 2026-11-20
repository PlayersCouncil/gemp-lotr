package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.GameRandom;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.scenario.ScenarioDefinition.RngEntry;
import com.gempukku.lotro.scenario.ScenarioDefinition.RngKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A scenario decides its own random outcomes.  Each shuffle or random pick takes the next outcome the scenario
 * listed under "rng"; when there is none left, the scenario's rngDefault applies:
 *
 *     identity   a shuffle leaves the deck as it is, and a random pick takes the first cards of the pile
 *     random     the usual randomness
 *
 * A listed outcome that does not fit the random event it meets (a pick where a shuffle was expected, or the wrong
 * pile) is a mistake in the scenario.  It is reported through problems(), the listed outcome is left where it is, and
 * the event falls back to rngDefault.
 */
public class ScenarioRandom implements GameRandom {
    private final ScenarioTable _table;
    private final LinkedList<RngEntry> _queue;
    private final boolean _trulyRandom;
    private final List<String> _problems = new ArrayList<>();

    public ScenarioRandom(ScenarioTable table, ScenarioDefinition def) {
        _table = table;
        _queue = new LinkedList<>(def.rng);
        _trulyRandom = def.rngDefault == ScenarioDefinition.RngDefault.RANDOM;
    }

    /** Listed outcomes that have not been used yet. */
    public List<RngEntry> remaining() {
        return List.copyOf(_queue);
    }

    public List<String> problems() {
        return _problems;
    }

    @Override
    public void shuffleDeck(String playerId, List<? extends PhysicalCard> deck) {
        RngEntry entry = next(RngKind.SHUFFLE, playerId, "deck");
        if (entry == null) {
            if (_trulyRandom)
                GameRandom.DEFAULT.shuffleDeck(playerId, deck);
            return;
        }

        //The listed cards go on top in the order given; everything else keeps its order underneath.
        List<PhysicalCard> top = new ArrayList<>();
        for (String alias : entry.result()) {
            PhysicalCard card = _table.Card(alias);
            if (!deck.contains(card)) {
                _problems.add("rng: shuffle(" + describe(entry) + ") lists '" + alias + "', which was not in that deck when it was shuffled");
                continue;
            }
            top.add(card);
        }
        reorder(deck, top);
    }

    @SuppressWarnings("unchecked")
    private static <T extends PhysicalCard> void reorder(List<T> deck, List<PhysicalCard> top) {
        List<T> rest = new ArrayList<>(deck);
        rest.removeAll(top);
        deck.clear();
        for (PhysicalCard card : top)
            deck.add((T) card);
        deck.addAll(rest);
    }

    @Override
    public List<PhysicalCard> pickCards(String playerId, Zone zone, Collection<? extends PhysicalCard> cards, int count) {
        RngEntry entry = next(RngKind.PICK, playerId, zoneName(zone));
        if (entry == null) {
            if (_trulyRandom)
                return GameRandom.DEFAULT.pickCards(playerId, zone, cards, count);
            List<PhysicalCard> inOrder = new ArrayList<>(cards);
            return new LinkedList<>(inOrder.subList(0, Math.min(count, inOrder.size())));
        }

        List<PhysicalCard> picked = new LinkedList<>();
        for (String alias : entry.result()) {
            PhysicalCard card = _table.Card(alias);
            if (!cards.contains(card))
                _problems.add("rng: pick(" + describe(entry) + ") lists '" + alias + "', which was not there to be picked");
            else if (picked.size() < count)
                picked.add(card);
        }
        if (picked.size() != Math.min(count, cards.size()))
            _problems.add("rng: pick(" + describe(entry) + ") supplies " + picked.size() + " card(s) where the game picks "
                    + Math.min(count, cards.size()));
        return picked;
    }

    @Override
    public void shufflePlayers(List<String> players) {
        //Scenarios seat their players deliberately, so a tie is never left to chance.
        if (_trulyRandom)
            GameRandom.DEFAULT.shufflePlayers(players);
    }

    private RngEntry next(RngKind kind, String playerId, String zone) {
        RngEntry entry = _queue.peek();
        if (entry == null)
            return null;
        if (entry.kind() != kind || !_table.Player(entry.seat()).equals(playerId) || !entry.zone().equalsIgnoreCase(zone)) {
            _problems.add("rng: the next listed outcome is " + entry.kind().name().toLowerCase() + "(" + describe(entry)
                    + "), but the game " + (kind == RngKind.SHUFFLE ? "shuffled" : "picked at random from") + " "
                    + _table.SeatOf(playerId).key() + "." + zone);
            return null;
        }
        return _queue.poll();
    }

    private static String zoneName(Zone zone) {
        return switch (zone) {
            case HAND -> "hand";
            case ADVENTURE_DECK -> "adventureDeck";
            case DECK -> "deck";
            default -> zone.name();
        };
    }

    private static String describe(RngEntry entry) {
        return entry.seat().key() + "." + entry.zone() + ": " + String.join(", ", entry.result());
    }
}
