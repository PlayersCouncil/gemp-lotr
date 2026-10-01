package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Exercises {@link RTMDPathGenerator}: the ramp, the bucket-fair tie handling, locked slots and the visual card
 * mapping.  Most tests run against a synthetic pool so that the expectations do not move when cards are added;
 * the last one checks the real card library still produces a usable path.
 */
public class RTMDPathGeneratorTest extends AbstractAtTest {

    // ------------------------------------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------------------------------------

    /**
     * Four modifiers at every intensity from -5 to 5, ids "9x_n" so they look like real blueprint ids.
     */
    private static List<RTMDPathGenerator.Card> modifiers() {
        var cards = new ArrayList<RTMDPathGenerator.Card>();
        int number = 1;
        for (int intensity = -5; intensity <= 5; intensity++) {
            for (int copy = 0; copy < 4; copy++)
                cards.add(new RTMDPathGenerator.Card("91_" + (number++), intensity));
        }
        return cards;
    }

    /**
     * The 18 real visual positions: 90_1 (The Shire) through 90_18 (Mount Doom), intensity 1..18.
     */
    private static List<RTMDPathGenerator.Card> visualCards() {
        var cards = new ArrayList<RTMDPathGenerator.Card>();
        for (int i = 1; i <= 18; i++)
            cards.add(new RTMDPathGenerator.Card("90_" + i, i));
        return cards;
    }

    private static RTMDPathGenerator.Pool pool() {
        return new RTMDPathGenerator.Pool(modifiers(), visualCards());
    }

    private static int intensityOf(String blueprintId) {
        for (RTMDPathGenerator.Card card : modifiers()) {
            if (card.blueprintId().equals(blueprintId))
                return card.intensity();
        }
        throw new AssertionError("Not a pool card: " + blueprintId);
    }

    private static double average(List<String> path, int from, int to) {
        double total = 0;
        for (int i = from; i < to; i++)
            total += intensityOf(path.get(i));
        return total / (to - from);
    }

    // ------------------------------------------------------------------------------------------------
    // Shape of the result
    // ------------------------------------------------------------------------------------------------

    @Test
    public void aPathHasOneDistinctModifierAndOneDistinctVisualCardPerSlot() {
        var path = RTMDPathGenerator.generate(pool(), 9, -5, 5, null, new Random(7));

        assertEquals(9, path.racePath().size());
        assertEquals(9, path.raceVisualPath().size());
        assertEquals(9, new HashSet<>(path.racePath()).size());
        assertEquals(9, new HashSet<>(path.raceVisualPath()).size());
    }

    @Test
    public void theSameSeedProducesTheSamePath() {
        var first = RTMDPathGenerator.generate(pool(), 12, -5, 5, null, new Random(1234));
        var second = RTMDPathGenerator.generate(pool(), 12, -5, 5, null, new Random(1234));

        assertEquals(first.racePath(), second.racePath());
        assertEquals(first.raceVisualPath(), second.raceVisualPath());
    }

    @Test
    public void differentSeedsProduceDifferentPaths() {
        var first = RTMDPathGenerator.generate(pool(), 12, -5, 5, null, new Random(1));
        var second = RTMDPathGenerator.generate(pool(), 12, -5, 5, null, new Random(2));

        assertNotEquals(first.racePath(), second.racePath());
    }

    @Test
    public void pathLengthIsClampedToWhatALeagueAccepts() {
        assertEquals(LeagueFactory.MAX_RACE_PATH_LENGTH,
                RTMDPathGenerator.generate(pool(), 50, -5, 5, null, new Random(3)).racePath().size());
        assertEquals(1, RTMDPathGenerator.generate(pool(), 0, -5, 5, null, new Random(3)).racePath().size());
        assertEquals(1, RTMDPathGenerator.generate(pool(), -4, -5, 5, null, new Random(3)).racePath().size());
    }

    @Test
    public void aPathIsOnlyAsLongAsThePoolAllows() {
        var tiny = new RTMDPathGenerator.Pool(
                List.of(new RTMDPathGenerator.Card("91_1", 0), new RTMDPathGenerator.Card("91_2", 0)), visualCards());

        var path = RTMDPathGenerator.generate(tiny, 9, 0, 0, null, new Random(5));

        assertEquals(2, path.racePath().size());
        assertEquals(2, path.raceVisualPath().size());
    }

    @Test
    public void anIntensityRangeThatMatchesNothingProducesNoPath() {
        var path = RTMDPathGenerator.generate(pool(), 9, 40, 50, null, new Random(5));

        assertTrue(path.isEmpty());
        assertTrue(path.raceVisualPath().isEmpty());
    }

    // ------------------------------------------------------------------------------------------------
    // The ramp
    // ------------------------------------------------------------------------------------------------

    @Test
    public void onlyModifiersInsideTheIntensityRangeAreUsed() {
        for (int seed = 0; seed < 25; seed++) {
            var path = RTMDPathGenerator.generate(pool(), 9, -1, 2, null, new Random(seed));
            for (String blueprintId : path.racePath()) {
                int intensity = intensityOf(blueprintId);
                assertTrue("seed " + seed + ": " + blueprintId + " has intensity " + intensity,
                        intensity >= -1 && intensity <= 2);
            }
        }
    }

    @Test
    public void thePathGetsHarderFromFloorToCeiling() {
        for (int seed = 0; seed < 25; seed++) {
            var path = RTMDPathGenerator.generate(pool(), 9, -5, 5, null, new Random(seed)).racePath();
            // two adjacent pairs are swapped on purpose, so only the overall trend is guaranteed
            assertTrue("seed " + seed, average(path, 0, 3) < average(path, 6, 9));
        }
    }

    @Test
    public void theEndsOfThePathSitAtTheEndsOfTheRange() {
        for (int seed = 0; seed < 25; seed++) {
            var path = RTMDPathGenerator.generate(pool(), 9, -5, 5, null, new Random(seed)).racePath();
            // the first slot targets the floor and the last the ceiling, each jittered by at most 1
            assertTrue("seed " + seed, intensityOf(path.getFirst()) <= -3);
            assertTrue("seed " + seed, intensityOf(path.getLast()) >= 3);
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Bucket fairness
    // ------------------------------------------------------------------------------------------------

    /**
     * Every card tied at the nearest intensity must be reachable: the old "first three of the sorted list" always
     * handed the same few cards out.
     */
    @Test
    public void everyCardInTheNearestBucketCanBePicked() {
        var bucket = new ArrayList<RTMDPathGenerator.Card>();
        for (int i = 1; i <= 6; i++)
            bucket.add(new RTMDPathGenerator.Card("91_" + i, 0));
        var onePool = new RTMDPathGenerator.Pool(bucket, visualCards());

        var seen = new HashSet<String>();
        for (int seed = 0; seed < 300; seed++)
            seen.add(RTMDPathGenerator.generate(onePool, 1, 0, 0, null, new Random(seed)).racePath().getFirst());

        assertEquals("every tied card must be reachable", 6, seen.size());
    }

    @Test
    public void tiedCardsAreSpreadRatherThanAlwaysTheSameOnes() {
        var counts = new java.util.HashMap<String, Integer>();
        for (int seed = 0; seed < 400; seed++) {
            String picked = RTMDPathGenerator.generate(pool(), 1, 0, 0, null, new Random(seed)).racePath().getFirst();
            counts.merge(picked, 1, Integer::sum);
        }
        // four cards sit at intensity 0; none of them should be starved
        assertEquals(4, counts.size());
        for (var entry : counts.entrySet())
            assertTrue(entry.getKey() + " picked only " + entry.getValue() + " times", entry.getValue() > 400 / 20);
    }

    // ------------------------------------------------------------------------------------------------
    // Locked slots
    // ------------------------------------------------------------------------------------------------

    @Test
    public void lockedSlotsKeepTheirCardAndAreNotReusedElsewhere() {
        var locked = Arrays.asList("", "91_1", "", "", "91_44", "", "", "", "");

        for (int seed = 0; seed < 25; seed++) {
            var path = RTMDPathGenerator.generate(pool(), 9, -5, 5, locked, new Random(seed)).racePath();
            assertEquals("seed " + seed, "91_1", path.get(1));
            assertEquals("seed " + seed, "91_44", path.get(4));
            assertEquals("seed " + seed, 9, new HashSet<>(path).size());
        }
    }

    @Test
    public void aLockedCardOutsideTheIntensityRangeIsStillKept() {
        var locked = Arrays.asList("", "", "91_1", "", "");

        var path = RTMDPathGenerator.generate(pool(), 5, 3, 5, locked, new Random(9)).racePath();

        assertEquals("91_1", path.get(2));
        assertEquals(5, path.size());
    }

    // ------------------------------------------------------------------------------------------------
    // Visual cards
    // ------------------------------------------------------------------------------------------------

    /**
     * The Shire and Mount Doom are pinned to the ends whenever the proportional mapping hands them out, even when
     * the intensity order would have put them somewhere else: here the harshest modifier is locked into the first
     * slot and the mildest into the last, so the mapping picks Mount Doom for slot 1 and The Shire for slot 3.
     */
    @Test
    public void theShireAndMountDoomArePinnedToTheEnds() {
        var locked = Arrays.asList("91_44", "", "91_1");   // intensity 5 first, intensity -5 last

        var path = RTMDPathGenerator.generate(pool(), 3, -5, 5, locked, new Random(11));

        assertEquals(List.of("91_44", path.racePath().get(1), "91_1"), path.racePath());
        assertEquals(RTMDPathGenerator.FIRST_VISUAL_ID, path.raceVisualPath().getFirst());
        assertEquals(RTMDPathGenerator.LAST_VISUAL_ID, path.raceVisualPath().getLast());
    }

    @Test
    public void whereverTheShireAndMountDoomAppearTheyAreAtTheEnds() {
        int sawShire = 0;
        int sawDoom = 0;
        for (int seed = 0; seed < 40; seed++) {
            var visuals = RTMDPathGenerator.generate(pool(), 9, -5, 5, null, new Random(seed)).raceVisualPath();
            int shire = visuals.indexOf(RTMDPathGenerator.FIRST_VISUAL_ID);
            int doom = visuals.indexOf(RTMDPathGenerator.LAST_VISUAL_ID);
            if (shire >= 0) {
                sawShire++;
                assertEquals("seed " + seed, 0, shire);
            }
            if (doom >= 0) {
                sawDoom++;
                assertEquals("seed " + seed, visuals.size() - 1, doom);
            }
        }
        assertTrue("The Shire should turn up on a full-range path", sawShire > 0);
        assertTrue("Mount Doom should turn up on a full-range path", sawDoom > 0);
    }

    @Test
    public void mildModifiersStayNearTheStartOfTheRoad() {
        // the visual mapping uses the whole modifier range, so a mild path must not be stretched to Mordor
        var visuals = RTMDPathGenerator.generate(pool(), 5, -5, -3, null, new Random(11)).raceVisualPath();

        for (String blueprintId : visuals)
            assertTrue(blueprintId + " is too far along for a mild modifier", position(blueprintId) <= 9);
    }

    @Test
    public void harshModifiersSitLateOnTheRoad() {
        var visuals = RTMDPathGenerator.generate(pool(), 5, 3, 5, null, new Random(13)).raceVisualPath();

        for (String blueprintId : visuals)
            assertTrue(blueprintId + " is too early for a harsh modifier", position(blueprintId) >= 10);
    }

    private static int position(String visualBlueprintId) {
        return Integer.parseInt(visualBlueprintId.split("_")[1]);
    }

    // ------------------------------------------------------------------------------------------------
    // The real card library
    // ------------------------------------------------------------------------------------------------

    @Test
    public void theLoadedCardLibraryProducesAUsablePath() throws Exception {
        var loaded = RTMDPathGenerator.loadPool(_cardLibrary);

        assertFalse("sets 91-94 should be loaded", loaded.modifiers().isEmpty());
        assertFalse("set 90 should be loaded", loaded.visualCards().isEmpty());

        var path = RTMDPathGenerator.generate(loaded, 9, -10, 10, null, new Random(17));

        assertEquals(9, path.racePath().size());
        assertEquals(9, path.raceVisualPath().size());
        assertEquals(9, new HashSet<>(path.racePath()).size());
        assertEquals(9, new HashSet<>(path.raceVisualPath()).size());
        for (String blueprintId : path.racePath())
            assertNotNull(blueprintId, _cardLibrary.getLotroCardBlueprint(blueprintId));
        for (String blueprintId : path.raceVisualPath()) {
            assertNotNull(blueprintId, _cardLibrary.getLotroCardBlueprint(blueprintId));
            assertTrue(blueprintId, blueprintId.startsWith("90_"));
        }
        assertEquals(RTMDPathGenerator.FIRST_VISUAL_ID, path.raceVisualPath().getFirst());
        assertEquals(RTMDPathGenerator.LAST_VISUAL_ID, path.raceVisualPath().getLast());
    }

    @Test
    public void theLoadedPoolIsOrderedSoThatOnlyTheSeedDecides() {
        var first = RTMDPathGenerator.loadPool(_cardLibrary);
        var second = RTMDPathGenerator.loadPool(_cardLibrary);

        assertEquals(first.modifiers(), second.modifiers());
        assertEquals(first.visualCards(), second.visualCards());
        assertEquals(RTMDPathGenerator.generate(first, 9, -10, 10, null, new Random(21)).racePath(),
                RTMDPathGenerator.generate(second, 9, -10, 10, null, new Random(21)).racePath());
    }
}
