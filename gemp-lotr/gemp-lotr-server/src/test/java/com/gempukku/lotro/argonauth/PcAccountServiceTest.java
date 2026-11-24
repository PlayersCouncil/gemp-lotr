package com.gempukku.lotro.argonauth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.argonauth.FakeArgonauth.LinkRow;
import com.gempukku.lotro.argonauth.FakeArgonauth.Person;
import com.gempukku.lotro.argonauth.PcAccountService.Alias;
import com.gempukku.lotro.argonauth.PcAccountService.Landing;
import com.gempukku.lotro.argonauth.PcAccountService.Refusal;
import com.gempukku.lotro.argonauth.PcAccountService.View;
import com.gempukku.lotro.db.LoginInvalidException;
import com.gempukku.lotro.db.PlayerArgonauthDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.LoggedUserHolder;
import com.gempukku.lotro.service.LoggedUserHolder.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * The PC account's rules (PC-ACCOUNT.md), against an Argonauth that answers on the real wire: who gets in, as whom,
 * and what each refusal is.
 */
public class PcAccountServiceTest {
    private static final Person FRODO = new Person("760f5b53-1a93-4a03-91a7-d812c8f0fc23", "frodo", "Frodo Baggins");
    private static final Person SAM = new Person("0d9b6a0e-52a7-4a55-a3a5-5c0b1c7f7c11", "sam", "Samwise");
    private static final String IP = "203.0.113.7";

    private FakeArgonauth _argonauth;
    private LoggedUserHolder _sessions;
    private PcAccountService _service;

    // ---- a Gemp of its own: players, the copy of the links, who is busy, password guesses
    private final Map<Integer, Player> _players = new TreeMap<>();
    private final Map<Integer, String[]> _copy = new TreeMap<>();
    private final Set<String> _busy = new HashSet<>();
    private final List<String> _lastIps = new ArrayList<>();
    private final List<String> _failedAttempts = new ArrayList<>();
    private final Set<String> _blockedNames = new HashSet<>();
    private int _nextPlayerId = 5000;

    private final PlayerArgonauthDAO _linkDao = new PlayerArgonauthDAO() {
        @Override
        public List<Integer> getPlayerIds(String sub) {
            return _copy.entrySet().stream().filter(link -> link.getValue()[0].equals(sub)).map(Map.Entry::getKey).toList();
        }

        @Override
        public String getSub(int playerId) {
            return _copy.containsKey(playerId) ? _copy.get(playerId)[0] : null;
        }

        @Override
        public void setLink(int playerId, String sub, String proof) {
            _copy.put(playerId, new String[]{sub, proof});
        }

        @Override
        public void removeLink(int playerId) {
            _copy.remove(playerId);
        }

        @Override
        public void replaceLinks(String sub, Map<Integer, String> proofByPlayerId) {
            _copy.entrySet().removeIf(link -> link.getValue()[0].equals(sub) && !proofByPlayerId.containsKey(link.getKey()));
            proofByPlayerId.forEach((playerId, proof) -> setLink(playerId, sub, proof));
        }
    };

    private final PcAccountService.Attempts _attempts = new PcAccountService.Attempts() {
        @Override
        public boolean isBlocked(String name, String ip, long now) {
            return _blockedNames.contains(name);
        }

        @Override
        public void failed(String name, String ip, long now) {
            _failedAttempts.add(name + " from " + ip);
        }

        @Override
        public void succeeded(String name, String ip, long now) {
            _failedAttempts.removeIf(attempt -> attempt.equals(name + " from " + ip));
        }
    };

    @Before
    public void start() throws Exception {
        _argonauth = new FakeArgonauth();
        _sessions = new LoggedUserHolder();
        _service = service(_argonauth.config());
    }

    @After
    public void stop() {
        _argonauth.close();
    }

    private PcAccountService service(ArgonauthConfig config) throws Exception {
        ArgonauthClient client = config.enabled()
                ? new ArgonauthClient(config, _argonauth.now::get, Duration.ofSeconds(5)) : null;
        return new PcAccountService(config, client, players(), _linkDao, _sessions, _busy::contains, _attempts, _argonauth.now::get);
    }

    /** The player table, as far as this service touches it. Passwords are kept as typed. */
    private PlayerDAO players() throws Exception {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        Mockito.when(dao.getPlayer(Mockito.anyInt())).thenAnswer(call -> _players.get((int) call.getArgument(0)));
        Mockito.when(dao.getPlayer(Mockito.anyString())).thenAnswer(call -> byName(call.getArgument(0)));
        Mockito.when(dao.loginUser(Mockito.anyString(), Mockito.anyString())).thenAnswer(call -> {
            Player player = byName(call.getArgument(0));
            // more lenient than the real one, which never answers an account with a blank password: the service
            // must not rely on that
            return player != null && (player.getPassword().isEmpty() || player.getPassword().equals(call.getArgument(1))) ? player : null;
        });
        Mockito.when(dao.registerAlias(Mockito.anyString(), Mockito.anyString())).thenAnswer(call -> {
            String name = call.getArgument(0);
            if (!name.matches("[A-Za-z0-9_-]{2,30}") || name.toLowerCase().startsWith("admin"))
                throw new LoginInvalidException();
            if (_players.values().stream().anyMatch(player -> player.getName().equalsIgnoreCase(name)))
                return null;
            return player(_nextPlayerId++, name, "", "u", null);
        });
        Mockito.doAnswer(call -> _players.remove((int) call.getArgument(0))).when(dao).removeUnusedAlias(Mockito.anyInt());
        Mockito.doAnswer(call -> _lastIps.add(call.getArgument(0) + " from " + call.getArgument(1)))
                .when(dao).updateLastLoginIp(Mockito.anyString(), Mockito.anyString());
        return dao;
    }

    private Player byName(String name) {
        return _players.values().stream().filter(player -> player.getName().equals(name)).findFirst().orElse(null);
    }

    private Player player(int id, String name, String password, String type, Date bannedUntil) {
        Player player = new Player(id, name, password, type, null, bannedUntil, null, null, false);
        _players.put(id, player);
        return player;
    }

    /** A Gemp account that Argonauth and Gemp's copy both have as this person's. */
    private Player alias(Person person, int id, String name) {
        Player player = player(id, name, "pass-" + name, "u", null);
        _argonauth.link(person, id, name, "password");
        _copy.put(id, new String[]{person.sub(), "password"});
        return player;
    }

    /** Through Argonauth and back, as the browser holding `session` would go. */
    private Landing signIn(Session session, Person person, String then, String returnPath) throws Exception {
        Map<String, String> back = _argonauth.signIn(_service.startSignIn(session, then, returnPath), person);
        return _service.completeSignIn(session, back.get("code"), back.get("state"), null, IP);
    }

    /** A browser signed in as this person, wherever that landed. */
    private Session signedIn(Person person) throws Exception {
        Landing landing = signIn(_sessions.newSession(), person, null, null);
        assertNotNull("the sign-in failed: " + landing.location(), landing.session());
        return landing.session();
    }

    /** A browser logged in to this account by its password. */
    private Session loggedIn(String name) {
        return _sessions.getSession(_sessions.logUser(name));
    }

    private static void assertRefused(int status, String error, ThrowingCall call) throws Exception {
        try {
            call.run();
            fail("was not refused");
        } catch (Refusal refusal) {
            assertEquals(error, refusal.getMessage());
            assertEquals(status, refusal.getStatus());
            assertEquals("{\"error\":\"" + error + "\"}", refusal.toJson());
        }
    }

    private interface ThrowingCall {
        void run() throws Exception;
    }

    // ---- signing in

    @Test
    public void withOneAliasASignInGoesStraightToTheHall() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session browser = _sessions.newSession();
        String cookieBefore = browser.getId();

        Landing landing = signIn(browser, FRODO, null, null);

        assertEquals("/gemp-lotr/ng/hall", landing.location());
        assertEquals("Frodo", landing.session().getPlayer());
        assertEquals(FRODO.sub(), landing.session().getPcAccount().sub());
        assertEquals("Frodo Baggins", landing.session().getPcAccount().name());
        assertEquals(List.of("Frodo from " + IP), _lastIps);
        // a new session id: one seen before the sign-in is worth nothing after it
        assertNotEquals(cookieBefore, landing.session().getId());
        assertNull(_sessions.getSession(cookieBefore));
        assertSame(landing.session(), _sessions.getSession(landing.session().getId()));
    }

    @Test
    public void aSignInGoesBackToWhereItWasAskedFromIfThatIsInTheApp() throws Exception {
        alias(FRODO, 101, "Frodo");
        assertEquals("/gemp-lotr/ng/deckbuilder?deck=Shire", signIn(_sessions.newSession(), FRODO, null, "/deckbuilder?deck=Shire").location());
        for (String elsewhere : new String[]{"//evil.example/x", "https://evil.example/", "/login", "/login?return=/hall",
                "\\\\evil.example", "/a b", "hall", "", "/x#y", "/" + "a".repeat(201), "/../../x", "/hall/../../other"})
            assertEquals(elsewhere, "/gemp-lotr/ng/hall", signIn(_sessions.newSession(), FRODO, null, elsewhere).location());
    }

    @Test
    public void withSeveralAliasesOrNoneASignInGoesToTheList() throws Exception {
        Landing none = signIn(_sessions.newSession(), SAM, null, "/hall");
        assertEquals("/gemp-lotr/ng/login", none.location());
        assertNull(none.session().getPlayer());
        assertEquals(SAM.sub(), none.session().getPcAccount().sub());

        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Landing several = signIn(_sessions.newSession(), FRODO, null, "/hall");
        assertEquals("/gemp-lotr/ng/login", several.location());
        assertNull(several.session().getPlayer());
        assertTrue(_lastIps.isEmpty());
    }

    @Test
    public void anOnlyAliasThatIsBannedIsNotEntered() throws Exception {
        alias(FRODO, 101, "Frodo");
        player(101, "Frodo", "pass", "", null);
        Landing landing = signIn(_sessions.newSession(), FRODO, null, null);
        assertEquals("/gemp-lotr/ng/login", landing.location());
        assertNull(landing.session().getPlayer());

        player(101, "Frodo", "pass", "u", new Date(_argonauth.now.get() + 60_000));
        assertNull(signIn(_sessions.newSession(), FRODO, null, null).session().getPlayer());

        player(101, "Frodo", "pass", "u", new Date(_argonauth.now.get() - 60_000));
        assertEquals("Frodo", signIn(_sessions.newSession(), FRODO, null, null).session().getPlayer());
    }

    @Test
    public void aSignInStartedToLinkOrCreateLandsOnThatForm() throws Exception {
        alias(FRODO, 101, "Frodo");
        Landing link = signIn(_sessions.newSession(), FRODO, "link", "/hall");
        assertEquals("/gemp-lotr/ng/login?open=link", link.location());
        assertNull(link.session().getPlayer());
        assertEquals("/gemp-lotr/ng/login?open=create", signIn(_sessions.newSession(), FRODO, "create", null).location());
        // anything else asked for is a plain sign-in
        assertEquals("/gemp-lotr/ng/hall", signIn(_sessions.newSession(), FRODO, "delete-everything", null).location());
    }

    @Test
    public void argonauthsLinksReplaceGempsCopyAtEverySignIn() throws Exception {
        alias(FRODO, 101, "Frodo");
        // an admin unlinked one there, linked another, and moved a third over from Sam
        alias(FRODO, 102, "Underhill");
        _argonauth.unlink(102);
        player(103, "Ringbearer", "pass", "u", null);
        _argonauth.link(FRODO, 103, "Ringbearer", "admin");
        alias(SAM, 104, "Gardener");
        _argonauth.link(FRODO, 104, "Gardener", "admin");
        // and Argonauth knows an account this Gemp has never had (a dev Argonauth's seeded ones)
        _argonauth.link(FRODO, 900001, "Elsewhere", "password");

        Session frodo = signedIn(FRODO);

        assertEquals(List.of(101, 103, 104), _linkDao.getPlayerIds(FRODO.sub()));
        assertEquals("admin", _copy.get(103)[1]);
        assertEquals(List.of(), _linkDao.getPlayerIds(SAM.sub()));
        assertEquals(List.of("Frodo", "Gardener", "Ringbearer"),
                _service.view(frodo).aliases().stream().map(Alias::name).toList());
    }

    @Test
    public void anAnswerIsTakenOnceAndOnlyWithTheStateThatAsked() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session browser = _sessions.newSession();
        Map<String, String> back = _argonauth.signIn(_service.startSignIn(browser, null, null), FRODO);

        // a made-up state gets nothing, and the code is never sent to Argonauth with it
        Landing wrong = _service.completeSignIn(browser, back.get("code"), ArgonauthClient.randomToken(), null, IP);
        assertEquals("/gemp-lotr/ng/login?pc=failed", wrong.location());
        assertNull(wrong.session());
        assertNull(browser.getPcAccount());
        assertFalse(_argonauth.requests.contains("POST /identity/o/api/token"));

        // nor does it call the real sign-in off (another site could send a browser to the callback with one)
        Landing right = _service.completeSignIn(browser, back.get("code"), back.get("state"), null, IP);
        assertEquals("/gemp-lotr/ng/hall", right.location());

        // the answer was taken: the same one again is nothing, in the old session or the new
        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(browser, back.get("code"), back.get("state"), null, IP).location());
        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(right.session(), back.get("code"), back.get("state"), null, IP).location());
    }

    @Test
    public void theSessionFromBeforeASignInIsLeftWithNothing() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session before = _sessions.newSession();
        Landing landing = signIn(before, FRODO, null, null);

        // someone who knew the cookie from before the sign-in, and whose request picked the session up in time
        assertNull(before.getPcAccount());
        assertNull(before.getPlayer());
        assertRefused(401, "no-pc-account", () -> _service.create(before, "Fixated", IP));
        assertRefused(401, "no-pc-account", () -> _service.enter(before, 101, IP));
        assertEquals("Frodo", landing.session().getPlayer());
    }

    @Test
    public void anAnswerIsOnlyTakenByTheBrowserThatAsked() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session victim = _sessions.newSession();
        Session attacker = _sessions.newSession();
        // the attacker signs in as themselves and gets the victim's browser to open the callback
        Map<String, String> back = _argonauth.signIn(_service.startSignIn(attacker, null, null), FRODO);

        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(victim, back.get("code"), back.get("state"), null, IP).location());
        assertNull(victim.getPcAccount());
        // and a browser with no session at all
        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(null, back.get("code"), back.get("state"), null, IP).location());
    }

    @Test
    public void anAnswerThatComesTooLateIsNotTaken() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session browser = _sessions.newSession();
        Map<String, String> back = _argonauth.signIn(_service.startSignIn(browser, null, null), FRODO);
        _argonauth.now.addAndGet(Duration.ofMinutes(5).toMillis() + 1);
        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(browser, back.get("code"), back.get("state"), null, IP).location());
    }

    @Test
    public void aSignInThePersonGaveUpOrThatArgonauthRefusedSaysSo() throws Exception {
        Session browser = _sessions.newSession();
        String state = FakeArgonauth.form(_service.startSignIn(browser, null, null).split("\\?")[1]).get("state");
        assertEquals("/gemp-lotr/ng/login?pc=cancelled", _service.completeSignIn(browser, null, state, "access_denied", IP).location());

        state = FakeArgonauth.form(_service.startSignIn(browser, null, null).split("\\?")[1]).get("state");
        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(browser, null, state, "server_error", IP).location());

        state = FakeArgonauth.form(_service.startSignIn(browser, null, null).split("\\?")[1]).get("state");
        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(browser, "", state, null, IP).location());
        assertNull(browser.getPcAccount());
    }

    @Test
    public void aSignInArgonauthsAnswerCannotBeTrustedForChangesNothing() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session browser = loggedIn("Somebody");
        _argonauth.claims = claims -> claims.audience("redbook-dev");

        Landing landing = signIn(browser, FRODO, null, null);

        assertEquals("/gemp-lotr/ng/login?pc=failed", landing.location());
        assertNull(landing.session());
        assertNull(browser.getPcAccount());
        assertEquals("Somebody", browser.getPlayer());
        assertSame(browser, _sessions.getSession(browser.getId()));
    }

    @Test
    public void anArgonauthThatIsDownFailsTheSignInBeforeAndAfterTheTrip() throws Exception {
        Session browser = _sessions.newSession();
        Map<String, String> back = _argonauth.signIn(_service.startSignIn(browser, null, null), FRODO);
        _argonauth.statusFor.put("/identity/o/api/token", 503);
        assertEquals("/gemp-lotr/ng/login?pc=failed", _service.completeSignIn(browser, back.get("code"), back.get("state"), null, IP).location());

        _argonauth.close();
        try {
            service(_argonauth.config()).startSignIn(browser, null, null);
            fail("started a sign-in with nowhere to send it");
        } catch (ArgonauthException exp) {
            assertEquals(ArgonauthException.Reason.UNAVAILABLE, exp.getReason());
        }
    }

    @Test
    public void aPasswordLoginThatSignsInKeepsItsAliasOnlyIfItIsThePcAccounts() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        player(201, "Stranger", "pass", "u", null);

        Session asOwn = signIn(loggedIn("Underhill"), FRODO, null, null).session();
        assertEquals("Underhill", asOwn.getPlayer());

        Landing asStranger = signIn(loggedIn("Stranger"), FRODO, null, null);
        assertNull(asStranger.session().getPlayer());
        assertEquals("/gemp-lotr/ng/login", asStranger.location());
    }

    // ---- connecting the account a password login is in

    /** Connect PC Account, from giving the password to coming back from Argonauth as `person`. */
    private Landing connect(Session browser, String password, Person person) throws Exception {
        Map<String, String> back = _argonauth.signIn(_service.startConnect(browser, password, IP), person);
        return _service.completeSignIn(browser, back.get("code"), back.get("state"), null, IP);
    }

    @Test
    public void connectingLinksTheAccountThisBrowserIsLoggedInToByItsPassword() throws Exception {
        player(201, "OldHand", "s3cret", "u", null);
        Session browser = loggedIn("OldHand");
        String cookieBefore = browser.getId();

        Landing landing = connect(browser, "s3cret", FRODO);

        assertEquals("/gemp-lotr/ng/account?pc=connected", landing.location());
        assertEquals(new LinkRow(FRODO.sub(), "OldHand", "password"), _argonauth.linkOf(201));
        assertEquals(FRODO.sub(), _linkDao.getSub(201));
        assertEquals("OldHand", landing.session().getPlayer());
        assertEquals(FRODO.sub(), landing.session().getPcAccount().sub());
        assertNotEquals(cookieBefore, landing.session().getId());
        assertTrue(_service.view(landing.session()).linked());
    }

    @Test
    public void beingLoggedInIsNotEnoughToConnect() throws Exception {
        // a shared computer left logged in, or a borrowed cookie: without the password the account stays its owner's
        player(201, "OldHand", "s3cret", "u", null);
        Session browser = loggedIn("OldHand");

        assertRefused(401, "bad-credentials", () -> _service.startConnect(browser, "guess", IP));
        assertRefused(401, "bad-credentials", () -> _service.startConnect(browser, "", IP));
        assertRefused(401, "bad-credentials", () -> _service.startConnect(browser, null, IP));
        assertEquals(3, _failedAttempts.size());
        _blockedNames.add("OldHand");
        assertRefused(429, "slow-down", () -> _service.startConnect(browser, "s3cret", IP));
        _blockedNames.clear();

        // and the address anyone can be sent to starts a plain sign-in, which connects nothing
        Landing landing = signIn(browser, FRODO, "connect", null);
        assertEquals("/gemp-lotr/ng/login", landing.location());
        assertNull(_argonauth.linkOf(201));
        assertNull(_linkDao.getSub(201));
        assertFalse(_argonauth.requests.contains("POST /api/v1/links"));
    }

    @Test
    public void onlyAPasswordLoginConnects() throws Exception {
        alias(FRODO, 101, "Frodo");
        assertRefused(403, "not-allowed", () -> _service.startConnect(null, "x", IP));
        assertRefused(403, "not-allowed", () -> _service.startConnect(_sessions.newSession(), "x", IP));
        Session frodo = signedIn(FRODO);
        assertRefused(403, "not-allowed", () -> _service.startConnect(frodo, "pass-Frodo", IP));
        assertTrue(_failedAttempts.isEmpty());
    }

    @Test
    public void anAliasAnAdminUnlinkedCannotBeTakenBackByThePcAccountStillPlayingIt() throws Exception {
        // an admin linked the account to the wrong person, saw it, and unlinked it at Argonauth
        alias(FRODO, 101, "Victim");
        Session impostor = signedIn(FRODO);
        assertEquals("Victim", impostor.getPlayer());
        _argonauth.unlink(101);

        assertRefused(403, "not-allowed", () -> _service.startConnect(impostor, "pass-Victim", IP));
        Landing landing = signIn(impostor, FRODO, "connect", null);
        assertNull(_argonauth.linkOf(101));
        // and that sign-in, with Argonauth's word fresh, put the alias down
        assertNull(landing.session().getPlayer());
    }

    @Test
    public void connectingAnAccountThatIsAlreadyTheirsAssertsNothing() throws Exception {
        alias(FRODO, 101, "Frodo");
        Landing landing = connect(loggedIn("Frodo"), "pass-Frodo", FRODO);
        assertEquals("/gemp-lotr/ng/account?pc=connected", landing.location());
        assertFalse(_argonauth.requests.contains("POST /api/v1/links"));
    }

    @Test
    public void anAccountOfAnotherPcAccountCannotBeConnected() throws Exception {
        alias(SAM, 301, "SamGamgee");
        Session browser = loggedIn("SamGamgee");

        Landing landing = connect(browser, "pass-SamGamgee", FRODO);

        assertEquals("/gemp-lotr/ng/account?pc=linked-elsewhere", landing.location());
        // the browser stays what it was: logged in to that account by its password, and no PC account's
        assertNull(landing.session());
        assertNull(browser.getPcAccount());
        assertEquals("SamGamgee", browser.getPlayer());
        assertEquals(SAM.sub(), _argonauth.linkOf(301).sub());
        assertEquals(SAM.sub(), _linkDao.getSub(301));
    }

    @Test
    public void connectingThatFailsSaysSoOnMyAccount() throws Exception {
        player(201, "OldHand", "s3cret", "u", null);
        Session browser = loggedIn("OldHand");

        _argonauth.statusFor.put("/api/v1/links", 429);
        assertEquals("/gemp-lotr/ng/account?pc=failed", connect(browser, "s3cret", FRODO).location());
        _argonauth.statusFor.put("/api/v1/links", 500);
        assertEquals("/gemp-lotr/ng/account?pc=failed", connect(browser, "s3cret", FRODO).location());
        assertNull(browser.getPcAccount());
        assertNull(_linkDao.getSub(201));

        String state = FakeArgonauth.form(_service.startConnect(browser, "s3cret", IP).split("\\?")[1]).get("state");
        assertEquals("/gemp-lotr/ng/account?pc=cancelled", _service.completeSignIn(browser, null, state, "access_denied", IP).location());

        _argonauth.close();
        PcAccountService restarted = service(_argonauth.config());
        assertRefused(503, "unavailable", () -> restarted.startConnect(browser, "s3cret", IP));
    }

    // ---- entering

    @Test
    public void anAliasIsEnteredFromTheList() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session frodo = signedIn(FRODO);

        View view = _service.enter(frodo, 102, IP);

        assertEquals("Underhill", frodo.getPlayer());
        assertEquals("Underhill", view.player());
        assertEquals(List.of(new Alias(101, "Frodo", false, false), new Alias(102, "Underhill", true, false)), view.aliases());
        assertEquals(List.of("Underhill from " + IP), _lastIps);
        // and switched
        assertEquals("Frodo", _service.enter(frodo, 101, IP).player());
    }

    @Test
    public void onlyAPcAccountCanEnterFromTheList() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        assertRefused(401, "no-pc-account", () -> _service.enter(null, 102, IP));
        assertRefused(401, "no-pc-account", () -> _service.enter(_sessions.newSession(), 102, IP));
        // a password login to one alias opens that alias and nothing beside it
        Session byPassword = loggedIn("Frodo");
        assertRefused(401, "no-pc-account", () -> _service.enter(byPassword, 102, IP));
        assertEquals("Frodo", byPassword.getPlayer());
    }

    @Test
    public void anAccountThatIsNotThePcAccountsIsNotEnteredWhateverGempsCopySays() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        alias(SAM, 301, "SamGamgee");
        Session frodo = signedIn(FRODO);

        // somebody else's, and nobody's
        assertRefused(403, "not-linked", () -> _service.enter(frodo, 301, IP));
        player(401, "Loose", "pass", "u", null);
        assertRefused(403, "not-linked", () -> _service.enter(frodo, 401, IP));
        assertRefused(403, "not-linked", () -> _service.enter(frodo, 999999, IP));

        // unlinked at Argonauth since the sign-in: refused, and the list corrected
        _argonauth.unlink(102);
        assertRefused(403, "not-linked", () -> _service.enter(frodo, 102, IP));
        assertEquals(List.of(101), _linkDao.getPlayerIds(FRODO.sub()));

        // moved to Sam at Argonauth since the sign-in: refused, and Gemp's copy follows
        _argonauth.link(SAM, 101, "Frodo", "admin");
        assertRefused(403, "not-linked", () -> _service.enter(frodo, 101, IP));
        assertEquals(SAM.sub(), _linkDao.getSub(101));
        assertNull(frodo.getPlayer());
    }

    @Test
    public void anAccountAnAdminLinkedSinceTheSignInCanBeEntered() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session frodo = signedIn(FRODO);
        player(103, "Recovered", "", "u", null);
        _argonauth.link(FRODO, 103, "Recovered", "admin");

        assertEquals("Recovered", _service.enter(frodo, 103, IP).player());
        assertEquals("admin", _copy.get(103)[1]);
    }

    @Test
    public void nobodyIsEnteredWhileArgonauthCannotBeAsked() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session frodo = signedIn(FRODO);

        _argonauth.statusFor.put("/api/v1/links/", 503);
        assertRefused(503, "unavailable", () -> _service.enter(frodo, 102, IP));
        _argonauth.statusFor.put("/api/v1/links/", 429);
        assertRefused(503, "unavailable", () -> _service.enter(frodo, 102, IP));
        assertNull(frodo.getPlayer());
        assertEquals(List.of(101, 102), _linkDao.getPlayerIds(FRODO.sub()));
    }

    @Test
    public void aBrowserDoesNotLeaveAnAliasThatIsInTheMiddleOfSomething() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session browser = signedIn(FRODO);
        _service.enter(browser, 101, IP);
        _busy.add("Frodo");

        assertRefused(409, "busy", () -> _service.enter(browser, 102, IP));
        assertEquals("Frodo", browser.getPlayer());
        // the busy one itself can always be entered again
        assertEquals("Frodo", _service.enter(browser, 101, IP).player());
        View view = _service.view(browser);
        assertEquals("Frodo", view.blockedBy());
        assertEquals(List.of(new Alias(101, "Frodo", true, true), new Alias(102, "Underhill", false, false)), view.aliases());

        _busy.remove("Frodo");
        assertEquals("Underhill", _service.enter(browser, 102, IP).player());
        assertNull(_service.view(browser).blockedBy());
    }

    @Test
    public void theGateIsEachBrowsersOwn() throws Exception {
        // Playing oneself: two browsers, two aliases of one PC account, one table (Ketura, 2026-11-23).
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session one = signedIn(FRODO);
        _service.enter(one, 101, IP);
        _busy.add("Frodo");

        Session other = signedIn(FRODO);
        View view = _service.enter(other, 102, IP);
        assertEquals("Underhill", view.player());
        // the other alias is seen to be busy, and holds nothing up here
        assertEquals(List.of(new Alias(101, "Frodo", false, true), new Alias(102, "Underhill", true, false)), view.aliases());
        assertNull(view.blockedBy());

        _busy.add("Underhill");
        assertEquals("Frodo", _service.view(one).blockedBy());
        assertEquals("Underhill", _service.view(other).blockedBy());
        assertRefused(409, "busy", () -> _service.enter(other, 101, IP));
    }

    @Test
    public void loggingOutIsNeverGatedAndABrowserPlayingNothingEntersAnything() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session browser = signedIn(FRODO);
        _service.enter(browser, 101, IP);
        _busy.add("Frodo");

        assertEquals("/gemp-lotr/ng/login", _service.logOut(browser, "alias"));
        assertNull(_service.view(browser).blockedBy());
        assertEquals("Underhill", _service.enter(browser, 102, IP).player());
    }

    @Test
    public void aBannedAliasIsNotEntered() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session frodo = signedIn(FRODO);
        player(102, "Underhill", "pass", "", null);
        assertRefused(403, "banned", () -> _service.enter(frodo, 102, IP));
        player(102, "Underhill", "pass", "u", new Date(_argonauth.now.get() + 1000));
        assertRefused(403, "banned", () -> _service.enter(frodo, 102, IP));
        assertNull(frodo.getPlayer());
        assertTrue(_lastIps.isEmpty());
    }

    @Test
    public void aBanThatLandsWhileArgonauthIsAskedStillKeepsTheAliasOut() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session frodo = signedIn(FRODO);
        // the lookup takes its time, and the ban lands during it
        _argonauth.duringLookup = () -> player(102, "Underhill", "pass", "", null);
        assertRefused(403, "banned", () -> _service.enter(frodo, 102, IP));
        assertNull(frodo.getPlayer());
    }

    // ---- an alias being played, looked at again

    @Test
    public void anAliasUnlinkedAtArgonauthIsPutDownAtTheNextLook() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(SAM, 301, "SamGamgee");
        Session frodo = signedIn(FRODO);
        Session sam = signedIn(SAM);
        Session byPassword = loggedIn("Frodo");

        _service.recheckAliases();
        assertEquals("nothing has changed", "Frodo", frodo.getPlayer());

        _argonauth.unlink(101);
        _sessions.getSession(frodo.getId());
        _sessions.getSession(sam.getId());
        _service.recheckAliases();

        assertNull(frodo.getPlayer());
        assertEquals("the PC account is still signed in, at its list", FRODO.sub(), frodo.getPcAccount().sub());
        assertTrue(_service.view(frodo).aliases().isEmpty());
        assertEquals("SamGamgee", sam.getPlayer());
        // a password login is its own proof, and is not Argonauth's to end
        assertEquals("Frodo", byPassword.getPlayer());
    }

    @Test
    public void anAliasMovedToAnotherPcAccountIsPutDownAndTheCopyFollows() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session frodo = signedIn(FRODO);
        _argonauth.link(SAM, 101, "Frodo", "admin");
        _sessions.getSession(frodo.getId());

        _service.recheckAliases();

        assertNull(frodo.getPlayer());
        assertEquals(SAM.sub(), _linkDao.getSub(101));
    }

    @Test
    public void theLookAsksOnlyAboutSessionsInUseAndStopsWhenArgonauthIsAway() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(SAM, 301, "SamGamgee");
        Session frodo = signedIn(FRODO);
        Session sam = signedIn(SAM);
        Thread.sleep(5);
        _service.recheckAliases();
        _argonauth.requests.clear();

        // only Sam's browser has asked for anything since
        Thread.sleep(5);
        _sessions.getSession(sam.getId());
        _argonauth.unlink(101);
        _service.recheckAliases();
        assertEquals(List.of("GET /api/v1/links/301"), _argonauth.requests);
        assertEquals("not asked about, so not put down yet", "Frodo", frodo.getPlayer());

        // an Argonauth that cannot be asked puts nobody down
        _argonauth.unlink(301);
        _argonauth.statusFor.put("/api/v1/links/", 503);
        _sessions.getSession(sam.getId());
        _sessions.getSession(frodo.getId());
        _service.recheckAliases();
        assertEquals("SamGamgee", sam.getPlayer());
        assertEquals("Frodo", frodo.getPlayer());
    }

    // ---- creating

    @Test
    public void aNewAliasIsMadeRecordedAndEntered() throws Exception {
        Session sam = signedIn(SAM);

        View view = _service.create(sam, "  Gardener ", IP);

        Player made = byName("Gardener");
        assertEquals("no password: it is entered through the PC account", "", made.getPassword());
        assertEquals(new LinkRow(SAM.sub(), "Gardener", "created"), _argonauth.linkOf(made.getId()));
        assertEquals(SAM.sub(), _linkDao.getSub(made.getId()));
        assertEquals("Gardener", sam.getPlayer());
        assertEquals(List.of(new Alias(made.getId(), "Gardener", true, false)), view.aliases());
        assertEquals(List.of("Gardener from " + IP), _lastIps);
    }

    @Test
    public void aNameThatCannotBeHadIsRefused() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session sam = signedIn(SAM);
        assertRefused(409, "name-taken", () -> _service.create(sam, "frodo", IP));
        for (String name : new String[]{"", " ", "x", "has space", "Administrator", "~bot", "a".repeat(31)})
            assertRefused(400, "invalid-name", () -> _service.create(sam, name, IP));
        assertRefused(400, "invalid-name", () -> _service.create(sam, null, IP));
        assertNull(sam.getPlayer());
        assertFalse(_argonauth.requests.contains("POST /api/v1/links"));
    }

    @Test
    public void creatingNeedsAPcAccountSignedInWithinTheLastTenMinutes() throws Exception {
        assertRefused(401, "no-pc-account", () -> _service.create(loggedIn("Somebody"), "Gardener", IP));
        Session sam = signedIn(SAM);
        _argonauth.now.addAndGet(600_000);
        assertFalse(_service.view(sam).fresh());
        assertRefused(401, "reauth", () -> _service.create(sam, "Gardener", IP));
        assertNull(byName("Gardener"));

        // through Argonauth again (no page, with its session alive), and it goes
        Session again = signIn(sam, SAM, "create", null).session();
        assertTrue(_service.view(again).fresh());
        assertEquals("Gardener", _service.create(again, "Gardener", IP).player());
    }

    @Test
    public void anAliasThatCouldNotBeRecordedIsTakenBackAndItsNameWithIt() throws Exception {
        Session sam = signedIn(SAM);

        // Argonauth's clock ran ahead of Gemp's: the token lapsed there first
        _argonauth.statusFor.put("/api/v1/links", 401);
        _argonauth.bodyFor.put("/api/v1/links", "{\"error\":\"invalid_user_token\"}");
        assertRefused(401, "reauth", () -> _service.create(sam, "Gardener", IP));
        _argonauth.statusFor.put("/api/v1/links", 429);
        assertRefused(429, "slow-down", () -> _service.create(sam, "Gardener", IP));
        _argonauth.statusFor.put("/api/v1/links", 503);
        assertRefused(503, "unavailable", () -> _service.create(sam, "Gardener", IP));
        // a brand-new id that Argonauth has as somebody's: this Gemp is at the wrong Argonauth
        _argonauth.statusFor.put("/api/v1/links", 409);
        assertRefused(503, "unavailable", () -> _service.create(sam, "Gardener", IP));

        assertNull(byName("Gardener"));
        assertNull(sam.getPlayer());
        assertTrue(_copy.isEmpty());

        _argonauth.statusFor.clear();
        assertEquals("Gardener", _service.create(sam, "Gardener", IP).player());
    }

    @Test
    public void anAliasArgonauthRecordedButNeverAnsweredForIsKept() throws Exception {
        Session sam = signedIn(SAM);
        // the link is recorded, and the answer lost on the way back
        _argonauth.loseNextAssertAnswer = true;

        View view = _service.create(sam, "Gardener", IP);

        assertEquals("Gardener", view.player());
        Player made = byName("Gardener");
        assertNotNull("not taken back: Argonauth has it as Sam's", made);
        assertEquals(SAM.sub(), _argonauth.linkOf(made.getId()).sub());
        assertEquals(SAM.sub(), _linkDao.getSub(made.getId()));
    }

    @Test
    public void manyCreatesAtOnceStillGetTenBetweenThem() throws Exception {
        Session sam = signedIn(SAM);
        _argonauth.delayMillis = 30;
        java.util.concurrent.atomic.AtomicInteger made = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger slowedDown = new java.util.concurrent.atomic.AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        for (int number = 0; number < 25; number++) {
            String name = "Racer" + number;
            threads.add(Thread.ofVirtual().start(() -> {
                try {
                    _service.create(sam, name, IP);
                    made.incrementAndGet();
                } catch (Refusal refusal) {
                    if (refusal.getMessage().equals("slow-down"))
                        slowedDown.incrementAndGet();
                } catch (Exception exp) {
                    throw new IllegalStateException(exp);
                }
            }));
        }
        for (Thread thread : threads)
            thread.join();
        assertEquals(PcAccountService.CREATED_PER_HOUR, made.get());
        assertEquals(25 - PcAccountService.CREATED_PER_HOUR, slowedDown.get());
    }

    @Test
    public void noAliasIsCreatedFromABrowserWhoseAliasIsInTheMiddleOfSomething() throws Exception {
        alias(FRODO, 101, "Frodo");
        alias(FRODO, 102, "Underhill");
        Session browser = signedIn(FRODO);
        _service.enter(browser, 101, IP);
        // a new alias is entered at once, which would be leaving this one
        _busy.add("Frodo");
        assertRefused(409, "busy", () -> _service.create(browser, "Ringbearer", IP));
        assertNull(byName("Ringbearer"));

        // another alias being busy elsewhere holds nothing up
        _busy.remove("Frodo");
        _busy.add("Underhill");
        assertEquals("Ringbearer", _service.create(browser, "Ringbearer", IP).player());
    }

    @Test
    public void aPcAccountCreatesTenAliasesAnHourAndNoMore() throws Exception {
        Session sam = signedIn(SAM);
        for (int number = 0; number < PcAccountService.CREATED_PER_HOUR; number++)
            _service.create(sam, "Gardener" + number, IP);
        // failures do not count against it, successes do
        assertRefused(429, "slow-down", () -> _service.create(sam, "OneMore", IP));
        assertNull(byName("OneMore"));
        // somebody else is not held up
        assertEquals("Ringbearer", _service.create(signedIn(FRODO), "Ringbearer", IP).player());

        _argonauth.now.addAndGet(Duration.ofMinutes(61).toMillis());
        Session again = signIn(sam, SAM, "create", null).session();
        assertEquals("OneMore", _service.create(again, "OneMore", IP).player());
        assertEquals(11, _service.view(again).aliases().size());
    }

    // ---- linking an old account

    @Test
    public void anOldAccountIsLinkedByItsPasswordAndNotEntered() throws Exception {
        player(201, "OldHand", "s3cret", "u", null);
        Session sam = signedIn(SAM);

        View view = _service.link(sam, " OldHand ", "s3cret", IP);

        assertEquals(new LinkRow(SAM.sub(), "OldHand", "password"), _argonauth.linkOf(201));
        assertEquals(List.of(new Alias(201, "OldHand", false, false)), view.aliases());
        assertNull(view.player());
        assertNull(sam.getPlayer());
        assertTrue(_lastIps.isEmpty());
    }

    @Test
    public void aWrongPasswordLinksNothingAndIsCounted() throws Exception {
        player(201, "OldHand", "s3cret", "u", null);
        Session sam = signedIn(SAM);

        assertRefused(401, "bad-credentials", () -> _service.link(sam, "OldHand", "guess", IP));
        assertRefused(401, "bad-credentials", () -> _service.link(sam, "Nobody", "guess", IP));
        assertRefused(401, "bad-credentials", () -> _service.link(sam, "OldHand", "", IP));
        assertRefused(401, "bad-credentials", () -> _service.link(sam, null, null, IP));
        assertEquals(List.of("OldHand from " + IP, "Nobody from " + IP, "OldHand from " + IP, " from " + IP), _failedAttempts);
        assertNull(_argonauth.linkOf(201));

        // too many guesses: the password is not even looked at, right or wrong
        _blockedNames.add("OldHand");
        assertRefused(429, "slow-down", () -> _service.link(sam, "OldHand", "s3cret", IP));
        assertNull(_argonauth.linkOf(201));

        _blockedNames.clear();
        _service.link(sam, "OldHand", "s3cret", IP);
        assertEquals("a success clears the count", List.of("Nobody from " + IP, " from " + IP), _failedAttempts);
    }

    @Test
    public void anAccountThatCannotLogInCannotBeLinkedByLoggingIn() throws Exception {
        // no password (an alias made through a PC account, or a reset never finished), a bot, a banned player
        player(201, "Blank", "", "u", null);
        player(202, "~bot", "", "b", null);
        player(203, "Banned", "s3cret", "", null);
        Session sam = signedIn(SAM);

        assertRefused(401, "bad-credentials", () -> _service.link(sam, "Blank", "anything", IP));
        assertRefused(401, "bad-credentials", () -> _service.link(sam, "~bot", "anything", IP));
        assertRefused(401, "bad-credentials", () -> _service.link(sam, "Banned", "s3cret", IP));
        assertFalse(_argonauth.requests.contains("POST /api/v1/links"));
        assertTrue(_copy.isEmpty());
    }

    @Test
    public void anAccountOfAnotherPcAccountCannotBeLinked() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session sam = signedIn(SAM);
        assertRefused(409, "linked-elsewhere", () -> _service.link(sam, "Frodo", "pass-Frodo", IP));
        assertEquals(FRODO.sub(), _argonauth.linkOf(101).sub());
        assertEquals(FRODO.sub(), _linkDao.getSub(101));
    }

    @Test
    public void linkingNeedsAFreshPcAccountAndAnArgonauthThatAnswers() throws Exception {
        player(201, "OldHand", "s3cret", "u", null);
        assertRefused(401, "no-pc-account", () -> _service.link(loggedIn("OldHand"), "OldHand", "s3cret", IP));
        Session sam = signedIn(SAM);

        _argonauth.statusFor.put("/api/v1/links", 429);
        assertRefused(429, "slow-down", () -> _service.link(sam, "OldHand", "s3cret", IP));
        _argonauth.statusFor.put("/api/v1/links", 502);
        assertRefused(503, "unavailable", () -> _service.link(sam, "OldHand", "s3cret", IP));
        _argonauth.statusFor.put("/api/v1/links", 401);
        _argonauth.bodyFor.put("/api/v1/links", "{\"error\":\"invalid_user_token\"}");
        assertRefused(401, "reauth", () -> _service.link(sam, "OldHand", "s3cret", IP));
        _argonauth.statusFor.clear();

        _argonauth.now.addAndGet(600_000);
        assertRefused(401, "reauth", () -> _service.link(sam, "OldHand", "s3cret", IP));
        assertNull(_linkDao.getSub(201));
    }

    // ---- logging out

    @Test
    public void loggingOutOfTheHallEndsTheAliasAndKeepsThePcAccount() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session frodo = signedIn(FRODO);

        assertEquals("/gemp-lotr/ng/login", _service.logOut(frodo, "alias"));

        assertNull(frodo.getPlayer());
        assertEquals(FRODO.sub(), frodo.getPcAccount().sub());
        assertSame(frodo, _sessions.getSession(frodo.getId()));
        // anything that is not "all" is the lesser one
        _service.enter(frodo, 101, IP);
        assertEquals("/gemp-lotr/ng/login", _service.logOut(frodo, null));
        assertNotNull(_sessions.getSession(frodo.getId()));
    }

    @Test
    public void loggingOutOfTheListEndsEverythingByWayOfArgonauth() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session frodo = signedIn(FRODO);

        String next = _service.logOut(frodo, "all");

        assertNull(_sessions.getSession(frodo.getId()));
        assertTrue(next, next.startsWith(_argonauth.issuer + "/identity/o/logout?"));
        Map<String, String> query = FakeArgonauth.form(next.split("\\?")[1]);
        assertEquals(FakeArgonauth.POST_LOGOUT_URI, query.get("post_logout_redirect_uri"));
        assertEquals(frodo.getPcAccount().idToken(), query.get("id_token_hint"));
    }

    @Test
    public void aPasswordLoginHasOneThingToEnd() throws Exception {
        for (String scope : new String[]{"alias", "all", null}) {
            Session browser = loggedIn("Somebody");
            assertEquals("/gemp-lotr/ng/login", _service.logOut(browser, scope));
            assertNull(_sessions.getSession(browser.getId()));
        }
        assertEquals("/gemp-lotr/ng/login", _service.logOut(null, "all"));
    }

    @Test
    public void loggingOutWorksWithArgonauthAway() throws Exception {
        alias(FRODO, 101, "Frodo");
        Session frodo = signedIn(FRODO);
        // a server that has not yet learned Argonauth's addresses, and cannot now
        PcAccountService restarted = service(_argonauth.config());
        _argonauth.close();

        assertEquals("/gemp-lotr/ng/login", restarted.logOut(frodo, "all"));
        assertNull(_sessions.getSession(frodo.getId()));
    }

    // ---- who this browser is

    @Test
    public void theSessionSaysWhoABrowserIs() throws Exception {
        View nobody = _service.view(null);
        assertEquals("{\"mode\":\"optional\",\"player\":null,\"pcAccount\":null,\"linked\":null,\"aliases\":[],\"blockedBy\":null}", nobody.toJson());
        assertEquals(nobody, _service.view(_sessions.newSession()));

        alias(FRODO, 101, "Frodo");
        player(201, "OldHand", "pass", "u", null);
        assertEquals("{\"mode\":\"optional\",\"player\":\"OldHand\",\"pcAccount\":null,\"linked\":false,\"aliases\":[],\"blockedBy\":null}",
                _service.view(loggedIn("OldHand")).toJson());
        // a password login to a linked account: linked, and still no list
        assertEquals("{\"mode\":\"optional\",\"player\":\"Frodo\",\"pcAccount\":null,\"linked\":true,\"aliases\":[],\"blockedBy\":null}",
                _service.view(loggedIn("Frodo")).toJson());

        alias(FRODO, 102, "underhill");
        alias(FRODO, 103, "Baggins");
        Session frodo = signedIn(FRODO);
        _service.enter(frodo, 102, IP);
        _busy.add("underhill");
        JSONObject json = JSON.parseObject(_service.view(frodo).toJson());
        assertEquals("optional", json.getString("mode"));
        assertEquals("underhill", json.getString("player"));
        assertEquals("{\"name\":\"Frodo Baggins\",\"manageUrl\":\"" + _argonauth.issuer + "/\",\"fresh\":true}", json.getJSONObject("pcAccount").toJSONString());
        assertEquals(true, json.getBoolean("linked"));
        // by name, whatever the case
        assertEquals("[{\"id\":103,\"name\":\"Baggins\",\"current\":false,\"busy\":false},"
                + "{\"id\":101,\"name\":\"Frodo\",\"current\":false,\"busy\":false},"
                + "{\"id\":102,\"name\":\"underhill\",\"current\":true,\"busy\":true}]", json.getJSONArray("aliases").toJSONString());
        assertEquals("underhill", json.getString("blockedBy"));
    }

    // ---- test mode: no PC account anywhere

    @Test
    public void inTestModeTheSessionIsAPasswordLoginAndLoggingOutStillWorks() throws Exception {
        PcAccountService test = service(ArgonauthConfig.TEST);
        alias(FRODO, 101, "Frodo");

        assertEquals("{\"mode\":\"test\",\"player\":null,\"pcAccount\":null,\"linked\":null,\"aliases\":[],\"blockedBy\":null}", test.view(null).toJson());
        Session browser = loggedIn("Frodo");
        // not even "linked": there is no PC account to be linked to
        assertEquals("{\"mode\":\"test\",\"player\":\"Frodo\",\"pcAccount\":null,\"linked\":false,\"aliases\":[],\"blockedBy\":null}", test.view(browser).toJson());

        assertEquals("/gemp-lotr/ng/login", test.logOut(browser, "all"));
        assertNull(_sessions.getSession(browser.getId()));
        assertFalse(test.getConfig().enabled());
    }
}
