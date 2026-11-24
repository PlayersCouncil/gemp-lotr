package com.gempukku.lotro.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The sessions of this server, kept in memory and lost on a restart: one per browser, named by the value of its
 * `loggedUser` cookie.
 *
 * A session says two separate things (PC-ACCOUNT.md): which PC account is signed in, if any, and which Gemp account
 * ("alias") is being played, if any. A password login has an alias and no PC account. Someone who has signed in with
 * a PC account and not yet chosen an alias has a PC account and no alias. A session with neither exists only while a
 * PC sign-in it started is on its way to Argonauth and back.
 */
public class LoggedUserHolder {
    /**
     * How long a session lasts without being used. Until November 2026 this said ten minutes, but the sweep looked
     * at one session a minute, so in practice a session lasted until the server restarted. That is kept on purpose,
     * as six months (Ketura, 2026-11-24): nobody is logged out of Gemp for stepping away.
     */
    static final long SESSION_IDLE_MILLIS = 1000L * 60 * 60 * 24 * 183;
    /** How long Argonauth's answer to a sign-in is waited for. */
    static final long PENDING_SIGN_IN_MILLIS = 1000L * 60 * 5;
    static final int MAX_SESSIONS = 50_000;
    /** How long a session that is nobody's is kept unused: twice the time a sign-in may take. */
    static final long NOBODYS_SESSION_IDLE_MILLIS = PENDING_SIGN_IN_MILLIS * 2;
    private static final long SWEEP_INTERVAL_MILLIS = 1000L * 60;

    private static final char[] ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int ID_LENGTH = 32;

    /**
     * The PC account signed in on a session.
     *
     * @param sub            the account's permanent id at Argonauth: the only thing that identifies the person
     * @param name           its display name, as of the sign-in
     * @param accessToken    Argonauth's token for the person, which the Link API asks for
     * @param tokenExpiresAt when that token stops being accepted (epoch millis)
     * @param idToken        the ID token of the sign-in
     */
    public record PcAccount(String sub, String name, String accessToken, long tokenExpiresAt, String idToken) {
        /** Whether Argonauth will still take the token: creating and linking need it. */
        public boolean isFresh(long now) {
            return now < tokenExpiresAt;
        }
    }

    /**
     * A PC sign-in on its way to Argonauth and back.
     *
     * @param state        returned by Argonauth with its answer, which is only taken with the same one
     * @param nonce        must come back inside the ID token
     * @param codeVerifier the PKCE secret, sent when the code is exchanged
     * @param then         what the sign-in was started for: "" (enter), "link", "create" or "connect"
     * @param returnPath   a place in the app to go to afterwards, or ""
     * @param startedAt    epoch millis
     */
    public record PendingSignIn(String state, String nonce, String codeVerifier, String then, String returnPath,
                                long startedAt) {
    }

    public static final class Session {
        private final String _id;
        private volatile String _player;
        private volatile PcAccount _pcAccount;
        private volatile PendingSignIn _pending;
        private volatile long _lastAccess;

        private Session(String id, long now) {
            _id = id;
            _lastAccess = now;
        }

        /** The cookie's value. */
        public String getId() {
            return _id;
        }

        /** The name of the alias this session plays as, or null. */
        public String getPlayer() {
            return _player;
        }

        public void setPlayer(String player) {
            _player = player;
        }

        /** The PC account signed in, or null. */
        public PcAccount getPcAccount() {
            return _pcAccount;
        }

        public void setPcAccount(PcAccount pcAccount) {
            _pcAccount = pcAccount;
        }

        public void setPending(PendingSignIn pending) {
            _pending = pending;
        }

        /**
         * The sign-in this session is waiting on, if `state` is its and it has not been waited on for too long. It
         * is forgotten by being taken: an answer is taken once. A `state` that is not its takes nothing and leaves
         * it waiting, so that a made-up callback cannot call off somebody's sign-in (the state cannot be guessed,
         * so there is nothing to be had by trying).
         */
        public synchronized PendingSignIn takePending(String state, long now) {
            PendingSignIn pending = _pending;
            if (pending == null || state == null || !pending.state().equals(state))
                return null;
            _pending = null;
            if (now > pending.startedAt() + PENDING_SIGN_IN_MILLIS)
                return null;
            return pending;
        }

        /** Whether a request has come with this session's cookie since `time`. */
        public boolean usedSince(long time) {
            return _lastAccess >= time;
        }
    }

    private final Map<String, Session> _sessions = new ConcurrentHashMap<>();
    private final SecureRandom _random = new SecureRandom();

    public void start() {
        Thread sweeper = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(SWEEP_INTERVAL_MILLIS);
                } catch (InterruptedException exp) {
                    return;
                }
                removeIdle(System.currentTimeMillis());
            }
        }, "session-sweeper");
        sweeper.setDaemon(true);
        sweeper.start();
    }

    /**
     * Ends every session not used for SESSION_IDLE_MILLIS, and much sooner the ones that are nobody's: a session
     * with neither a player nor a PC account exists only to carry a sign-in to Argonauth and back, and anyone can
     * have one made by asking to sign in.
     */
    void removeIdle(long now) {
        _sessions.values().removeIf(session -> now > session._lastAccess
                + (session.getPlayer() == null && session.getPcAccount() == null ? NOBODYS_SESSION_IDLE_MILLIS : SESSION_IDLE_MILLIS));
    }

    /** The session of a cookie value, counted as used now; null if there is none. */
    public Session getSession(String sessionId) {
        if (sessionId == null)
            return null;
        Session session = _sessions.get(sessionId);
        if (session != null)
            session._lastAccess = System.currentTimeMillis();
        return session;
    }

    /** The name of the alias a cookie value plays as, or null. */
    public String getLoggedUser(String sessionId) {
        Session session = getSession(sessionId);
        return session == null ? null : session.getPlayer();
    }

    /** A new session with nothing in it. */
    public Session newSession() {
        long now = System.currentTimeMillis();
        while (true) {
            Session session = new Session(newId(), now);
            if (_sessions.putIfAbsent(session.getId(), session) == null)
                return session;
        }
    }

    /** A password login: a new session playing as `userName`. Returns the cookie's value. */
    public String logUser(String userName) {
        Session session = newSession();
        session.setPlayer(userName);
        return session.getId();
    }

    /**
     * The same session under a new id, the old id ended: done when a sign-in completes, so that an id known from
     * before the sign-in is worth nothing after it.
     */
    public Session renew(Session old) {
        Session session = newSession();
        session._player = old._player;
        session._pcAccount = old._pcAccount;
        _sessions.remove(old.getId());
        // A request already holding the old session is left holding nothing.
        old._player = null;
        old._pcAccount = null;
        old._pending = null;
        return session;
    }

    /** Every session there is, as of now. */
    public List<Session> sessions() {
        return new ArrayList<>(_sessions.values());
    }

    /**
     * Whether there are as many sessions as there may be. A session costs a request (asking to sign in with a PC
     * account makes one for a browser that has none), so there is a ceiling, far above any use the game has seen.
     */
    public boolean isFull() {
        return _sessions.size() >= MAX_SESSIONS;
    }

    public void end(String sessionId) {
        if (sessionId != null)
            _sessions.remove(sessionId);
    }

    /**
     * Ends every session playing as `userName` (a ban). A PC account signed in on one goes with it: the person
     * signs in again, and entering a banned alias is refused.
     */
    public void forceLogoutUser(String userName) {
        _sessions.values().removeIf(session -> userName.equals(session._player));
    }

    private String newId() {
        StringBuilder result = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++)
            result.append(ID_CHARS[_random.nextInt(ID_CHARS.length)]);
        return result.toString();
    }
}
