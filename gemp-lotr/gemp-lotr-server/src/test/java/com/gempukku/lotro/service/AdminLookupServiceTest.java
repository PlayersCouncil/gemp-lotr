package com.gempukku.lotro.service;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.competitive.BestOfOneStandingsProducer;
import com.gempukku.lotro.competitive.ModifiedMedianStandingsProducer;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.AdminLookupService.EventEntry;
import com.gempukku.lotro.service.AdminLookupService.EventKind;
import com.gempukku.lotro.service.AdminLookupService.EventParticipant;
import com.gempukku.lotro.service.AdminLookupService.ItemEntry;
import com.gempukku.lotro.service.AdminLookupService.ItemKind;
import com.gempukku.lotro.tournament.TournamentMatch;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * The matching and ranking behind the admin add-items form: synthetic entries for the ranking rules, the real card
 * and product libraries for "does the admin find what they are looking for".
 */
public class AdminLookupServiceTest extends AbstractAtTest {

    private static ItemEntry card(String id, String title, String subtitle) {
        return new ItemEntry(id, ItemKind.CARD, title, subtitle, null, false);
    }

    private static ItemEntry alt(String id, String title) {
        return new ItemEntry(id, ItemKind.CARD, title, null, "alternate", true);
    }

    private static ItemEntry product(String name, ItemKind kind) {
        return new ItemEntry(name, kind, name, null, null, false);
    }

    private static List<String> values(List<ItemEntry> entries) {
        List<String> result = new ArrayList<>();
        for (ItemEntry e : entries)
            result.add(e.value());
        return result;
    }

    private static final List<ItemEntry> SYNTHETIC = List.of(
            card("1_1", "The One Ring", "The Ruling Ring"),
            card("1_10", "Ring of Barahir", null),
            card("1_2", "Gandalf", "The Grey Wizard"),
            card("1_100", "Úlairë Nertëa", "Messenger of Dol Guldur"),
            card("1_217", "Nazgûl Sword", null),
            card("6_1", "Between Nazgûl and Prey", null),
            card("2_85", "Witch-king", "Lord of the Nazgûl"),
            card("3_1", "Gorbag's Sword", null),
            alt("0_1", "Gandalf"),
            product("Random PC Full Art", ItemKind.AWARD),
            product("(S)FotR - Tengwar", ItemKind.SELECTION),
            product("FotR - Booster", ItemKind.PACK),
            product("Ring Pack", ItemKind.PACK)
    );

    // ---- normalisation ----

    @Test
    public void normalizeStripsAccentsCaseAndPunctuation() {
        assertEquals("nazgul", AdminLookupService.normalize("Nazgûl"));
        assertEquals("ulaire nertea", AdminLookupService.normalize("Úlairë Nertëa"));
        assertEquals("witch king", AdminLookupService.normalize("Witch-king"));
        assertEquals("witchking", AdminLookupService.compact("Witch-king"));
        assertEquals("gorbags sword", AdminLookupService.normalize("Gorbag’s Sword"));
        assertEquals("s fotr tengwar", AdminLookupService.normalize("(S)FotR - Tengwar"));
        assertEquals("", AdminLookupService.normalize(null));
    }

    // ---- ranking ----

    @Test
    public void accentInsensitiveTitleMatch() {
        var hits = values(AdminLookupService.rankItems("nazgul", SYNTHETIC, 10));
        assertTrue(hits.contains("1_217"));
        assertTrue(hits.contains("6_1"));
        assertTrue("subtitle matches too", hits.contains("2_85"));
        assertEquals("title prefix before later word before subtitle", List.of("1_217", "6_1", "2_85"), hits);

        assertEquals(List.of("1_100"), values(AdminLookupService.rankItems("ulaire", SYNTHETIC, 10)));
        assertEquals("typing the accent works as well", List.of("1_217", "6_1", "2_85"),
                values(AdminLookupService.rankItems("Nazgûl", SYNTHETIC, 10)));
    }

    @Test
    public void punctuationAndSpacingDoNotMatter() {
        assertEquals("2_85", AdminLookupService.rankItems("witchking", SYNTHETIC, 10).getFirst().value());
        assertEquals("2_85", AdminLookupService.rankItems("witch king", SYNTHETIC, 10).getFirst().value());
        assertEquals("3_1", AdminLookupService.rankItems("gorbags", SYNTHETIC, 10).getFirst().value());
    }

    @Test
    public void blueprintIdMatch() {
        var hits = values(AdminLookupService.rankItems("1_1", SYNTHETIC, 10));
        assertEquals("exact id first, then ids starting with it in set/number order", List.of("1_1", "1_10", "1_100"), hits);
        assertEquals("a foil star is ignored for matching", "1_1", AdminLookupService.rankItems("1_1*", SYNTHETIC, 10).getFirst().value());
        assertEquals("id query never matches titles or products", List.of("6_1"), values(AdminLookupService.rankItems("6_1", SYNTHETIC, 10)));
        assertEquals(List.of("0_1"), values(AdminLookupService.rankItems("0_1", SYNTHETIC, 10)));
    }

    @Test
    public void rankingOrder() {
        // "ring": exact title? no. Prefix: "Ring of Barahir", "Ring Pack". Later word: "The One Ring". Subtitle: "The Ruling Ring" (1_1 already matched)
        var hits = values(AdminLookupService.rankItems("ring", SYNTHETIC, 10));
        assertEquals(List.of("Ring Pack", "1_10", "1_1"), hits);

        // exact title beats everything; the base card beats its alternate id
        hits = values(AdminLookupService.rankItems("gandalf", SYNTHETIC, 10));
        assertEquals(List.of("1_2", "0_1"), hits);

        // substring inside a word ranks after prefix / word-prefix matches
        hits = values(AdminLookupService.rankItems("arahir", SYNTHETIC, 10));
        assertEquals(List.of("1_10"), hits);

        // every word somewhere in title / subtitle
        hits = values(AdminLookupService.rankItems("gandalf grey", SYNTHETIC, 10));
        assertEquals(List.of("1_2"), hits);
        assertTrue(AdminLookupService.rankItems("gandalf balrog", SYNTHETIC, 10).isEmpty());
    }

    @Test
    public void productsAndSelectionsAreReachable() {
        var hits = AdminLookupService.rankItems("tengwar", SYNTHETIC, 10);
        assertEquals("(S)FotR - Tengwar", hits.getFirst().value());
        assertEquals(ItemKind.SELECTION, hits.getFirst().kind());

        assertEquals("(S)FotR - Tengwar", AdminLookupService.rankItems("(S)FotR - Tengwar", SYNTHETIC, 10).getFirst().value());
        assertEquals("Random PC Full Art", AdminLookupService.rankItems("random pc", SYNTHETIC, 10).getFirst().value());
        assertEquals("FotR - Booster", AdminLookupService.rankItems("fotr boost", SYNTHETIC, 10).getFirst().value());
    }

    @Test
    public void limits() {
        List<ItemEntry> many = new ArrayList<>();
        for (int i = 1; i <= 120; i++)
            many.add(card("7_" + i, "Orc Soldier " + i, null));
        assertEquals(5, AdminLookupService.rankItems("orc", many, 5).size());
        assertEquals("0 means the default", AdminLookupService.DEFAULT_LIMIT, AdminLookupService.rankItems("orc", many, 0).size());
        assertEquals("capped", AdminLookupService.MAX_LIMIT, AdminLookupService.rankItems("orc", many, 1000).size());
        assertEquals("7_1", AdminLookupService.rankItems("orc", many, 5).getFirst().value());
        assertTrue(AdminLookupService.rankItems("", many, 5).isEmpty());
        assertTrue(AdminLookupService.rankItems("   ", many, 5).isEmpty());
        assertTrue(AdminLookupService.rankItems("--", many, 5).isEmpty());
        assertTrue(AdminLookupService.rankItems(null, many, 5).isEmpty());
    }

    // ---- the real libraries ----

    @Test
    public void realLibrarySearch() {
        var service = new AdminLookupService(_cardLibrary, _productLibrary, null);

        var nazgul = service.searchItems("nazgul", 50);
        assertFalse(nazgul.isEmpty());
        assertTrue("finds Nazgûl Sword", nazgul.stream().anyMatch(e -> e.title().equals("Nazgûl Sword")));
        assertTrue("cards and products in one list", nazgul.stream().anyMatch(e -> e.kind() == ItemKind.PACK
                && e.value().equals("The Great Eye - Dwarves+Gondor/Nazgul Starter")));
        assertEquals("a card whose title starts with it ranks first", ItemKind.CARD, nazgul.getFirst().kind());

        var oneOne = service.searchItems("1_1", 5);
        assertEquals("1_1", oneOne.getFirst().value());
        assertEquals("The One Ring", oneOne.getFirst().title());
        assertEquals(ItemKind.CARD, oneOne.getFirst().kind());
        assertNotNull("set name shown for disambiguation", oneOne.getFirst().detail());

        var tengwar = service.searchItems("(S)FotR - Tengwar", 5);
        assertEquals("(S)FotR - Tengwar", tengwar.getFirst().value());
        assertEquals(ItemKind.SELECTION, tengwar.getFirst().kind());

        var fullArt = service.searchItems("random pc full art", 5);
        assertEquals("Random PC Full Art", fullArt.getFirst().value());
        assertEquals("openOnDelivery product is an award", ItemKind.AWARD, fullArt.getFirst().kind());
        assertEquals("opened on delivery", fullArt.getFirst().detail());

        var booster = service.searchItems("FotR - Booster", 5);
        assertEquals("FotR - Booster", booster.getFirst().value());
        assertEquals(ItemKind.PACK, booster.getFirst().kind());

        assertEquals(ItemKind.AWARD, service.searchItems("Random Rare Foil", 1).getFirst().kind());

        // placeholders are never offered
        assertTrue(service.searchItems("404_", 50).isEmpty());
        assertTrue(service.searchItems("Future Prize", 50).stream().noneMatch(e -> e.value().startsWith("404_")));
    }

    @Test
    public void everySearchResultIsAcceptedByTheItemCheck() {
        var service = new AdminLookupService(_cardLibrary, _productLibrary, null);
        for (String q : List.of("gandalf", "nazgul", "1_1", "tengwar", "booster", "random", "starter", "selection")) {
            for (var entry : service.searchItems(q, 50)) {
                var check = service.checkItemLine("2x" + entry.value());
                assertNull(q + " -> " + entry.value() + ": " + check.problem(), check.problem());
                assertEquals(entry.kind(), check.kind());
                assertEquals(2, check.count());
                assertEquals(entry.value(), check.value());
            }
        }
    }

    @Test
    public void itemCheck() {
        var service = new AdminLookupService(_cardLibrary, _productLibrary, null);
        var card = service.checkItemLine("3x1_1");
        assertNull(card.problem());
        assertEquals(ItemKind.CARD, card.kind());
        assertEquals(3, card.count());
        assertEquals("1_1", card.value());

        assertNull(service.checkItemLine("1x1_1*").problem());
        assertEquals(ItemKind.AWARD, service.checkItemLine("1xRandom PC Full Art").kind());
        assertEquals(ItemKind.SELECTION, service.checkItemLine("1x(S)FotR - Tengwar").kind());
        assertEquals(ItemKind.PACK, service.checkItemLine("2xFotR - Booster").kind());

        assertNotNull(service.checkItemLine("1xNo Such Pack").problem());
        assertNotNull(service.checkItemLine("1x99_9999").problem());
        assertNotNull(service.checkItemLine("0x1_1").problem());
        assertNotNull(service.checkItemLine("ax1_1").problem());
        assertNotNull("placeholders are handed out as promises", service.checkItemLine("1x404_1").problem());
    }

    // ---- players ----

    @Test
    public void playerRanking() {
        var names = List.of("xKetura", "Ketura", "ketura_alt", "Bob", "KeturaFan", "aKETURAb", "Ketura");
        assertEquals(List.of("Ketura", "KeturaFan", "ketura_alt", "xKetura", "aKETURAb"),
                AdminLookupService.rankPlayerNames("ketura", names, 10));
        assertEquals(List.of("Ketura", "KeturaFan"), AdminLookupService.rankPlayerNames("KETURA", names, 2));
        assertEquals(List.of("Bob"), AdminLookupService.rankPlayerNames("ob", names, 10));
        assertTrue(AdminLookupService.rankPlayerNames("zzz", names, 10).isEmpty());
        assertTrue(AdminLookupService.rankPlayerNames("", names, 10).isEmpty());
        assertEquals("underscores are literal", List.of("ketura_alt"), AdminLookupService.rankPlayerNames("a_a", names, 10));
    }

    @Test
    public void splitPlayerList() {
        assertEquals(List.of("a", "b", "c", "d", "e"), AdminLookupService.splitPlayerList("a\nb, c;d \r\n  e \n\n"));
        assertTrue(AdminLookupService.splitPlayerList(null).isEmpty());
        assertTrue(AdminLookupService.splitPlayerList(" \n ,").isEmpty());
    }

    @Test
    public void resolvePlayers() {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        Player ketura = new Player(1, "Ketura", "p", "u", null, null, null, null, false);
        Mockito.when(dao.getPlayer("Ketura")).thenReturn(ketura);
        Mockito.when(dao.findPlayerNames(Mockito.eq("ketura"), Mockito.anyInt())).thenReturn(List.of("Ketura", "KeturaFan"));
        Mockito.when(dao.findPlayerNames(Mockito.eq("ketur"), Mockito.anyInt())).thenReturn(List.of("Ketura", "KeturaFan"));
        Mockito.when(dao.findPlayerNames(Mockito.eq("nobody"), Mockito.anyInt())).thenReturn(List.of());

        var service = new AdminLookupService(null, null, dao);
        var result = service.resolvePlayers(List.of("Ketura", "ketura", "ketur", "nobody", " ", "KETURA"));
        assertEquals("duplicates (ignoring case) reported once", 3, result.size());

        assertEquals("Ketura", result.get(0).name());
        assertEquals("ketur", result.get(1).input());
        assertNull(result.get(1).name());
        assertEquals(List.of("Ketura", "KeturaFan"), result.get(1).suggestions());
        assertNull(result.get(2).name());
        assertTrue(result.get(2).suggestions().isEmpty());

        // a case-insensitive database miss still resolves when the search finds the exact name in another case
        var caseOnly = service.resolvePlayers(List.of("ketura"));
        assertEquals("Ketura", caseOnly.getFirst().name());
    }

    @Test
    public void searchPlayersRanksWhatTheDatabaseReturns() {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        Mockito.when(dao.findPlayerNames(Mockito.eq("ket"), Mockito.anyInt())).thenReturn(List.of("xKet", "Ketura", "Ket"));
        var service = new AdminLookupService(null, null, dao);
        assertEquals(List.of("Ket", "Ketura", "xKet"), service.searchPlayers("ket", 10));
        assertEquals(List.of("Ket"), service.searchPlayers(" ket ", 1));
        assertTrue(service.searchPlayers("  ", 10).isEmpty());
        Mockito.verify(dao, Mockito.never()).findPlayerNames(Mockito.eq(""), Mockito.anyInt());
    }

    // ---- t4-admin-load-from-event: "load participants from a recent event" ----

    private static List<String> names(List<EventParticipant> players) {
        List<String> result = new ArrayList<>();
        for (EventParticipant p : players)
            result.add(p.name());
        return result;
    }

    private static PlayerStanding standing(String name, int place, int gamesPlayed) {
        var s = new PlayerStanding(name, 0, gamesPlayed, 0, 0, 0);
        s.standing = place;
        return s;
    }

    /**
     * A league's players come out in the order of its standings (the league event page's producer, fed real match
     * results): signed-up players with 0 games last, players who share a place by name, case-insensitively.
     */
    @Test
    public void leaguePlayersLoadInStandingsOrderWithZeroGamePlayersLast() {
        var signedUp = List.of("Zed", "Carl", "amy", "Alice", "Gus", "bob", "Eve", "Fay", "dave");
        var matches = List.of(
                new LeagueMatchResult("Series 1", "Alice", "bob"),
                new LeagueMatchResult("Series 1", "Alice", "Carl"),
                new LeagueMatchResult("Series 2", "bob", "Carl"),
                new LeagueMatchResult("Series 2", "dave", "Eve"),
                new LeagueMatchResult("Series 2", "Fay", "Gus"));
        var standings = new ArrayList<>(BestOfOneStandingsProducer.produceStandings(signedUp, matches, 2, 1, Map.of()));
        Collections.reverse(standings);   // the producer returns them unsorted anyway: order must not matter

        var players = AdminLookupService.participantsInStandingsOrder(signedUp, standings, null);
        // Alice 4 pts; bob 3; dave & Fay 2 pts in 1 game (tied: by name); Carl 2 pts in 2 games;
        // Eve & Gus 1 pt (tied); amy & Zed never played (tied, last)
        assertEquals(List.of("Alice", "bob", "dave", "Fay", "Carl", "Eve", "Gus", "amy", "Zed"), names(players));
        assertEquals(new EventParticipant("dave", 3, 1, false), players.get(2));
        assertEquals(new EventParticipant("Fay", 3, 1, false), players.get(3));
        assertEquals("0-game players keep their (shared, last) place", new EventParticipant("amy", 8, 0, false), players.get(7));
        assertEquals(new EventParticipant("Zed", 8, 0, false), players.get(8));

        var body = AdminLookupService.participantsResponse("league", "1", "L", players);
        assertEquals(AdminLookupService.ORDER_STANDINGS, body.get("order"));
    }

    /**
     * A tournament's players, in the order of Tournament.getCurrentStandings (modified median, a loss worth 0): a
     * player who dropped keeps their place and is flagged; one registered who never played comes last, below players
     * who lost every game.
     */
    @Test
    public void tournamentPlayersLoadInStandingsOrderIncludingDroppedAndZeroGamePlayers() {
        var registered = Set.of("A", "B", "C", "D", "W");
        var finished = List.of(
                new TournamentMatch("A", "B", "A", 1),
                new TournamentMatch("C", "D", "C", 1),
                new TournamentMatch("A", "C", "A", 2),
                new TournamentMatch("B", "D", "B", 2));
        var standings = ModifiedMedianStandingsProducer.produceStandings(registered, finished, 1, 0, Map.of());

        var players = AdminLookupService.participantsInStandingsOrder(registered, standings, Set.of("D"));
        // A 2 wins; C and B 1-1 with the same median, C ahead on cumulative score; D 0-2 (dropped); W never played
        assertEquals(List.of("A", "C", "B", "D", "W"), names(players));
        assertTrue("dropped players are loaded, flagged", players.get(3).dropped());
        assertEquals(2, players.get(3).gamesPlayed());
        assertFalse(players.get(0).dropped());
        assertEquals(0, players.get(4).gamesPlayed());
        for (int i = 1; i < players.size(); i++)
            assertTrue("standings order", players.get(i - 1).standing() <= players.get(i).standing());

        // a bye counts as a game played, as the standings count it: a bye-only player is not a 0-game player
        var withBye = ModifiedMedianStandingsProducer.produceStandings(Set.of("A", "B", "Y"),
                List.of(new TournamentMatch("A", "B", "A", 1)), 1, 0, Map.of("Y", 1));
        var byePlayers = AdminLookupService.participantsInStandingsOrder(Set.of("A", "B", "Y"), withBye, null);
        assertEquals(1, byePlayers.get(names(byePlayers).indexOf("Y")).gamesPlayed());
    }

    /**
     * A World Championship bookkeeping tournament (or a league that has not started) where nobody has played:
     * everyone shares first place, so they load alphabetically, and the response says the order is by name.
     */
    @Test
    public void eventWithNoGamesLoadsEveryoneAlphabetically() {
        var registered = Set.of("frodo", "Bilbo", "sam", "Aragorn");
        var standings = ModifiedMedianStandingsProducer.produceStandings(registered, List.of(), 1, 0, Map.of());
        var players = AdminLookupService.participantsInStandingsOrder(registered, standings, Set.of());
        assertEquals(List.of("Aragorn", "Bilbo", "frodo", "sam"), names(players));
        assertEquals("all four loaded, none filtered for having no games", 4, players.size());
        assertEquals(AdminLookupService.ORDER_NAME,
                AdminLookupService.participantsResponse("tournament", "wc", "WC", players).get("order"));

        var leagueStandings = BestOfOneStandingsProducer.produceStandings(List.of("zoe", "Ann", "mia"), List.of(), 2, 1, Map.of());
        assertEquals(List.of("Ann", "mia", "zoe"),
                names(AdminLookupService.participantsInStandingsOrder(List.of("zoe", "Ann", "mia"), leagueStandings, null)));
    }

    /**
     * No standings at all (a scheduled tournament's queue): by name, no place.  The sign-ups come from the queue's
     * own list, which a "competitive" queue does not hide from this (its getPlayerList() is "Competitive, player
     * count: N", which must not turn into players named "Competitive", "player", "count:" and "3").
     */
    @Test
    public void queueSignUpsWithoutStandingsLoadByName() throws Exception {
        var params = new com.gempukku.lotro.tournament.TournamentParams();
        params.tournamentId = "wc-q";
        params.name = "WC Competitive Qualifier";
        params.format = "pc_fotr_block";
        params.type = com.gempukku.lotro.tournament.Tournament.TournamentType.CONSTRUCTED;
        params.startTime = com.gempukku.lotro.common.DateUtils.Now().plusMinutes(30).toLocalDateTime().withSecond(0).withNano(0);
        params.playoff = com.gempukku.lotro.tournament.Tournament.PairingType.SWISS;
        params.prizes = com.gempukku.lotro.tournament.Tournament.PrizeType.NONE;
        params.minimumPlayers = 2;
        params.maximumPlayers = -1;
        var info = new com.gempukku.lotro.tournament.TournamentInfo(null, null, null, params, params.tournamentId,
                com.gempukku.lotro.common.DateUtils.ParseDate(params.startTime), params.getInitialStage(), 0);
        var queue = new com.gempukku.lotro.tournament.ScheduledTournamentQueue(
                Mockito.mock(com.gempukku.lotro.tournament.TournamentService.class), "wc-q", params.name, info, null,
                Mockito.mock(com.gempukku.lotro.collection.CollectionsManager.class));
        for (String name : List.of("dave-x", "bob", "Carl_2")) {
            var player = Mockito.mock(Player.class);
            Mockito.when(player.getName()).thenReturn(name);
            queue.joinPlayer(player, new com.gempukku.lotro.logic.vo.LotroDeck(name));
        }
        assertEquals("the list players see is hidden", "Competitive, player count: 3", queue.getPlayerList());
        assertEquals("sign-up order", List.of("dave-x", "bob", "Carl_2"), queue.getSignedUpPlayers());

        var players = AdminLookupService.participantsInStandingsOrder(queue.getSignedUpPlayers(), null, null);
        assertEquals(List.of("bob", "Carl_2", "dave-x"), names(players));
        assertNull(players.get(0).standing());
        assertEquals(0, players.get(0).gamesPlayed());

        assertTrue(AdminLookupService.participantsInStandingsOrder(null, null, null).isEmpty());
    }

    /**
     * Tie-breaks: within a shared place, anyone who has played before anyone who has not (whatever the names); each
     * player once when they are both a participant and in the standings; participants missing from the standings
     * after everyone placed.
     */
    @Test
    public void participantOrderTieBreaks() {
        var standings = List.of(
                standing("aaron", 5, 0),
                standing("Zack", 5, 2),
                standing("Mia", 1, 3),
                standing("mia2", 5, 2));
        var players = AdminLookupService.participantsInStandingsOrder(
                List.of("Mia", "Zack", "late", "aaron", "Mia"), standings, null);
        assertEquals(List.of("Mia", "mia2", "Zack", "aaron", "late"), names(players));
        assertNull("signed up but not in the standings: no place, last", players.get(4).standing());
        assertEquals("case-sensitive names that differ only by case both kept, stable",
                List.of("Bob", "bob"),
                names(AdminLookupService.participantsInStandingsOrder(List.of("bob", "Bob"), null, null)));
    }

    /**
     * The not-started rule of round 3 re-evaluated: a league that has not started is "upcoming" (listed when someone
     * has signed up), e.g. "Dash Bash - Movie Block (2026-10-01 – 2026-10-21)" on 2026-09-25.
     */
    @Test
    public void leagueThatHasNotStartedIsUpcomingNotRunning() {
        var now = ZonedDateTime.of(2026, 9, 25, 12, 0, 0, 0, ZoneOffset.UTC);
        var dashBashStart = ZonedDateTime.of(2026, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        var dashBashEnd = ZonedDateTime.of(2026, 10, 21, 23, 59, 59, 0, ZoneOffset.UTC);
        assertEquals(AdminLookupService.EVENT_UPCOMING, AdminLookupService.eventTiming(dashBashStart, dashBashEnd, now));

        assertEquals(AdminLookupService.EVENT_RUNNING, AdminLookupService.eventTiming(now.minusDays(3), now.plusDays(10), now));
        assertEquals("started today", AdminLookupService.EVENT_RUNNING, AdminLookupService.eventTiming(now, now.plusDays(10), now));
        assertEquals("series over, league row not yet expired", AdminLookupService.EVENT_FINISHED,
                AdminLookupService.eventTiming(now.minusDays(20), now.minusDays(1), now));
        assertEquals("no dates (unparseable definition): still offered", AdminLookupService.EVENT_RUNNING,
                AdminLookupService.eventTiming(null, null, now));
    }

    /** The exact body the add-items form receives, so the client harness can be fed the real shape. */
    @Test
    public void participantsResponseIsWhatTheFormReads() {
        var body = com.gempukku.util.JsonUtils.SerializeWithNulls(AdminLookupService.participantsResponse(
                "tournament", "wc2026", "WC 2026", List.of(
                        new EventParticipant("Alice", 1, 3, false),
                        new EventParticipant("bob", 2, 3, true),
                        new EventParticipant("Carl", 3, 0, false))));
        assertEquals("{\"kind\":\"tournament\",\"id\":\"wc2026\",\"name\":\"WC 2026\",\"order\":\"standings\",\"players\":["
                + "{\"name\":\"Alice\",\"standing\":1,\"gamesPlayed\":3,\"dropped\":false},"
                + "{\"name\":\"bob\",\"standing\":2,\"gamesPlayed\":3,\"dropped\":true},"
                + "{\"name\":\"Carl\",\"standing\":3,\"gamesPlayed\":0,\"dropped\":false}]}", body);

        assertEquals("{\"kind\":\"league\",\"id\":\"1\",\"name\":null,\"order\":\"name\",\"players\":"
                + "[{\"name\":\"Ann\",\"standing\":null,\"gamesPlayed\":0,\"dropped\":false}]}",
                com.gempukku.util.JsonUtils.SerializeWithNulls(AdminLookupService.participantsResponse("league", "1", null,
                        List.of(new EventParticipant("Ann", null, 0, false)))));
        assertEquals("{\"kind\":\"tournament\",\"id\":\"t1\",\"name\":null,\"order\":\"name\",\"players\":[]}",
                com.gempukku.util.JsonUtils.SerializeWithNulls(AdminLookupService.participantsResponse("tournament", "t1", null, null)));
    }

    @Test
    public void eventOrderPutsRunningEventsFirstThenNewestStart() {
        var runningOld = new EventEntry(EventKind.LEAGUE, "1", "Running, started long ago", "2025-01-01T00:00", null, "running", true, 5);
        var runningNoDate = new EventEntry(EventKind.LEAGUE, "2", "Running, no dates", null, null, "running", true, 1);
        var finishedNew = new EventEntry(EventKind.TOURNAMENT, "3", "Finished recently", "2026-09-20T00:00", null, "finished", false, 8);
        var finishedOld = new EventEntry(EventKind.TOURNAMENT, "4", "Finished a while ago", "2026-08-01T00:00", null, "finished", false, 2);
        var finishedNoDate = new EventEntry(EventKind.LEAGUE, "5", "Finished, no start recorded", null, null, "finished", false, 1);

        var upcoming = new EventEntry(EventKind.LEAGUE, "6", "Starts next week, 4 signed up", "2026-10-01T00:00", "2026-10-21T00:00", "upcoming", false, 4);

        var list = new ArrayList<>(List.of(finishedOld, runningNoDate, upcoming, finishedNoDate, finishedNew, runningOld));
        list.sort(AdminLookupService.EVENT_ORDER);

        assertEquals("running events first, then (newest start first) upcoming before finished",
                List.of(runningOld, runningNoDate, upcoming, finishedNew, finishedOld, finishedNoDate), list);
    }
}
