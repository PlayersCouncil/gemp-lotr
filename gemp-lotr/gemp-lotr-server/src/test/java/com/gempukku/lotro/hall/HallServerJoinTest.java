package com.gempukku.lotro.hall;

import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Adventure;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentInfo;
import com.gempukku.lotro.tournament.TournamentParams;
import com.gempukku.lotro.tournament.TournamentQueue;
import com.gempukku.lotro.tournament.TournamentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * The join / registration contract of {@link HallServer} behind the Play and Join flows: every way of not ending up
 * where the player asked is a {@link HallException} carrying a message for the player (never a silent no-op, a
 * success-shaped string or an NPE), and the deck comes from the owner the client named (the player, or the Librarian
 * for a Deck Library deck).
 */
public class HallServerJoinTest {
    private LotroServer _lotroServer;
    private TournamentService _tournamentService;
    private LotroFormatLibrary _formatLibrary;
    private LotroFormat _format;
    private HallServer _hall;

    private final Player _alice = player(1, "alice");
    private final Player _librarian = player(2, "Librarian");

    @Before
    public void setUp() {
        _lotroServer = Mockito.mock(LotroServer.class);
        Mockito.when(_lotroServer.createNewGame(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mockito.mock(LotroGameMediator.class));
        _tournamentService = Mockito.mock(TournamentService.class);
        _formatLibrary = Mockito.mock(LotroFormatLibrary.class);

        // A format that accepts any deck as it is.
        _format = Mockito.mock(LotroFormat.class);
        Mockito.when(_format.getName()).thenReturn("Test Format");
        Mockito.when(_format.getAdventure()).thenReturn(Mockito.mock(Adventure.class));
        Mockito.when(_format.applyErrata(Mockito.any(LotroDeck.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(_format.validateDeckForHall(Mockito.any(LotroDeck.class), Mockito.any())).thenReturn("");
        Mockito.when(_formatLibrary.getFormat("test")).thenReturn(_format);
        Mockito.when(_formatLibrary.getHallFormats()).thenReturn(Map.of("test", _format));

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.getPlayerCollection(Mockito.any(Player.class), Mockito.anyString()))
                .thenReturn(Mockito.mock(CardCollection.class));

        ChatServer chatServer = Mockito.mock(ChatServer.class);
        Mockito.when(chatServer.createChatRoom(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyInt(),
                Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(Mockito.mock(ChatRoomMediator.class));

        _hall = new HallServer(Mockito.mock(IgnoreDAO.class), _lotroServer, chatServer, Mockito.mock(LeagueService.class),
                _tournamentService, Mockito.mock(LotroCardBlueprintLibrary.class), _formatLibrary, collectionsManager,
                Mockito.mock(AdminService.class));
    }

    private static Player player(int id, String name) {
        return new Player(id, name, "pass", "u", null, null, null, null, false);
    }

    private void owns(Player owner, String deckName) {
        LotroDeck deck = new LotroDeck(deckName);
        deck.setTargetFormat("Test Format");
        Mockito.when(_lotroServer.getParticipantDeck(owner, deckName)).thenReturn(deck);
    }

    private static String refusal(ThrowingRunnable call) {
        try {
            call.run();
        } catch (HallException expected) {
            assertNotNull(expected.getMessage());
            return expected.getMessage();
        } catch (Exception other) {
            throw new AssertionError("expected a HallException, got " + other, other);
        }
        fail("expected a HallException");
        return null;
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    // ---- table descriptions (B1) ----

    @Test
    public void descriptionsLoseMarkupAndControlCharactersAndAreCapped() {
        assertEquals("", HallServer.sanitizeTableDescription(null));
        assertEquals("img src=x onerror=alert(1)", HallServer.sanitizeTableDescription("<img src=x onerror=alert(1)>"));
        assertEquals("line oneline two", HallServer.sanitizeTableDescription("line one\nline two\u0007"));
        assertEquals("a & b \"quoted\" 'x'", HallServer.sanitizeTableDescription("  a & b \"quoted\" 'x'  "));
        assertEquals("Frodo-B", HallServer.sanitizeTableDescription("Frodo-B"));

        String longText = "x".repeat(150);
        assertEquals(HallServer.MAX_TABLE_DESCRIPTION_LENGTH, HallServer.sanitizeTableDescription(longText).length());
        // a surrogate pair is never cut in half
        String emoji = "😀".repeat(120);
        String capped = HallServer.sanitizeTableDescription(emoji);
        assertEquals(HallServer.MAX_TABLE_DESCRIPTION_LENGTH, capped.codePointCount(0, capped.length()));
    }

    @Test
    public void aCreatedTableStoresTheSanitizedDescription() throws Exception {
        owns(_alice, "Mine");
        _hall.createNewTable("test", _alice, "Mine", "default", "<b>hi</b>" + "y".repeat(200), false, false, false);

        var tables = waitingTables();
        assertEquals(1, tables.size());
        String stored = tables.values().iterator().next().getGameSettings().userDescription();
        assertFalse(stored.contains("<"));
        assertFalse(stored.contains(">"));
        assertTrue(stored.startsWith("bhi/b"));
        assertEquals(HallServer.MAX_TABLE_DESCRIPTION_LENGTH, stored.length());
    }

    // ---- deck owner (library decks, F1) ----

    @Test
    public void aLibraryDeckIsTakenFromTheLibrarian() throws Exception {
        owns(_librarian, "FotR Starter");
        _hall.createNewTable("test", _alice, _librarian, "FotR Starter", "default", "", false, false, false);

        Mockito.verify(_lotroServer).getParticipantDeck(_librarian, "FotR Starter");
        Mockito.verify(_lotroServer, Mockito.never()).getParticipantDeck(Mockito.eq(_alice), Mockito.anyString());
    }

    @Test
    public void joiningATableWithALibraryDeckSeatsThePlayer() throws Exception {
        Player bob = player(3, "bob");
        owns(bob, "Bob's");
        _hall.createNewTable("test", bob, "Bob's", "default", "", false, false, false);
        owns(_librarian, "FotR Starter");

        var tables = waitingTables();
        assertEquals(1, tables.size());
        String tableId = tables.keySet().iterator().next();

        assertTrue(_hall.joinTableAsPlayer(tableId, _alice, _librarian, "FotR Starter"));
        assertTrue("the table started, so it is no longer waiting", waitingTables().isEmpty());
        Mockito.verify(_lotroServer).getParticipantDeck(_librarian, "FotR Starter");
    }

    @Test
    public void aMissingDeckIsARefusalNamingTheProblem() {
        String message = refusal(() -> _hall.createNewTable("test", _alice, "Nope", "default", "", false, false, false));
        assertTrue(message, message.contains("You don't have a deck registered yet"));
    }

    // ---- queues (F4.1) ----

    @Test
    public void aQueueRefusalReachesTheCaller() throws Exception {
        TournamentQueue queue = Mockito.mock(TournamentQueue.class);
        Mockito.when(queue.isRequiresDeck()).thenReturn(false);
        Mockito.doThrow(new HallException("Daily is full (8 players).")).when(queue).joinPlayer(Mockito.eq(_alice), Mockito.any());
        Mockito.when(_tournamentService.getTournamentQueue("q1")).thenReturn(queue);

        String message = refusal(() -> _hall.joinQueue("q1", _alice, null));
        assertEquals("Daily is full (8 players).", message);
    }

    @Test
    public void aVanishedQueueOrASecondJoinIsRefused() throws Exception {
        assertTrue(refusal(() -> _hall.joinQueue("gone", _alice, null)).contains("finished accepting players"));

        TournamentQueue queue = Mockito.mock(TournamentQueue.class);
        Mockito.when(queue.isPlayerSignedUp("alice")).thenReturn(true);
        Mockito.when(_tournamentService.getTournamentQueue("q1")).thenReturn(queue);
        assertEquals("You have already joined that queue", refusal(() -> _hall.joinQueue("q1", _alice, null)));
        Mockito.verify(queue, Mockito.never()).joinPlayer(Mockito.any(), Mockito.any());
    }

    @Test
    public void aConstructedQueueJoinValidatesTheNamedOwnersDeck() throws Exception {
        TournamentQueue queue = Mockito.mock(TournamentQueue.class);
        Mockito.when(queue.isRequiresDeck()).thenReturn(true);
        Mockito.when(queue.getFormatCode()).thenReturn("test");
        Mockito.when(queue.getCollectionType()).thenReturn(CollectionType.ALL_CARDS);
        Mockito.when(_tournamentService.getTournamentQueue("q1")).thenReturn(queue);
        owns(_librarian, "FotR Starter");

        assertTrue(_hall.joinQueue("q1", _alice, _librarian, "FotR Starter"));
        Mockito.verify(queue).joinPlayer(Mockito.eq(_alice), Mockito.notNull());
        Mockito.verify(_lotroServer).getParticipantDeck(_librarian, "FotR Starter");
    }

    // ---- limited deck registration (F4.3 / F4.4) ----

    @Test
    public void registeringForAnUnknownTournamentIsARefusalNotAnNpe() {
        String message = refusal(() -> _hall.registerLimitedTournamentDeck("missing", _alice, "Mine"));
        assertTrue(message, message.contains("not found"));
        assertFalse(message, message.contains("registration has already closed"));
    }

    @Test
    public void aDeckTheTournamentDoesNotTakeIsARefusal() throws Exception {
        Tournament tournament = tournament("Sealed Cup");
        Mockito.when(tournament.playerSubmittedDeck(Mockito.eq("alice"), Mockito.any())).thenReturn(false);
        owns(_alice, "Mine");

        String message = refusal(() -> _hall.registerLimitedTournamentDeck("t1", _alice, "Mine"));
        assertTrue(message, message.startsWith("Could not register your deck with tournament 'Sealed Cup'"));
    }

    @Test
    public void anInvalidDeckIsARefusalWithTheValidationReason() throws Exception {
        tournament("Sealed Cup");
        owns(_alice, "Mine");
        Mockito.when(_format.validateDeckForHall(Mockito.any(LotroDeck.class), Mockito.any())).thenReturn("Deck too small");

        String message = refusal(() -> _hall.registerLimitedTournamentDeck("t1", _alice, "Mine"));
        assertEquals("Your selected deck is not valid for this format: Deck too small", message);
    }

    @Test
    public void aRegisteredDeckReturnsTheSuccessMessage() throws Exception {
        Tournament tournament = tournament("Sealed Cup");
        Mockito.when(tournament.playerSubmittedDeck(Mockito.eq("alice"), Mockito.any())).thenReturn(true);
        owns(_alice, "Mine");

        String message = _hall.registerLimitedTournamentDeck("t1", _alice, "Mine");
        assertTrue(message, message.startsWith("Registered deck 'Mine' with tournament 'Sealed Cup' successfully."));
    }

    // ---- late join (F4.3) ----

    @Test
    public void lateJoinRefusalsAreExceptions() throws Exception {
        assertEquals("That tournament is already over.", refusal(() -> _hall.joinTournamentLate("missing", _alice, "Mine")));

        Tournament closed = tournament("Closed Cup");
        Mockito.when(closed.isJoinable()).thenReturn(false);
        assertEquals("That tournament does not allow late joining.", refusal(() -> _hall.joinTournamentLate("t1", _alice, "Mine")));

        Mockito.when(closed.isJoinable()).thenReturn(true);
        closed.getInfo().Parameters().requiresDeck = false;
        Mockito.when(_tournamentService.joinTournamentLate("t1", "alice", null)).thenReturn(false);
        assertTrue(refusal(() -> _hall.joinTournamentLate("t1", _alice, "Mine")).startsWith("Joining tournament 'Closed Cup' failed."));
    }

    @Test
    public void aLateJoinReturnsThePlainSuccessMessage() throws Exception {
        Tournament open = tournament("Open Cup");
        Mockito.when(open.isJoinable()).thenReturn(true);
        open.getInfo().Parameters().requiresDeck = true;
        owns(_librarian, "FotR Starter");
        Mockito.when(_tournamentService.joinTournamentLate(Mockito.eq("t1"), Mockito.eq("alice"), Mockito.notNull())).thenReturn(true);

        assertEquals("Joined tournament 'Open Cup' successfully.", _hall.joinTournamentLate("t1", _alice, _librarian, "FotR Starter"));
    }

    private Tournament tournament(String name) {
        Tournament tournament = Mockito.mock(Tournament.class);
        Mockito.when(tournament.getTournamentName()).thenReturn(name);
        Mockito.when(tournament.getFormatCode()).thenReturn("test");
        Mockito.when(tournament.getCollectionType()).thenReturn(CollectionType.ALL_CARDS);
        TournamentParams params = new TournamentParams();
        params.tournamentId = "t1";
        params.name = name;
        TournamentInfo info = new TournamentInfo(null, null, null, params, "t1", null, Tournament.Stage.PLAYING_GAMES, 0);
        Mockito.when(tournament.getInfo()).thenReturn(info);
        Mockito.when(_tournamentService.getTournamentById("t1")).thenReturn(tournament);
        return tournament;
    }

    /**
     * The waiting tables, read straight from the hall's TableHolder (by reflection, so this test does not depend on
     * the hall visitor's signature, which the hall poll owns).
     */
    @SuppressWarnings("unchecked")
    // ---- invite-only tables (round 3): only the invitee, or an administrator, may take the seat ----

    private String openInviteTable(String invitee) throws Exception {
        owns(_alice, "Mine");
        _hall.createNewTable("test", _alice, "Mine", "default", invitee, true, false, false);
        var tables = waitingTables();
        assertEquals(1, tables.size());
        return tables.keySet().iterator().next();
    }

    @Test
    public void theInviteeMayJoinAnInviteOnlyTable() throws Exception {
        String tableId = openInviteTable("Bob");
        Player bob = player(3, "bob");
        owns(bob, "Bob's");

        assertTrue("the invite matches the name case-insensitively, as the invitee lookup does",
                _hall.joinTableAsPlayer(tableId, bob, "Bob's"));
        assertTrue(waitingTables().isEmpty());
    }

    @Test
    public void anyoneElseIsRefusedAnInviteOnlyTable() throws Exception {
        String tableId = openInviteTable("Bob");
        Player carol = player(4, "carol");
        owns(carol, "Carol's");
        Player jimBob = player(5, "JimBob");
        owns(jimBob, "Jim's");

        String message = refusal(() -> _hall.joinTableAsPlayer(tableId, carol, "Carol's"));
        assertEquals("This table is invite-only; only the invited player can join it.", message);
        assertTrue("never a suffix match", refusal(() -> _hall.joinTableAsPlayer(tableId, jimBob, "Jim's")).contains("invite-only"));
        assertTrue("a Deck Library deck changes nothing",
                refusal(() -> _hall.joinTableAsPlayer(tableId, carol, _librarian, "FotR Starter")).contains("invite-only"));
        assertEquals("the table still waits for Bob", 1, waitingTables().size());
        Mockito.verify(_lotroServer, Mockito.never()).getParticipantDeck(carol, "Carol's");
    }

    @Test
    public void aLibraryDeckInviteStillSeatsTheInvitee() throws Exception {
        owns(_librarian, "FotR Starter");
        _hall.createNewTable("test", _alice, _librarian, "FotR Starter", "default", "(New Player) Bob", true, false, false);
        String tableId = waitingTables().keySet().iterator().next();
        Player bob = player(3, "Bob");
        owns(bob, "Bob's");

        assertTrue(_hall.joinTableAsPlayer(tableId, bob, "Bob's"));
    }

    @Test
    public void anAdministratorMayJoinAnInviteOnlyTable() throws Exception {
        String tableId = openInviteTable("Bob");
        Player admin = new Player(6, "gandalf", "pass", "a", null, null, null, null, false);
        owns(admin, "Staff");

        assertTrue(_hall.joinTableAsPlayer(tableId, admin, "Staff"));
        assertTrue(waitingTables().isEmpty());
    }

    @Test
    public void theCreatorStillCannotTakeTheOtherSeat() throws Exception {
        String tableId = openInviteTable("Bob");
        assertEquals("You can't play against yourself", refusal(() -> _hall.joinTableAsPlayer(tableId, _alice, "Mine")));
    }

    @Test
    public void anOpenTableAdmitsAnyone() throws Exception {
        owns(_alice, "Mine");
        _hall.createNewTable("test", _alice, "Mine", "default", "Bob", false, false, false);
        String tableId = waitingTables().keySet().iterator().next();
        Player carol = player(4, "carol");
        owns(carol, "Carol's");

        assertTrue("a description that happens to be a name is not an invite", _hall.joinTableAsPlayer(tableId, carol, "Carol's"));
    }

    private Map<String, GameTable> waitingTables() throws Exception {
        java.lang.reflect.Field holderField = HallServer.class.getDeclaredField("tableHolder");
        holderField.setAccessible(true);
        Object holder = holderField.get(_hall);
        java.lang.reflect.Field awaiting = TableHolder.class.getDeclaredField("awaitingTables");
        awaiting.setAccessible(true);
        return (Map<String, GameTable>) awaiting.get(holder);
    }
}
