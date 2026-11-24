package com.gempukku.lotro.service;

import org.junit.Test;

import static com.gempukku.lotro.service.LoginThrottle.*;
import static org.junit.Assert.*;

/**
 * Failed password checks counted over a sliding window of {@link LoginThrottle#WINDOW_MILLIS}: per name (ignoring
 * case) from one address, per name from everywhere, and per address.
 */
public class LoginThrottleTest {
    private static final long T0 = 1_000_000_000L;
    private static final long MINUTE = 60_000L;
    private static final String HOME = "10.0.0.1";

    private final LoginThrottle throttle = new LoginThrottle();

    private void failMany(String name, String address, int times, long at) {
        for (int i = 0; i < times; i++)
            throttle.failed(name, address, at);
    }

    /** A different address for each attempt, so that only the name's count from everywhere matters. */
    private void failFromManyAddresses(String name, int times, long at) {
        for (int i = 0; i < times; i++)
            throttle.failed(name, "10.1." + (i / 200) + "." + (i % 200), at);
    }

    @Test
    public void nothingIsBlockedAtFirst() {
        assertFalse(throttle.isBlocked("Frodo", HOME, T0));
        assertFalse(throttle.isBlocked(null, null, T0));
    }

    @Test
    public void aNameIsBlockedFromTheAddressThatGuessedAtIt() {
        failMany("Frodo", HOME, MAX_FAILURES_PER_NAME - 1, T0);
        assertFalse("one short of the limit", throttle.isBlocked("Frodo", HOME, T0));
        throttle.failed("Frodo", HOME, T0);
        assertTrue(throttle.isBlocked("Frodo", HOME, T0));
        assertFalse("other names from there are not", throttle.isBlocked("Sam", HOME, T0));
    }

    @Test
    public void oneAddressGuessingDoesNotLockTheOwnerOut() {
        // wrong passwords typed for a rival's name on purpose, from one address
        failMany("Frodo", "10.6.6.6", MAX_FAILURES_PER_NAME, T0);
        assertTrue(throttle.isBlocked("Frodo", "10.6.6.6", T0));
        assertFalse("Frodo, at home, still logs in", throttle.isBlocked("Frodo", HOME, T0));
    }

    @Test
    public void aNameIsBlockedEverywhereOnceEnoughAddressesHaveGuessedAtIt() {
        failFromManyAddresses("Frodo", MAX_FAILURES_PER_NAME_EVERYWHERE - 1, T0);
        assertFalse("one short of the limit", throttle.isBlocked("Frodo", HOME, T0));
        throttle.failed("Frodo", "10.8.8.8", T0);
        assertTrue(throttle.isBlocked("Frodo", HOME, T0));
        assertFalse("other names are not", throttle.isBlocked("Sam", HOME, T0));
    }

    @Test
    public void namesAreCountedIgnoringCase() {
        failMany("frodo", HOME, MAX_FAILURES_PER_NAME / 2, T0);
        failMany("FRODO", HOME, MAX_FAILURES_PER_NAME - MAX_FAILURES_PER_NAME / 2, T0);
        assertTrue(throttle.isBlocked("Frodo", HOME, T0));
    }

    @Test
    public void anAddressIsBlockedAtItsLimitWhateverTheName() {
        for (int i = 0; i < MAX_FAILURES_PER_ADDRESS - 1; i++)
            throttle.failed("name" + i, HOME, T0);
        assertFalse(throttle.isBlocked("Someone", HOME, T0));
        throttle.failed("last", HOME, T0);
        assertTrue(throttle.isBlocked("Someone", HOME, T0));
        assertFalse("other addresses are not", throttle.isBlocked("Someone", "10.0.0.2", T0));
    }

    @Test
    public void theBlockLiftsWhenTheWindowHasPassed() {
        failMany("Frodo", HOME, MAX_FAILURES_PER_NAME, T0);
        assertTrue(throttle.isBlocked("Frodo", HOME, T0 + WINDOW_MILLIS - 1));
        assertFalse(throttle.isBlocked("Frodo", HOME, T0 + WINDOW_MILLIS));

        failFromManyAddresses("Sam", MAX_FAILURES_PER_NAME_EVERYWHERE, T0);
        assertTrue(throttle.isBlocked("Sam", HOME, T0 + WINDOW_MILLIS - 1));
        assertFalse(throttle.isBlocked("Sam", HOME, T0 + WINDOW_MILLIS));
    }

    @Test
    public void theWindowSlides() {
        failMany("Frodo", HOME, MAX_FAILURES_PER_NAME - 1, T0);
        throttle.failed("Frodo", HOME, T0 + 5 * MINUTE);
        assertTrue(throttle.isBlocked("Frodo", HOME, T0 + 5 * MINUTE));
        // the first nine leave the window; the tenth, five minutes later, is still in it
        assertFalse(throttle.isBlocked("Frodo", HOME, T0 + WINDOW_MILLIS));
        failMany("Frodo", HOME, MAX_FAILURES_PER_NAME - 1, T0 + WINDOW_MILLIS);
        assertTrue("one old failure and nine new ones", throttle.isBlocked("Frodo", HOME, T0 + WINDOW_MILLIS));
        assertFalse(throttle.isBlocked("Frodo", HOME, T0 + 5 * MINUTE + WINDOW_MILLIS));
    }

    @Test
    public void aSuccessClearsTheNameFromThatAddressAndNothingElse() {
        failMany("Frodo", HOME, MAX_FAILURES_PER_NAME - 1, T0);
        failMany("Frodo", "10.6.6.6", MAX_FAILURES_PER_NAME, T0);
        // enough others from home to leave the address two short of its own limit at the end
        for (int i = 0; i < MAX_FAILURES_PER_ADDRESS - 2 * (MAX_FAILURES_PER_NAME - 1) - 2; i++)
            throttle.failed("other" + i, HOME, T0);

        throttle.succeeded("FRODO", HOME, T0);

        failMany("Frodo", HOME, MAX_FAILURES_PER_NAME - 1, T0);
        assertFalse("the name starts again from nothing there", throttle.isBlocked("Frodo", HOME, T0));
        assertTrue("the owner logging in gives whoever was guessing no more guesses", throttle.isBlocked("Frodo", "10.6.6.6", T0));
        throttle.failed("Sam", HOME, T0);
        throttle.failed("Sam", HOME, T0);
        assertTrue("the address still counts every failure from it", throttle.isBlocked("Sam", HOME, T0));
    }

    @Test
    public void aMissingNameOrAddressCountsOnlyTheOther() {
        failMany(null, HOME, MAX_FAILURES_PER_NAME, T0);
        assertFalse(throttle.isBlocked("Frodo", "10.0.0.2", T0));
        failMany("Frodo", null, MAX_FAILURES_PER_NAME, T0);
        assertTrue(throttle.isBlocked("Frodo", null, T0));
        throttle.succeeded(null, HOME, T0);
        throttle.succeeded("Frodo", null, T0);
        assertFalse(throttle.isBlocked("Frodo", null, T0));
    }

    @Test
    public void oldEntriesAreDropped() {
        for (int i = 0; i < 500; i++)
            throttle.failed("name" + i, "10.0." + (i / 250) + "." + (i % 250), T0);
        assertEquals(1500, throttle.trackedEntries());
        // the next call after the window has passed sweeps everything out
        assertFalse(throttle.isBlocked("anyone", "anywhere", T0 + WINDOW_MILLIS));
        assertEquals(0, throttle.trackedEntries());
    }
}
