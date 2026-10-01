package com.gempukku.lotro.league;

import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.FormatNames;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * A finished league whose format has since been retired must still build, so that the league detail view (which
 * the event browser now opens for leagues of any age) can show it: the serie then has no format, and keeps the code
 * it was defined with for display.
 */
public class RetiredFormatLeagueSerieTest {

    private final ProductLibrary _productLibrary = Mockito.mock(ProductLibrary.class);
    /** Knows no format at all, as for codes that have been retired. */
    private final LotroFormatLibrary _formatLibrary = Mockito.mock(LotroFormatLibrary.class);

    @Test
    public void aLegacyConstructedLeagueInARetiredFormatKeepsItsCode() {
        var series = ConstructedLeague.fromRawParameters(_productLibrary, _formatLibrary,
                "20120312,default,0.7,1,2,ancient_block,7,3,older_block,7,3").getSeries();

        assertEquals(2, series.size());
        assertNull(series.get(0).getFormat());
        assertEquals("ancient_block", series.get(0).getFormatCode());
        assertEquals("older_block", series.get(1).getFormatCode());
        assertEquals("ancient_block", FormatNames.nameOrCode(series.get(0).getFormat(), series.get(0).getFormatCode()));
    }

    @Test
    public void aJsonConstructedLeagueInARetiredFormatKeepsItsCode() {
        var params = new LeagueParams();
        params.name = "Old";
        params.code = 1;
        params.start = java.time.LocalDateTime.of(2012, 3, 12, 0, 0);
        params.series.add(new LeagueParams.SerieData("ancient_block", 7, 3));

        var series = new ConstructedLeague(_productLibrary, _formatLibrary, params).getSeries();

        assertNull(series.getFirst().getFormat());
        assertEquals("ancient_block", series.getFirst().getFormatCode());
    }

    @Test
    public void aKnownFormatStillReportsItsOwnCode() {
        LotroFormat format = Mockito.mock(LotroFormat.class);
        Mockito.when(format.getCode()).thenReturn("fotr_block");
        Mockito.when(format.getName()).thenReturn("Fellowship Block");
        Mockito.when(_formatLibrary.getFormat("fotr_block")).thenReturn(format);

        var serie = ConstructedLeague.fromRawParameters(_productLibrary, _formatLibrary,
                "20120312,default,0.7,1,1,fotr_block,7,3").getSeries().getFirst();

        assertEquals("fotr_block", serie.getFormatCode());
        assertEquals("Fellowship Block", FormatNames.nameOrCode(serie.getFormat(), serie.getFormatCode()));
    }

    @Test
    public void aSoloDraftLeagueWhoseDraftWasRemovedStillBuilds() {
        // Before: NullPointerException on _draft.getFormat(), so getLeagueData failed and the detail view 500'd.
        SoloDraftDefinitions drafts = Mockito.mock(SoloDraftDefinitions.class);

        var serie = SoloDraftLeague.fromRawParameters(_productLibrary, _formatLibrary, drafts,
                "gone_draft,20240807,12,10,1722989352076,Draft - Gone").getSeries().getFirst();

        assertNull(serie.getFormat());
        assertEquals("gone_draft", serie.getFormatCode());
    }

    @Test
    public void aSoloDraftLeagueWhoseFormatWasRetiredKeepsTheDraftsFormatCode() {
        SoloDraftDefinitions drafts = Mockito.mock(SoloDraftDefinitions.class);
        SoloDraft draft = Mockito.mock(SoloDraft.class);
        Mockito.when(draft.getFormat()).thenReturn("ttt_block_old");
        Mockito.when(drafts.getSoloDraft("ttt_draft")).thenReturn(draft);

        var serie = SoloDraftLeague.fromRawParameters(_productLibrary, _formatLibrary, drafts,
                "ttt_draft,20240807,12,10,1722989352076,Draft - Towers Block").getSeries().getFirst();

        assertNull(serie.getFormat());
        assertEquals("ttt_block_old", serie.getFormatCode());
    }
}
