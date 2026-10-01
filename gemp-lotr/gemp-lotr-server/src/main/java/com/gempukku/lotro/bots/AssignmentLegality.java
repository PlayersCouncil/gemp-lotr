package com.gempukku.lotro.bots;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lets a bot check an ASSIGN_MINIONS answer against the same rules the engine applies when the answer comes back,
 * so it never offers a pairing the engine will reject (issue #1097: Uruk Guard's "can't be assigned against the
 * chosen companion" made the bot's Aragorn pairing invalid, and an invalid bot answer loses the game).
 *
 * The checks mirror FreePeoplePlayerAssignsMinionsGameProcess and ShadowPlayerAssignsHisMinionsGameProcess:
 * <ul>
 *     <li>structure: every id is one offered in the decision's freeCharacters/minions, no minion used twice;</li>
 *     <li>Free Peoples only: a character takes at most 1 + Defender minions, counting any already assigned by
 *     assignment actions;</li>
 *     <li>both sides: ModifiersQuerying.isValidAssignments for the side doing the assigning (this is where
 *     "can't be assigned to a skirmish against X" and "no more than one minion per skirmish" live).</li>
 * </ul>
 * Answers are handled as card-id strings, the decision's own vocabulary: {freeCharId -> [minionId, ...]}.
 *
 * {@link #UNCHECKED} (no game) accepts everything and is what bots get when they run without a live game.
 */
public class AssignmentLegality {
    public static final AssignmentLegality UNCHECKED = new AssignmentLegality(null);

    private final LotroGame game;

    public AssignmentLegality(LotroGame game) {
        this.game = game;
    }

    public boolean isChecked() {
        return game != null;
    }

    /** The side doing the assigning: the current player assigns as Free Peoples, anyone else as Shadow. */
    public static Side assigningSide(GameState gameState, String playerName) {
        return playerName.equals(gameState.getCurrentPlayerId()) ? Side.FREE_PEOPLE : Side.SHADOW;
    }

    public boolean isLegal(Side side, String[] offeredFreeChars, String[] offeredMinions, Map<String, List<String>> assignment) {
        if (game == null)
            return true;

        Set<String> freeChars = offeredFreeChars == null ? Set.of() : Set.of(offeredFreeChars);
        Set<String> minions = offeredMinions == null ? Set.of() : Set.of(offeredMinions);
        GameState gameState = game.getGameState();

        Map<PhysicalCard, Set<PhysicalCard>> cards = new HashMap<>();
        Set<String> usedMinions = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : assignment.entrySet()) {
            if (!freeChars.contains(entry.getKey()))
                return false;
            PhysicalCard fp = card(gameState, entry.getKey());
            if (fp == null)
                return false;
            Set<PhysicalCard> assigned = new HashSet<>();
            for (String minionId : entry.getValue()) {
                if (!minions.contains(minionId) || !usedMinions.add(minionId))
                    return false;
                PhysicalCard minion = card(gameState, minionId);
                if (minion == null)
                    return false;
                assigned.add(minion);
            }
            if (side == Side.FREE_PEOPLE
                    && assigned.size() + alreadyAssignedTo(gameState, fp) > 1 + game.getModifiersQuerying().getKeywordCount(game, fp, Keyword.DEFENDER))
                return false;
            cards.put(fp, assigned);
        }
        return game.getModifiersQuerying().isValidAssignments(game, side, cards);
    }

    /**
     * Keeps as much of the given assignment as the rules allow, in its own order, dropping each pairing that would
     * make it illegal. Minions dropped here stay unassigned (for the Shadow player to assign, or to sit out).
     */
    public Map<String, List<String>> keepLegal(Side side, String[] offeredFreeChars, String[] offeredMinions, Map<String, List<String>> assignment) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : assignment.entrySet()) {
            for (String minionId : entry.getValue()) {
                List<String> assigned = result.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                assigned.add(minionId);
                if (!isLegal(side, offeredFreeChars, offeredMinions, result)) {
                    assigned.remove(assigned.size() - 1);
                    if (assigned.isEmpty())
                        result.remove(entry.getKey());
                }
            }
        }
        return result;
    }

    /** "fp m1 m2,fp2 m3" -> ordered map; blank groups are skipped. Throws NumberFormatException on non-numeric ids. */
    public static Map<String, List<String>> parse(String answer) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (answer == null || answer.isBlank())
            return result;
        for (String group : answer.split(",")) {
            String[] ids = group.trim().split("\\s+");
            if (ids.length == 0 || ids[0].isEmpty())
                continue;
            for (String id : ids)
                Integer.parseInt(id);
            result.put(ids[0], new ArrayList<>(Arrays.asList(ids).subList(1, ids.length)));
        }
        return result;
    }

    /** Inverse of {@link #parse}; characters with no minions are left out. */
    public static String format(Map<String, List<String>> assignment) {
        return assignment.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(","));
    }

    private static PhysicalCard card(GameState gameState, String id) {
        try {
            return gameState.findCardById(Integer.parseInt(id));
        } catch (NumberFormatException exp) {
            return null;
        }
    }

    private static int alreadyAssignedTo(GameState gameState, PhysicalCard fp) {
        for (Assignment assignment : gameState.getAssignments()) {
            if (assignment.getFellowshipCharacter() == fp)
                return assignment.getShadowCharacters().size();
        }
        return 0;
    }
}
