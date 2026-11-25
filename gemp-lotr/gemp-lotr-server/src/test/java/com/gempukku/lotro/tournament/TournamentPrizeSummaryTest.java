package com.gempukku.lotro.tournament;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * The prizes of each kind of tournament as data (a label and plain-text lines, for the new hall), and the legacy HTML
 * description derived from it, which must stay what the legacy hall has always been sent.
 */
public class TournamentPrizeSummaryTest {

    // The descriptions as they were hand-written before the structured form existed.
    private static final String LEGACY_DAILY = """
            <div class='prizeHint' value='<ul>
                <li>2+ Wins: Required for Event Awards and Tengwar cards</li>
                <li>Top 4: Get Tengwar cards (up to 4 for 1st place)</li>
                <li>Top 10: Get Event Award (up to 10 for 1st place)</li>
                <li>Top 32: Receive random foils (more for higher placement)</li>
            </ul>'>Prize Breakdown</div>
            """;

    private static final String LEGACY_WIN_FOR_AWARD = """
            <div class='prizeHint' value='<ul>
                <li>4+ Players required for any prizes</li>
                <li>1 Event Award per win or bye</li>
                <li>1 Tengwar card for the tournament winner</li>
            </ul>'>Prize Breakdown</div>""";

    @Test
    public void dailyPrizesAsData() {
        PrizeSummary summary = new DailyTournamentPrizes("daily", null).getPrizeSummary();
        assertEquals("Prize Breakdown", summary.label());
        assertEquals(List.of(
                "2+ Wins: Required for Event Awards and Tengwar cards",
                "Top 4: Get Tengwar cards (up to 4 for 1st place)",
                "Top 10: Get Event Award (up to 10 for 1st place)",
                "Top 32: Receive random foils (more for higher placement)"), summary.lines());
    }

    @Test
    public void dailyLegacyHtmlIsUnchanged() {
        // The old text block ended with a newline after </div>, which is no longer sent; the legacy hall trims the
        // description (GempLotrHallUI.parsePrizes) before reading it, so that is all that differs.
        assertEquals(LEGACY_DAILY.stripTrailing(), new DailyTournamentPrizes("daily", null).getPrizeDescription());
    }

    @Test
    public void winForAwardPrizesAsData() {
        PrizeSummary summary = new WinForAwardTournamentPrizes("win-for-award", null).getPrizeSummary();
        assertEquals("Prize Breakdown", summary.label());
        assertEquals(List.of(
                "4+ Players required for any prizes",
                "1 Event Award per win or bye",
                "1 Tengwar card for the tournament winner"), summary.lines());
    }

    @Test
    public void winForAwardLegacyHtmlIsUnchanged() {
        assertEquals(LEGACY_WIN_FOR_AWARD, new WinForAwardTournamentPrizes("win-for-award", null).getPrizeDescription());
    }

    @Test
    public void noPrizesAsData() {
        PrizeSummary summary = new NoPrizes().getPrizeSummary();
        assertEquals("No prizes", summary.label());
        assertEquals(List.of(), summary.lines());
    }

    @Test
    public void noPrizesLegacyTextIsUnchanged() {
        assertEquals("No prizes", new NoPrizes().getPrizeDescription());
    }

    @Test
    public void legacyHtmlEscapesMarkupInTheText() {
        PrizeSummary summary = PrizeSummary.of("A & B", "Top <4>", "Ketura's pick");
        assertEquals("""
                <div class='prizeHint' value='<ul>
                    <li>Top &amp;lt;4&amp;gt;</li>
                    <li>Ketura&amp;#39;s pick</li>
                </ul>'>A &amp; B</div>""", summary.toLegacyHtml());
    }
}
