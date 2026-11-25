package com.gempukku.lotro.service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Limits password guessing: failed password checks are counted over a sliding window, three ways, and while any
 * count is at its limit the attempt is refused without the password being checked.  Used by POST /login and by the
 * PC account's "Link Existing Gemp Account" and "Connect PC Account", which check an old password the same way.
 * <ul>
 * <li>Per account name (ignoring case) from one address: the limit someone guessing meets first.</li>
 * <li>Per account name from everywhere: a higher limit, against guessing spread over many addresses.  It is higher
 *     because it is also the one that can be turned on a player: wrong passwords typed for a name on purpose lock
 *     its owner out, and that should take more than one person at one address a moment's effort.</li>
 * <li>Per address, whatever the names.</li>
 * </ul>
 * The caller asks {@link #isBlocked} first; if not blocked it checks the password and reports {@link #failed} or
 * {@link #succeeded}.  A refused attempt is not reported, so it does not lengthen the wait.  A success clears the
 * name's count from that address and nothing else: a right password from the owner's address does not hand anyone
 * else more guesses, nor let that address go on guessing other names.
 * <p>
 * In memory only, so a restart forgets every count.  Time is passed in, so tests need not sleep.
 */
public class LoginThrottle {
    // The limits suggested when this was planned (2026-11-23).  Ten wrong passwords for one name in ten minutes is well past
    // anyone mistyping their own; fifty from one address leaves room for a club or a household behind one address.
    public static final int MAX_FAILURES_PER_NAME = 10;
    public static final int MAX_FAILURES_PER_NAME_EVERYWHERE = 30;
    public static final int MAX_FAILURES_PER_ADDRESS = 50;
    public static final long WINDOW_MILLIS = 10 * 60 * 1000L;
    // How often entries whose failures have all left the window are dropped, so the maps cannot grow without bound.
    private static final long SWEEP_INTERVAL_MILLIS = 60 * 1000L;

    private final Map<String, ArrayDeque<Long>> _byNameAndAddress = new HashMap<>();
    private final Map<String, ArrayDeque<Long>> _byName = new HashMap<>();
    private final Map<String, ArrayDeque<Long>> _byAddress = new HashMap<>();
    private long _lastSweep = Long.MIN_VALUE;

    /** Whether an attempt for {@code name} from {@code address} is to be refused now.  Either may be null. */
    public synchronized boolean isBlocked(String name, String address, long now) {
        sweepIfDue(now);
        return count(_byNameAndAddress, pairKey(name, address), now) >= MAX_FAILURES_PER_NAME
                || count(_byName, nameKey(name), now) >= MAX_FAILURES_PER_NAME_EVERYWHERE
                || count(_byAddress, address, now) >= MAX_FAILURES_PER_ADDRESS;
    }

    /** A password check for {@code name} from {@code address} failed. */
    public synchronized void failed(String name, String address, long now) {
        sweepIfDue(now);
        record(_byNameAndAddress, pairKey(name, address), now, MAX_FAILURES_PER_NAME);
        record(_byName, nameKey(name), now, MAX_FAILURES_PER_NAME_EVERYWHERE);
        record(_byAddress, address, now, MAX_FAILURES_PER_ADDRESS);
    }

    /** A password check for {@code name} from {@code address} succeeded: its failures from there are forgotten. */
    public synchronized void succeeded(String name, String address, long now) {
        String key = pairKey(name, address);
        if (key != null)
            _byNameAndAddress.remove(key);
    }

    /** How many names and addresses have failures being remembered (for tests). */
    synchronized int trackedEntries() {
        return _byNameAndAddress.size() + _byName.size() + _byAddress.size();
    }

    private static String nameKey(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT);
    }

    /** A name as typed from an address; an attempt with no name is counted by its address alone. */
    private static String pairKey(String name, String address) {
        return name == null ? null : nameKey(name) + "\n" + address;
    }

    private static int count(Map<String, ArrayDeque<Long>> map, String key, long now) {
        if (key == null)
            return 0;
        ArrayDeque<Long> failures = map.get(key);
        if (failures == null)
            return 0;
        dropExpired(failures, now);
        if (failures.isEmpty()) {
            map.remove(key);
            return 0;
        }
        return failures.size();
    }

    private static void record(Map<String, ArrayDeque<Long>> map, String key, long now, int limit) {
        if (key == null)
            return;
        ArrayDeque<Long> failures = map.computeIfAbsent(key, k -> new ArrayDeque<>());
        dropExpired(failures, now);
        failures.addLast(now);
        // Only the latest `limit` failures can matter: older ones leave the window first
        while (failures.size() > limit)
            failures.removeFirst();
    }

    private static void dropExpired(ArrayDeque<Long> failures, long now) {
        while (!failures.isEmpty() && failures.peekFirst() <= now - WINDOW_MILLIS)
            failures.removeFirst();
    }

    private void sweepIfDue(long now) {
        if (_lastSweep != Long.MIN_VALUE && now - _lastSweep < SWEEP_INTERVAL_MILLIS)
            return;
        _lastSweep = now;
        sweep(_byNameAndAddress, now);
        sweep(_byName, now);
        sweep(_byAddress, now);
    }

    private static void sweep(Map<String, ArrayDeque<Long>> map, long now) {
        for (Iterator<ArrayDeque<Long>> it = map.values().iterator(); it.hasNext(); ) {
            ArrayDeque<Long> failures = it.next();
            dropExpired(failures, now);
            if (failures.isEmpty())
                it.remove();
        }
    }
}
