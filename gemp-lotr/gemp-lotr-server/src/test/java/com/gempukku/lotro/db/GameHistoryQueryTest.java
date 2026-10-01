package com.gempukku.lotro.db;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The SQL behind My Account > Game History filtering: which conditions each filter adds, that every player-supplied
 * value is bound as a parameter (never pasted into the statement), and the case-insensitive / prefix / LIKE-escaping
 * rules.
 */
public class GameHistoryQueryTest {

    private static GameHistoryFilter filter(String format, String opponent, boolean exact, String event, LocalDate from, LocalDate to) {
        return new GameHistoryFilter(format, opponent, exact, event, from, to);
    }

    @Test
    public void noFilterSelectsOnlyThePlayersOwnGames() {
        var query = GameHistoryQuery.forPlayer("alice", GameHistoryFilter.NONE);

        assertEquals("(winner = :me OR loser = :me)", query.where());
        assertEquals(Map.of("me", "alice"), query.parameters());
        assertTrue(query.selectSql().endsWith("ORDER BY end_date DESC, id DESC LIMIT :start, :count"));
        assertEquals("SELECT COUNT(*) FROM game_history WHERE (winner = :me OR loser = :me)", query.countSql());
    }

    @Test
    public void nullFilterIsTheSameAsNone() {
        assertEquals(GameHistoryQuery.forPlayer("alice", GameHistoryFilter.NONE).where(),
                GameHistoryQuery.forPlayer("alice", null).where());
    }

    @Test
    public void blankFilterValuesAreIgnored() {
        var f = filter("  ", "", false, " \t", null, null);
        assertTrue(f.isEmpty());
        assertEquals("(winner = :me OR loser = :me)", GameHistoryQuery.forPlayer("alice", f).where());
    }

    @Test
    public void formatIsAnExactMatch() {
        var query = GameHistoryQuery.forPlayer("alice", filter(" PC-Movie Block ", null, false, null, null, null));

        assertTrue(query.where().contains("format_name = :format"));
        assertEquals("PC-Movie Block", query.parameters().get("format"));
    }

    @Test
    public void opponentIsACaseInsensitivePrefixOnTheOtherSide() {
        var query = GameHistoryQuery.forPlayer("alice", filter(null, "BoB", false, null, null, null));

        assertTrue(query.where().contains(
                "((winner = :me AND LOWER(loser) LIKE :opponent ESCAPE '!') OR (loser = :me AND LOWER(winner) LIKE :opponent ESCAPE '!'))"));
        assertEquals("bob%", query.parameters().get("opponent"));
    }

    @Test
    public void opponentCanBeMatchedExactly() {
        var query = GameHistoryQuery.forPlayer("alice", filter(null, "Bob", true, null, null, null));

        assertTrue(query.where().contains("LOWER(loser) = :opponent"));
        assertFalse(query.where().contains("LIKE :opponent"));
        assertEquals("bob", query.parameters().get("opponent"));
    }

    @Test
    public void likeWildcardsInTheInputAreEscaped() {
        var query = GameHistoryQuery.forPlayer("alice", filter(null, "a_b%c!", false, "50%_off!", null, null));

        assertEquals("a!_b!%c!!%", query.parameters().get("opponent"));
        assertEquals("50!%!_off!!%", query.parameters().get("event"));
    }

    @Test
    public void casualEventSelectsGamesWithoutAnEventOrInACasualPseudoEvent() {
        for (String casual : new String[]{"casual", "Casual", " CASUAL "}) {
            var query = GameHistoryQuery.forPlayer("alice", filter(null, null, false, casual, null, null));
            assertTrue(query.where().contains("(tournament IS NULL OR tournament LIKE 'Casual %')"));
            assertFalse(query.parameters().containsKey("event"));
        }
    }

    @Test
    public void otherEventsAreACaseInsensitivePrefixOfTheLeagueOrTournamentName() {
        var query = GameHistoryQuery.forPlayer("alice", filter(null, null, false, "PC Movie League", null, null));

        assertTrue(query.where().contains("LOWER(tournament) LIKE :event ESCAPE '!'"));
        assertEquals("pc movie league%", query.parameters().get("event"));
        assertFalse(query.where().contains("tournament IS NULL"));
    }

    @Test
    public void dateRangeIsInclusiveOfBothDays() {
        var query = GameHistoryQuery.forPlayer("alice",
                filter(null, null, false, null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));

        assertTrue(query.where().contains("end_date >= :fromDate"));
        assertTrue(query.where().contains("end_date < :toDate"));
        assertEquals("2026-09-01 00:00:00", query.parameters().get("fromDate"));
        assertEquals("2026-10-01 00:00:00", query.parameters().get("toDate"));
    }

    @Test
    public void openEndedDateRanges() {
        var fromOnly = GameHistoryQuery.forPlayer("alice", filter(null, null, false, null, LocalDate.of(2026, 1, 1), null));
        assertTrue(fromOnly.where().contains("end_date >= :fromDate"));
        assertFalse(fromOnly.where().contains(":toDate"));

        var toOnly = GameHistoryQuery.forPlayer("alice", filter(null, null, false, null, null, LocalDate.of(2025, 12, 31)));
        assertFalse(toOnly.where().contains(":fromDate"));
        assertEquals("2026-01-01 00:00:00", toOnly.parameters().get("toDate"));
    }

    @Test
    public void allFiltersCombineWithAnd() {
        var query = GameHistoryQuery.forPlayer("alice",
                filter("PC-FotR Block", "bo", false, "casual", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2)));

        assertEquals("(winner = :me OR loser = :me)"
                        + " AND format_name = :format"
                        + " AND ((winner = :me AND LOWER(loser) LIKE :opponent ESCAPE '!') OR (loser = :me AND LOWER(winner) LIKE :opponent ESCAPE '!'))"
                        + " AND (tournament IS NULL OR tournament LIKE 'Casual %')"
                        + " AND end_date >= :fromDate AND end_date < :toDate",
                query.where());
        assertEquals(5, query.parameters().size());
    }

    @Test
    public void hostileInputNeverReachesTheStatementText() {
        String evil = "x' OR '1'='1; DROP TABLE game_history; --";
        var query = GameHistoryQuery.forPlayer("alice'--", filter(evil, evil, false, evil, null, null));

        for (String sql : new String[]{query.selectSql(), query.countSql()}) {
            assertFalse(sql.contains("DROP"));
            assertFalse(sql.contains("alice"));
            assertFalse(sql.contains("'1'"));
        }
        assertEquals(evil, query.parameters().get("format"));
        assertEquals("alice'--", query.parameters().get("me"));
    }

    @Test
    public void optionQueriesAreParameterised() {
        assertTrue(GameHistoryQuery.formatsSql().contains("(winner = :me OR loser = :me)"));
        assertTrue(GameHistoryQuery.formatsSql().contains("ORDER BY COUNT(*) DESC"));
        assertTrue(GameHistoryQuery.eventsSql().contains("LIMIT :limit"));
        assertTrue(GameHistoryQuery.eventsSql().contains("tournament NOT LIKE 'Casual %'"));
    }

    @Test
    public void validationRejectsReversedDatesAndOverlongText() {
        assertNull(GameHistoryFilter.NONE.validate());
        assertNotNull(filter(null, null, false, null, LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 1)).validate());
        assertNull(filter(null, null, false, null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1)).validate());
        assertNotNull(filter(null, "x".repeat(GameHistoryFilter.MAX_TEXT_LENGTH + 1), false, null, null, null).validate());
    }
}
