package com.gempukku.lotro.service;

import org.junit.Test;

import static com.gempukku.lotro.service.LoginThrottle.*;
import static org.junit.Assert.*;

/**
 * Failed password checks counted per name (ignoring case) and per address over a sliding window of
 * {@link LoginThrottle#WINDOW_MILLIS}.
 */
public class LoginThrottleTest {
    private static final long T0 = 1_000_000_000L;
    private static final long MINUTE = 60_000L;

    private final LoginThrottle throttle = new LoginThrottle();

    private void failMany(String name, String address, int times, long at) {
        for (int i = 0; i < times; i++)
            throttle.failed(name, address, at);
    }

    /** A different address for each attempt, so that only the name's count matters. */
    private void failFromManyAddresses(String name, int times, long at) {
        for (int i = 0; i < times; i++)
            throttle.failed(name, "10.1.0." + i, at);
    }

    @Test
    public void nothingIsBlockedAtFirst() {
        assertFalse(throttle.isBlocked("Frodo", "10.0.0.1", T0));
        assertFalse(throttle.isBlocked(null, null, T0));
    }

    @Test
    public void aNameIsBlockedAtItsLimitWhateverTheAddress() {
        failFromManyAddresses("Frodo", MAX_FAILURES_PER_NAME - 1, T0);
        assertFalse("one short of the limit", throttle.isBlocked("Frodo", "10.9.9.9", T0));
        throttle.failed("Frodo", "10.1.1.1", T0);
        assertTrue(throttle.isBlocked("Frodo", "10.9.9.9", T0));
        assertFalse("other names are not", throttle.isBlocked("Sam", "10.9.9.9", T0));
    }

    @Test
    public void namesAreCountedIgnoringCase() {
        failFromManyAddresses("frodo", MAX_FAILURES_PER_NAME / 2, T0);
        failFromManyAddresses("FRODO", MAX_FAILURES_PER_NAME - MAX_FAILURES_PER_NAME / 2, T0);
        assertTrue(throttle.isBlocked("Frodo", "10.9.9.9", T0));
    }

    @Test
    public void anAddressIsBlockedAtItsLimitWhateverTheName() {
        for (int i = 0; i < MAX_FAILURES_PER_ADDRESS - 1; i++)
            throttle.failed("name" + i, "10.0.0.1", T0);
        assertFalse(throttle.isBlocked("Someone", "10.0.0.1", T0));
        throttle.failed("last", "10.0.0.1", T0);
        assertTrue(throttle.isBlocked("Someone", "10.0.0.1", T0));
        assertFalse("other addresses are not", throttle.isBlocked("Someone", "10.0.0.2", T0));
    }

    @Test
    public void theBlockLiftsWhenTheWindowHasPassed() {
        failFromManyAddresses("Frodo", MAX_FAILURES_PER_NAME, T0);
        assertTrue(throttle.isBlocked("Frodo", "x", T0 + WINDOW_MILLIS - 1));
        assertFalse(throttle.isBlocked("Frodo", "x", T0 + WINDOW_MILLIS));
    }

    @Test
    public void theWindowSlides() {
        failFromManyAddresses("Frodo", MAX_FAILURES_PER_NAME - 1, T0);
        throttle.failed("Frodo", "x", T0 + 5 * MINUTE);
        assertTrue(throttle.isBlocked("Frodo", "x", T0 + 5 * MINUTE));
        // the first nine leave the window; the tenth, five minutes later, is still in it
        assertFalse(throttle.isBlocked("Frodo", "x", T0 + WINDOW_MILLIS));
        failFromManyAddresses("Frodo", MAX_FAILURES_PER_NAME - 1, T0 + WINDOW_MILLIS);
        assertTrue("one old failure and nine new ones", throttle.isBlocked("Frodo", "x", T0 + WINDOW_MILLIS));
        assertFalse(throttle.isBlocked("Frodo", "x", T0 + 5 * MINUTE + WINDOW_MILLIS));
    }

    @Test
    public void aSuccessClearsTheNameButNotTheAddress() {
        failMany("Frodo", "10.0.0.1", MAX_FAILURES_PER_NAME - 1, T0);
        for (int i = 0; i < MAX_FAILURES_PER_ADDRESS - MAX_FAILURES_PER_NAME; i++)
            throttle.failed("other" + i, "10.0.0.1", T0);
        throttle.succeeded("FRODO", T0);
        failFromManyAddresses("Frodo", MAX_FAILURES_PER_NAME - 1, T0);
        assertFalse("the name starts again from nothing", throttle.isBlocked("Frodo", "10.9.9.9", T0));
        throttle.failed("Sam", "10.0.0.1", T0);
        assertTrue("the address still counts every failure from it", throttle.isBlocked("Sam", "10.0.0.1", T0));
    }

    @Test
    public void aMissingNameOrAddressCountsOnlyTheOther() {
        failMany(null, "10.0.0.1", MAX_FAILURES_PER_NAME, T0);
        assertFalse(throttle.isBlocked("Frodo", "10.0.0.2", T0));
        failFromManyAddresses("Frodo", MAX_FAILURES_PER_NAME - 1, T0);
        throttle.failed("Frodo", null, T0);
        assertTrue(throttle.isBlocked("Frodo", null, T0));
        throttle.succeeded(null, T0);
    }

    @Test
    public void oldEntriesAreDropped() {
        for (int i = 0; i < 500; i++)
            throttle.failed("name" + i, "10.0." + (i / 250) + "." + (i % 250), T0);
        assertEquals(1000, throttle.trackedEntries());
        // the next call after the window has passed sweeps everything out
        assertFalse(throttle.isBlocked("anyone", "anywhere", T0 + WINDOW_MILLIS));
        assertEquals(0, throttle.trackedEntries());
    }
}
