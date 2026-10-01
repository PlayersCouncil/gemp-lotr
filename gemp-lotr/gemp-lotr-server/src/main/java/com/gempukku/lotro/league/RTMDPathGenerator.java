package com.gempukku.lotro.league;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Builds a Race to Mount Doom path: an ordered list of modifier blueprint IDs plus the parallel list of set-90
 * visual position cards.
 * <p>
 * This is the server-side twin of the "Randomize" button in {@code admin/leagueAdmin.html} and deliberately follows
 * the same algorithm, so that a path generated for a scheduled league is indistinguishable from one an admin rolled
 * by hand:
 * <ol>
 *     <li>the intensity ramps linearly from the floor to the ceiling across the path;</li>
 *     <li>each slot's target is jittered by up to +/-1 and then filled from the <em>whole</em> bucket of remaining
 *         modifiers tied at the nearest intensity, picked uniformly - so cards inside a bucket are treated fairly
 *         rather than always yielding the same few;</li>
 *     <li>two random adjacent pairs are swapped afterwards for a dash of non-linearity;</li>
 *     <li>visual cards are mapped proportionally from the modifier's place in the <em>global</em> intensity range
 *         onto the visual range, with The Shire ({@value #FIRST_VISUAL_ID}) forced first and Mount Doom
 *         ({@value #LAST_VISUAL_ID}) forced last.</li>
 * </ol>
 * Nothing here touches game state; it only reads blueprint intensities.
 */
public final class RTMDPathGenerator {
    /** Sets whose cards are race modifiers (the meta-sites that carry the game text). */
    public static final Set<Integer> MODIFIER_SETS = Set.of(91, 92, 93, 94);
    /** Sets whose cards are the visual position cards shown underneath the modifier. */
    public static final Set<Integer> VISUAL_SETS = Set.of(90);
    /** The Shire: always the first stop of a race. */
    public static final String FIRST_VISUAL_ID = "90_1";
    /** Mount Doom: always the last stop of a race. */
    public static final String LAST_VISUAL_ID = "90_18";
    /** How many random adjacent pairs are swapped after the ramp has been filled. */
    public static final int ADJACENT_SWAPS = 2;
    /** Path length used when a definition asks for a generated path without saying how long it should be. */
    public static final int DEFAULT_PATH_LENGTH = 9;

    private RTMDPathGenerator() {
    }

    /**
     * One card the generator can place, with the intensity its blueprint declares.
     */
    public record Card(String blueprintId, int intensity) {
    }

    /**
     * Everything the generator picks from: the modifiers of sets 91-94 and the visual cards of set 90.
     */
    public record Pool(List<Card> modifiers, List<Card> visualCards) {
        public boolean isEmpty() {
            return modifiers.isEmpty() || visualCards.isEmpty();
        }
    }

    /**
     * A generated path.  The two lists are parallel and always the same length.
     */
    public record Path(List<String> racePath, List<String> raceVisualPath) {
        public boolean isEmpty() {
            return racePath.isEmpty();
        }
    }

    /**
     * Reads the modifier and visual card pools out of the card library.  This is the same view of the sets that the
     * admin page's race editor is served, so both sides randomise from the same cards.
     */
    public static Pool loadPool(LotroCardBlueprintLibrary cardLibrary) {
        var modifiers = new ArrayList<Card>();
        var visualCards = new ArrayList<Card>();
        if (cardLibrary == null)
            return new Pool(modifiers, visualCards);

        for (var entry : cardLibrary.getBaseCards().entrySet()) {
            String blueprintId = entry.getKey();
            int setId;
            try {
                setId = Integer.parseInt(blueprintId.split("_")[0]);
            } catch (NumberFormatException exp) {
                continue;
            }
            if (VISUAL_SETS.contains(setId))
                visualCards.add(new Card(blueprintId, entry.getValue().getIntensity()));
            else if (MODIFIER_SETS.contains(setId))
                modifiers.add(new Card(blueprintId, entry.getValue().getIntensity()));
        }
        // the library hands out a hash map, so fix an order before anything random happens
        modifiers.sort(Comparator.comparing(RTMDPathGenerator::sortKey));
        visualCards.sort(Comparator.comparing(RTMDPathGenerator::sortKey));
        return new Pool(modifiers, visualCards);
    }

    /**
     * Generates a fresh path.
     *
     * @param pool        the cards to pick from
     * @param pathLength  how many stops the race has; clamped to 1..{@link LeagueFactory#MAX_RACE_PATH_LENGTH}
     * @param floor       lowest intensity the ramp starts at; only modifiers in [floor, ceiling] are used
     * @param ceiling     highest intensity the ramp ends at
     * @param lockedSlots optional, one entry per slot: a blueprint ID keeps that slot as it is, null/blank re-rolls
     *                    it.  Locked slots never move and their cards are not used again elsewhere.
     * @param random      the source of randomness; pass a seeded instance for a reproducible path
     * @return the path; empty when no modifier at all could be placed (for instance an intensity range that matches
     *         nothing), which the caller is expected to report rather than persist
     */
    public static Path generate(Pool pool, int pathLength, int floor, int ceiling, List<String> lockedSlots,
                                Random random) {
        if (pool == null)
            pool = new Pool(List.of(), List.of());
        if (random == null)
            random = new Random();

        int length = Math.max(1, Math.min(pathLength, LeagueFactory.MAX_RACE_PATH_LENGTH));
        var slots = new Card[length];
        var locked = new boolean[length];
        var used = new HashSet<String>();

        applyLocks(pool, lockedSlots, slots, locked, used);

        var available = new ArrayList<Card>();
        for (Card card : pool.modifiers()) {
            if (card.intensity() >= floor && card.intensity() <= ceiling)
                available.add(card);
        }

        fillRamp(slots, locked, used, available, length, floor, ceiling, random);
        swapAdjacentPairs(slots, locked, random);

        Card[] visuals = assignVisualCards(slots, pool, length);

        var racePath = new ArrayList<String>();
        var visualPath = new ArrayList<String>();
        for (int i = 0; i < length; i++) {
            // a slot only counts when both halves are there: the two lists must stay parallel, and LeagueFactory
            // rejects a definition whose visual path has a different length
            if (slots[i] == null || visuals[i] == null)
                continue;
            racePath.add(slots[i].blueprintId());
            visualPath.add(visuals[i].blueprintId());
        }
        return new Path(racePath, visualPath);
    }

    /**
     * Pins the slots the caller locked.  An ID that is not in the pool is kept anyway (it may be a card the admin
     * added by hand from outside the intensity range) and treated as intensity 0 for the visual mapping.
     */
    private static void applyLocks(Pool pool, List<String> lockedSlots, Card[] slots, boolean[] locked,
                                   Set<String> used) {
        if (lockedSlots == null)
            return;
        for (int i = 0; i < slots.length && i < lockedSlots.size(); i++) {
            String blueprintId = lockedSlots.get(i);
            if (StringUtils.isBlank(blueprintId))
                continue;
            Card card = find(pool.modifiers(), blueprintId.trim());
            slots[i] = card == null ? new Card(blueprintId.trim(), 0) : card;
            locked[i] = true;
            used.add(slots[i].blueprintId());
        }
    }

    /**
     * Fills every unlocked slot from the nearest intensity bucket around that slot's jittered target.
     */
    private static void fillRamp(Card[] slots, boolean[] locked, Set<String> used, List<Card> available, int length,
                                 int floor, int ceiling, Random random) {
        for (int i = 0; i < length; i++) {
            if (locked[i] && slots[i] != null)
                continue;

            double target = floor + (ceiling - floor) * (i / (double) Math.max(1, length - 1));
            double adjusted = target + (random.nextDouble() - 0.5) * 2;

            double nearest = Double.MAX_VALUE;
            for (Card card : available) {
                if (used.contains(card.blueprintId()))
                    continue;
                nearest = Math.min(nearest, Math.abs(card.intensity() - adjusted));
            }
            if (nearest == Double.MAX_VALUE) {
                slots[i] = null;
                continue;
            }

            // every card tied at the nearest distance, so each one in that bucket has the same chance; variety
            // between buckets comes from the jitter above
            var bucket = new ArrayList<Card>();
            for (Card card : available) {
                if (!used.contains(card.blueprintId()) && Math.abs(card.intensity() - adjusted) == nearest)
                    bucket.add(card);
            }
            Card pick = bucket.get(random.nextInt(bucket.size()));
            slots[i] = pick;
            used.add(pick.blueprintId());
        }
    }

    /**
     * Swaps {@value #ADJACENT_SWAPS} random adjacent pairs of unlocked, filled slots.
     */
    private static void swapAdjacentPairs(Card[] slots, boolean[] locked, Random random) {
        for (int swap = 0; swap < ADJACENT_SWAPS; swap++) {
            var candidates = new ArrayList<Integer>();
            for (int i = 0; i < slots.length - 1; i++) {
                if (!locked[i] && !locked[i + 1] && slots[i] != null && slots[i + 1] != null)
                    candidates.add(i);
            }
            if (candidates.isEmpty())
                continue;
            int index = candidates.get(random.nextInt(candidates.size()));
            Card tmp = slots[index];
            slots[index] = slots[index + 1];
            slots[index + 1] = tmp;
        }
    }

    /**
     * Maps each placed modifier onto a distinct visual card.  The modifier's position in the <em>global</em>
     * modifier intensity range (not just this path's range) decides where on the visual range it lands, so a path
     * of mild modifiers stays near The Shire instead of being stretched all the way to Mount Doom.
     */
    private static Card[] assignVisualCards(Card[] slots, Pool pool, int length) {
        var assignments = new Card[length];
        var placed = new ArrayList<Integer>();
        for (int i = 0; i < length; i++) {
            if (slots[i] != null)
                placed.add(i);
        }
        if (placed.isEmpty() || pool.visualCards().isEmpty())
            return assignments;

        // ascending by intensity; ties keep their order along the path
        placed.sort(Comparator.comparingInt(index -> slots[index].intensity()));

        var available = new ArrayList<>(pool.visualCards());
        available.sort(Comparator.comparingInt(Card::intensity));
        int visMin = available.getFirst().intensity();
        int visMax = available.getLast().intensity();

        int globalMin = Integer.MAX_VALUE;
        int globalMax = Integer.MIN_VALUE;
        for (Card card : pool.modifiers()) {
            globalMin = Math.min(globalMin, card.intensity());
            globalMax = Math.max(globalMax, card.intensity());
        }
        if (globalMin > globalMax) {
            globalMin = 0;
            globalMax = 0;
        }

        var usedVisual = new HashSet<String>();
        for (int n = 0; n < placed.size(); n++) {
            int slot = placed.get(n);
            double targetVis;
            if (globalMax == globalMin) {
                // every modifier sits at the same intensity: spread them evenly across the visual range instead
                targetVis = visMin + (visMax - visMin) * (n / (double) Math.max(1, placed.size() - 1));
            } else {
                double t = (slots[slot].intensity() - globalMin) / (double) (globalMax - globalMin);
                targetVis = visMin + (visMax - visMin) * t;
            }

            Card best = null;
            double bestDistance = Double.MAX_VALUE;
            for (Card card : available) {
                if (usedVisual.contains(card.blueprintId()))
                    continue;
                double distance = Math.abs(card.intensity() - targetVis);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = card;
                }
            }
            if (best != null) {
                assignments[slot] = best;
                usedVisual.add(best.blueprintId());
            }
        }

        forceFirstAndLast(assignments);
        return assignments;
    }

    /**
     * The Shire opens the race and Mount Doom closes it; if the proportional mapping put either somewhere else,
     * swap it with whatever is holding its place.
     */
    private static void forceFirstAndLast(Card[] assignments) {
        int firstOccupied = -1;
        int lastOccupied = -1;
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == null)
                continue;
            if (firstOccupied < 0)
                firstOccupied = i;
            lastOccupied = i;
        }
        if (firstOccupied < 0)
            return;

        int shireIndex = indexOf(assignments, FIRST_VISUAL_ID);
        if (shireIndex >= 0 && shireIndex != firstOccupied) {
            Card shire = assignments[shireIndex];
            assignments[shireIndex] = assignments[firstOccupied];
            assignments[firstOccupied] = shire;
        }

        int doomIndex = indexOf(assignments, LAST_VISUAL_ID);
        if (doomIndex >= 0 && doomIndex != lastOccupied) {
            Card doom = assignments[doomIndex];
            assignments[doomIndex] = assignments[lastOccupied];
            assignments[lastOccupied] = doom;
        }
    }

    private static int indexOf(Card[] assignments, String blueprintId) {
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] != null && assignments[i].blueprintId().equals(blueprintId))
                return i;
        }
        return -1;
    }

    private static Card find(List<Card> cards, String blueprintId) {
        for (Card card : cards) {
            if (card.blueprintId().equals(blueprintId))
                return card;
        }
        return null;
    }

    /**
     * Orders blueprint IDs the way the admin page does: by set, then by card number.
     */
    private static String sortKey(Card card) {
        String[] parts = card.blueprintId().split("_");
        try {
            return String.format("%04d_%06d", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (RuntimeException exp) {
            return card.blueprintId();
        }
    }
}
