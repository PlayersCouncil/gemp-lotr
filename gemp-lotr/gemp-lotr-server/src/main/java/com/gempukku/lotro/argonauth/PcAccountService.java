package com.gempukku.lotro.argonauth;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.gempukku.lotro.argonauth.ArgonauthClient.Assertion;
import com.gempukku.lotro.argonauth.ArgonauthClient.SignIn;
import com.gempukku.lotro.db.LoginInvalidException;
import com.gempukku.lotro.db.PlayerArgonauthDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.LoggedUserHolder;
import com.gempukku.lotro.service.LoggedUserHolder.PcAccount;
import com.gempukku.lotro.service.LoggedUserHolder.PendingSignIn;
import com.gempukku.lotro.service.LoggedUserHolder.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * What a PC account does in Gemp: sign in, and enter, create and link aliases (PC-ACCOUNT.md, which is the
 * contract for every answer here). The request handler turns these into HTTP and nothing more.
 *
 * Almost everything here may wait on Argonauth, and none of it is called on one of the server's IO threads.
 */
public class PcAccountService {
    private static final Logger _log = LogManager.getLogger(PcAccountService.class);

    /** The Angular app, which every landing is a place in. */
    public static final String APP = "/gemp-lotr/ng";
    /** As the app's own isReturnPath: a path inside the app and nothing that could lead out of it. */
    private static final Pattern APP_PATH = Pattern.compile("^/(?!/)[A-Za-z0-9_\\-/?=&%.+]{1,200}$");

    /** Aliases one PC account may create in an hour. Argonauth takes 20 links an hour from a person, creating and linking together. */
    static final int CREATED_PER_HOUR = 10;
    private static final long HOUR_MILLIS = 1000L * 60 * 60;

    /** Password guesses, counted the same way wherever a Gemp password is tried. */
    public interface Attempts {
        boolean isBlocked(String name, String ip, long now);

        void failed(String name, String ip, long now);

        void succeeded(String name, String ip, long now);
    }

    /** An action that is refused: the HTTP status and the `error` of PC-ACCOUNT.md. */
    public static class Refusal extends Exception {
        private final int _status;

        public Refusal(int status, String error) {
            super(error);
            _status = status;
        }

        public int getStatus() {
            return _status;
        }

        public String toJson() {
            JSONObject json = new JSONObject();
            json.put("error", getMessage());
            return json.toJSONString();
        }
    }

    public record Alias(int id, String name, boolean current, boolean busy) {
    }

    /** `GET /session`: who a browser is, as far as PC accounts go. */
    public record View(String mode, String player, String pcAccountName, String manageUrl, boolean fresh,
                       Boolean linked, List<Alias> aliases, String blockedBy) {
        public String toJson() {
            JSONObject json = new JSONObject(new LinkedHashMap<>());
            json.put("mode", mode);
            json.put("player", player);
            if (pcAccountName == null) {
                json.put("pcAccount", null);
            } else {
                JSONObject pcAccount = new JSONObject(new LinkedHashMap<>());
                pcAccount.put("name", pcAccountName);
                pcAccount.put("manageUrl", manageUrl);
                pcAccount.put("fresh", fresh);
                json.put("pcAccount", pcAccount);
            }
            json.put("linked", linked);
            JSONArray list = new JSONArray();
            for (Alias alias : aliases) {
                JSONObject item = new JSONObject(new LinkedHashMap<>());
                item.put("id", alias.id());
                item.put("name", alias.name());
                item.put("current", alias.current());
                item.put("busy", alias.busy());
                list.add(item);
            }
            json.put("aliases", list);
            json.put("blockedBy", blockedBy);
            return json.toJSONString(JSONWriter.Feature.WriteMapNullValue);
        }
    }

    /** Where a sign-in ends: the session to give the browser (null to leave its cookie alone) and where to send it. */
    public record Landing(Session session, String location) {
    }

    private final ArgonauthConfig _config;
    private final ArgonauthClient _client;
    private final PlayerDAO _playerDao;
    private final PlayerArgonauthDAO _linkDao;
    private final LoggedUserHolder _sessions;
    private final Predicate<String> _busy;
    private final Attempts _attempts;
    private final LongSupplier _clock;
    private final Map<String, Deque<Long>> _created = new ConcurrentHashMap<>();

    /**
     * @param client null in test mode, where nothing here reaches for it
     * @param busy   whether the player of this name is at a table, in a queue, in a running tournament or in a game
     */
    public PcAccountService(ArgonauthConfig config, ArgonauthClient client, PlayerDAO playerDao,
                            PlayerArgonauthDAO linkDao, LoggedUserHolder sessions, Predicate<String> busy,
                            Attempts attempts, LongSupplier clock) {
        _config = config;
        _client = client;
        _playerDao = playerDao;
        _linkDao = linkDao;
        _sessions = sessions;
        _busy = busy;
        _attempts = attempts;
        _clock = clock;
    }

    public ArgonauthConfig getConfig() {
        return _config;
    }

    // ---- who this browser is

    public View view(Session session) throws SQLException {
        String mode = _config.mode().name().toLowerCase(Locale.ROOT);
        String playerName = session == null ? null : session.getPlayer();
        PcAccount pcAccount = !_config.enabled() || session == null ? null : session.getPcAccount();
        Boolean linked = null;
        if (playerName != null) {
            Player player = _playerDao.getPlayer(playerName);
            linked = _config.enabled() && player != null && _linkDao.getSub(player.getId()) != null;
        }
        if (pcAccount == null)
            return new View(mode, playerName, null, null, false, linked, List.of(), null);

        List<Alias> aliases = new ArrayList<>();
        for (Player alias : aliasesOf(pcAccount.sub()))
            aliases.add(new Alias(alias.getId(), alias.getName(), alias.getName().equals(playerName), _busy.test(alias.getName())));
        // The gate is this browser's own: the alias it plays, while that alias is in the middle of something.
        String blockedBy = playerName != null && _busy.test(playerName) ? playerName : null;
        return new View(mode, playerName, pcAccount.name(), _config.issuer() + "/", pcAccount.isFresh(_clock.getAsLong()),
                linked, aliases, blockedBy);
    }

    /** The accounts this PC account owns by Gemp's copy, by name. */
    private List<Player> aliasesOf(String sub) throws SQLException {
        List<Player> aliases = new ArrayList<>();
        for (int playerId : _linkDao.getPlayerIds(sub)) {
            Player player = _playerDao.getPlayer(playerId);
            if (player != null)
                aliases.add(player);
        }
        aliases.sort((one, other) -> one.getName().compareToIgnoreCase(other.getName()));
        return aliases;
    }

    // ---- signing in

    /**
     * Starts a sign-in for this session and returns Argonauth's address to send the browser to.
     *
     * @param then       "link" or "create": land on the alias list with that form open. Anything else is a plain sign-in
     * @param returnPath a place in the app to go to once an alias is entered; anything else is dropped
     */
    public String startSignIn(Session session, String then, String returnPath) throws ArgonauthException {
        return begin(session, then != null && List.of("link", "create").contains(then) ? then : "", returnPath);
    }

    /**
     * Connect PC Account: starts the sign-in that links the account this browser is logged in to, by its password,
     * to the PC account that then signs in. Returns Argonauth's address to send the browser to.
     *
     * The password is asked for again here. A browser that is merely logged in (a shared computer, a borrowed
     * cookie) must not be able to make the account somebody's for good, and nothing another site can send a
     * browser to (a link, a form) starts this.
     */
    public String startConnect(Session session, String password, String remoteIp) throws Refusal, SQLException {
        String playing = session == null ? null : session.getPlayer();
        // Only a password login connects. A PC account's own aliases are linked already, and one it has lost
        // (an admin's unlink) is not to be taken back this way.
        if (playing == null || session.getPcAccount() != null)
            throw new Refusal(403, "not-allowed");
        proveByPassword(playing, password, remoteIp);
        try {
            return begin(session, "connect", null);
        } catch (ArgonauthException exp) {
            _log.warn("A PC sign-in could not be started (" + exp.getReason() + "): " + exp.getMessage());
            throw new Refusal(503, "unavailable");
        }
    }

    private String begin(Session session, String purpose, String returnPath) throws ArgonauthException {
        String state = ArgonauthClient.randomToken();
        String nonce = ArgonauthClient.randomToken();
        String codeVerifier = ArgonauthClient.randomToken();
        String address = _client.signInAddress(state, nonce, codeVerifier);
        session.setPending(new PendingSignIn(state, nonce, codeVerifier, purpose,
                isAppPath(returnPath) ? returnPath : "", _clock.getAsLong()));
        return address;
    }

    /**
     * The player `name` is, proved by its password, with guesses counted as at the login form. An account without
     * a password proves nothing by one, and an account that cannot log in is not proved by logging in. Neither is
     * told apart from a wrong password.
     */
    private Player proveByPassword(String name, String password, String remoteIp) throws Refusal, SQLException {
        long now = _clock.getAsLong();
        if (_attempts.isBlocked(name, remoteIp, now))
            throw new Refusal(429, "slow-down");
        Player player = name.isEmpty() || password == null || password.isEmpty() ? null : _playerDao.loginUser(name, password);
        if (player == null || player.getPassword() == null || player.getPassword().isEmpty() || !player.hasType(Player.Type.USER)) {
            _attempts.failed(name, remoteIp, now);
            throw new Refusal(401, "bad-credentials");
        }
        _attempts.succeeded(name, remoteIp, now);
        return player;
    }

    static boolean isAppPath(String path) {
        // No ".." either: the browser would resolve it, and "/../../x" is a place on this site outside the app.
        return path != null && APP_PATH.matcher(path).matches() && !path.startsWith("/login") && !path.contains("..");
    }

    /**
     * Argonauth's answer to a sign-in this session started. Never throws: whatever happens, the browser is sent
     * somewhere in the app that can say what happened.
     */
    public Landing completeSignIn(Session session, String code, String state, String error, String remoteIp) {
        if (session == null)
            return new Landing(null, APP + "/login?pc=failed");
        // The answer is taken once and only by the session that asked: a `state` from anywhere else finds nothing.
        PendingSignIn pending = session.takePending(state, _clock.getAsLong());
        if (pending == null)
            return new Landing(null, APP + "/login?pc=failed");
        // Connecting was started from My Account, and whatever comes of it is said there.
        boolean connecting = pending.then().equals("connect");
        String page = APP + (connecting ? "/account" : "/login");
        String failed = page + "?pc=failed";
        if (error != null && !error.isEmpty())
            return new Landing(null, error.equals("access_denied") ? page + "?pc=cancelled" : failed);
        if (code == null || code.isEmpty())
            return new Landing(null, failed);

        try {
            SignIn signIn = _client.completeSignIn(code, pending.codeVerifier(), pending.nonce());
            PcAccount pcAccount = new PcAccount(signIn.sub(), signIn.displayName(), signIn.accessToken(),
                    signIn.tokenExpiresAt(), signIn.idToken());

            // What Argonauth says this person owns replaces whatever Gemp thought.
            Map<Integer, String> owned = new LinkedHashMap<>();
            for (ArgonauthClient.Link link : signIn.links()) {
                if (_playerDao.getPlayer(link.playerId()) != null)
                    owned.put(link.playerId(), link.proof() == null ? "unknown" : link.proof());
                else
                    _log.warn("Argonauth links PC account " + signIn.sub() + " to player " + link.playerId() + ", which this Gemp does not have");
            }
            _linkDao.replaceLinks(signIn.sub(), owned);

            Player playing = session.getPlayer() == null ? null : _playerDao.getPlayer(session.getPlayer());
            if (connecting) {
                // Still the password login that gave its password to start this (startConnect).
                if (playing == null || session.getPcAccount() != null)
                    return new Landing(null, failed);
                if (!owned.containsKey(playing.getId())) {
                    Assertion assertion = _client.assertLink(signIn.accessToken(), playing.getId(), playing.getName(), "password");
                    // The session stays a password login: it is not handed a PC account that does not own its alias.
                    if (assertion == Assertion.LINKED_ELSEWHERE)
                        return new Landing(null, APP + "/account?pc=linked-elsewhere");
                    if (assertion != Assertion.LINKED)
                        return new Landing(null, failed);
                    _linkDao.setLink(playing.getId(), signIn.sub(), "password");
                }
                session.setPcAccount(pcAccount);
                return new Landing(_sessions.renew(session), APP + "/account?pc=connected");
            }

            // The session is this PC account's now. An alias it was playing stays only if it is one of theirs.
            if (playing == null || !owned.containsKey(playing.getId()))
                session.setPlayer(null);
            session.setPcAccount(pcAccount);
            Session renewed = _sessions.renew(session);

            if (pending.then().equals("link") || pending.then().equals("create"))
                return new Landing(renewed, APP + "/login?open=" + pending.then());

            // With one alias there is nothing to choose: straight in. Argonauth has just said it is theirs.
            if (owned.size() == 1) {
                Player only = _playerDao.getPlayer(owned.keySet().iterator().next());
                try {
                    play(renewed, only, remoteIp);
                    return new Landing(renewed, APP + (pending.returnPath().isEmpty() ? "/hall" : pending.returnPath()));
                } catch (Refusal refusal) {
                    // Banned: the list will say so when it is tried.
                }
            }
            return new Landing(renewed, APP + "/login");
        } catch (ArgonauthException exp) {
            _log.warn("A PC sign-in failed (" + exp.getReason() + "): " + exp.getMessage());
            return new Landing(null, failed);
        } catch (Exception exp) {
            _log.error("A PC sign-in failed", exp);
            return new Landing(null, failed);
        }
    }

    // ---- the alias list's actions

    public View enter(Session session, int playerId, String remoteIp) throws Refusal, SQLException {
        PcAccount pcAccount = pcAccountOf(session);
        Player player = _playerDao.getPlayer(playerId);
        if (player == null)
            throw new Refusal(403, "not-linked");

        // Gemp's copy only draws the list. Whether this account is theirs is Argonauth's to say, every time: an
        // admin's unlink there is not sent here.
        ArgonauthClient.Owner owner;
        try {
            owner = _client.ownerOf(playerId);
        } catch (ArgonauthException exp) {
            _log.warn("Argonauth could not be asked who owns player " + playerId + " (" + exp.getReason() + "): " + exp.getMessage());
            throw new Refusal(503, "unavailable");
        }
        String copy = _linkDao.getSub(playerId);
        if (owner == null) {
            if (copy != null)
                _linkDao.removeLink(playerId);
            throw new Refusal(403, "not-linked");
        }
        if (!owner.sub().equals(copy))
            _linkDao.setLink(playerId, owner.sub(), owner.proof());
        if (!owner.sub().equals(pcAccount.sub()))
            throw new Refusal(403, "not-linked");

        leavingIsAllowed(session, player.getName());
        play(session, player, remoteIp);
        return view(session);
    }

    /**
     * The switching gate (Ketura, 2026-11-23): a browser does not leave the alias it is playing for another while
     * that alias sits at a table, in a queue, in a running tournament or in a game. It is this browser's alone:
     * what the PC account's other aliases are doing elsewhere does not come into it, since playing oneself with two
     * of one's own accounts, in two browsers, is a thing people do. Logging out is never gated.
     *
     * @param to the alias being entered, or null for one about to be created
     */
    private void leavingIsAllowed(Session session, String to) throws Refusal {
        String playing = session.getPlayer();
        if (playing != null && !playing.equals(to) && _busy.test(playing))
            throw new Refusal(409, "busy");
    }

    /** The checks a password login makes, and then the session plays as this player. */
    private void play(Session session, Player asked, String remoteIp) throws Refusal, SQLException {
        // Read again: Argonauth may have been waited on since `asked` was, and a ban may have landed meanwhile.
        Player player = _playerDao.getPlayer(asked.getId());
        if (player == null || !player.hasType(Player.Type.USER) || player.isTemporarilyBannedAt(new Date(_clock.getAsLong())))
            throw new Refusal(403, "banned");
        session.setPlayer(player.getName());
        _playerDao.updateLastLoginIp(player.getName(), remoteIp);
    }

    public View create(Session session, String name, String remoteIp) throws Refusal, SQLException {
        PcAccount pcAccount = freshPcAccountOf(session);
        // A new alias is entered at once, which is a switch like any other.
        leavingIsAllowed(session, null);
        long now = _clock.getAsLong();
        Deque<Long> created = _created.computeIfAbsent(pcAccount.sub(), sub -> new ArrayDeque<>());
        // The place in the hour is taken before anything is waited on, and given back if nothing comes of it:
        // any number asking at once still get ten between them.
        synchronized (created) {
            while (!created.isEmpty() && created.peekFirst() <= now - HOUR_MILLIS)
                created.removeFirst();
            if (created.size() >= CREATED_PER_HOUR)
                throw new Refusal(429, "slow-down");
            created.addLast(now);
        }
        boolean made = false;
        try {
            View view = createReserved(session, pcAccount, name, remoteIp);
            made = true;
            return view;
        } finally {
            if (!made) {
                synchronized (created) {
                    created.removeLastOccurrence(now);
                }
            }
        }
    }

    private View createReserved(Session session, PcAccount pcAccount, String name, String remoteIp) throws Refusal, SQLException {
        Player player;
        try {
            player = _playerDao.registerAlias(name == null ? "" : name.trim(), remoteIp);
        } catch (LoginInvalidException exp) {
            throw new Refusal(400, "invalid-name");
        }
        if (player == null)
            throw new Refusal(409, "name-taken");

        Refusal refusal;
        try {
            Assertion assertion = _client.assertLink(pcAccount.accessToken(), player.getId(), player.getName(), "created");
            refusal = switch (assertion) {
                case LINKED -> null;
                case TOKEN_LAPSED -> new Refusal(401, "reauth");
                case RATE_LIMITED -> new Refusal(429, "slow-down");
                case LINKED_ELSEWHERE -> {
                    // An account made a moment ago cannot be anybody's, unless this Gemp's ids are another Gemp's
                    // too: a test server pointed at an Argonauth that is not its own.
                    _log.error("Argonauth says the new player " + player.getId() + " already belongs to someone:"
                            + " is this Gemp registered at an Argonauth that another Gemp uses?");
                    yield new Refusal(503, "unavailable");
                }
            };
        } catch (ArgonauthException exp) {
            _log.warn("A new alias could not be recorded at Argonauth (" + exp.getReason() + "): " + exp.getMessage());
            refusal = new Refusal(503, "unavailable");
            // No answer is not a no: Argonauth may have recorded the link and the answer been lost. Ask before
            // taking the account back, or Argonauth is left with a link to an account that is gone.
            try {
                ArgonauthClient.Owner owner = _client.ownerOf(player.getId());
                if (owner != null && owner.sub().equals(pcAccount.sub()))
                    refusal = null;
            } catch (ArgonauthException again) {
                _log.error("Player " + player.getId() + " (" + player.getName() + ") is being taken back unrecorded, and Argonauth"
                        + " cannot be asked whether it recorded the link after all. If it did, unlink gemp " + player.getId() + " there.");
            }
        }
        if (refusal != null) {
            // Unrecorded, nobody could ever enter it: the name goes back.
            _playerDao.removeUnusedAlias(player.getId());
            throw refusal;
        }

        _linkDao.setLink(player.getId(), pcAccount.sub(), "created");
        play(session, player, remoteIp);
        return view(session);
    }

    public View link(Session session, String login, String password, String remoteIp) throws Refusal, SQLException {
        PcAccount pcAccount = freshPcAccountOf(session);
        Player player = proveByPassword(login == null ? "" : login.trim(), password, remoteIp);

        Assertion assertion;
        try {
            assertion = _client.assertLink(pcAccount.accessToken(), player.getId(), player.getName(), "password");
        } catch (ArgonauthException exp) {
            _log.warn("A link could not be recorded at Argonauth (" + exp.getReason() + "): " + exp.getMessage());
            throw new Refusal(503, "unavailable");
        }
        switch (assertion) {
            case LINKED -> _linkDao.setLink(player.getId(), pcAccount.sub(), "password");
            case LINKED_ELSEWHERE -> throw new Refusal(409, "linked-elsewhere");
            case TOKEN_LAPSED -> throw new Refusal(401, "reauth");
            case RATE_LIMITED -> throw new Refusal(429, "slow-down");
        }
        return view(session);
    }

    private PcAccount pcAccountOf(Session session) throws Refusal {
        PcAccount pcAccount = session == null ? null : session.getPcAccount();
        if (pcAccount == null)
            throw new Refusal(401, "no-pc-account");
        return pcAccount;
    }

    /** Creating and linking are said to Argonauth with the person's own token, which lasts ten minutes. */
    private PcAccount freshPcAccountOf(Session session) throws Refusal {
        PcAccount pcAccount = pcAccountOf(session);
        if (!pcAccount.isFresh(_clock.getAsLong()))
            throw new Refusal(401, "reauth");
        return pcAccount;
    }

    // ---- an alias being played, looked at again

    /** How often the aliases being played are looked at again. */
    static final long RECHECK_MILLIS = 1000L * 60 * 5;
    private volatile long _lastRecheck;

    /** Starts looking again, every few minutes, at who owns the alias each PC account's browser is playing. */
    public void start() {
        if (!_config.enabled())
            return;
        Thread.ofVirtual().name("pc-account-recheck").start(() -> {
            while (true) {
                try {
                    Thread.sleep(RECHECK_MILLIS);
                } catch (InterruptedException exp) {
                    return;
                }
                try {
                    recheckAliases();
                } catch (Exception exp) {
                    _log.error("Looking again at the aliases being played failed", exp);
                }
            }
        });
    }

    /**
     * Entering is checked with Argonauth, but a session lasts a month, and an unlink at Argonauth (an admin
     * undoing a wrong link) is not sent here. So each alias a PC account's browser is playing, in a session used
     * since the last look, is asked about again: one that is no longer theirs is put down, and the browser is
     * back at the alias list at its next request. An Argonauth that cannot be asked ends the look, and changes
     * nothing.
     */
    void recheckAliases() throws SQLException {
        // by the wall clock, as the sessions' own record of their use is
        long since = _lastRecheck;
        _lastRecheck = System.currentTimeMillis();
        Map<Integer, ArgonauthClient.Owner> asked = new LinkedHashMap<>();
        for (Session session : _sessions.sessions()) {
            PcAccount pcAccount = session.getPcAccount();
            String playing = session.getPlayer();
            if (pcAccount == null || playing == null || !session.usedSince(since))
                continue;
            Player player = _playerDao.getPlayer(playing);
            if (player == null)
                continue;
            if (!asked.containsKey(player.getId())) {
                try {
                    asked.put(player.getId(), _client.ownerOf(player.getId()));
                } catch (ArgonauthException exp) {
                    _log.warn("Argonauth could not be asked who owns player " + player.getId() + " (" + exp.getReason() + "): " + exp.getMessage());
                    return;
                }
            }
            ArgonauthClient.Owner owner = asked.get(player.getId());
            if (owner != null && owner.sub().equals(pcAccount.sub()))
                continue;
            _log.info(playing + " no longer belongs to the PC account playing it, by Argonauth: put down");
            if (owner == null)
                _linkDao.removeLink(player.getId());
            else
                _linkDao.setLink(player.getId(), owner.sub(), owner.proof());
            // only if it is still what the session plays: it may have switched while Argonauth was asked
            if (playing.equals(session.getPlayer()))
                session.setPlayer(null);
        }
    }

    // ---- logging out

    /**
     * Ends the alias ("alias": a PC account stays signed in, and its list is where the browser goes) or everything
     * ("all": Argonauth's session too, by way of its sign-out page). A password login has only the one thing to end.
     *
     * @return where to send the browser
     */
    public String logOut(Session session, String scope) {
        String loginPage = APP + "/login";
        if (session == null)
            return loginPage;
        PcAccount pcAccount = _config.enabled() ? session.getPcAccount() : null;
        if (pcAccount != null && !"all".equals(scope)) {
            session.setPlayer(null);
            return loginPage;
        }
        _sessions.end(session.getId());
        if (pcAccount == null)
            return loginPage;
        try {
            return _client.signOutAddress(pcAccount.idToken());
        } catch (ArgonauthException exp) {
            // Gemp's session is over either way. Argonauth's stays until it lapses or is ended there.
            _log.warn("Argonauth's sign-out address could not be had (" + exp.getReason() + "): " + exp.getMessage());
            return loginPage;
        }
    }
}
