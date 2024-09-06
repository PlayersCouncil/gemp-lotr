package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.DateUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConstructedLeagueTest extends AbstractAtTest {
    @Test
    public void testParameters() {
        //20240726,default,0.7,1,1,pc_movie,3,6
        var leagueData = ConstructedLeague.fromRawParameters(_productLibrary, _formatLibrary, "20120312,default,0.7,1,3,fotr1_block,7,3,fotr2_block,7,3,fotr_block,7,3");
        final List<LeagueSerieInfo> series = leagueData.getSeries();
        assertEquals(3, series.size());
        assertEquals(DateUtils.DateOf(2012, 3, 12), series.getFirst().getStart());
        assertEquals(DateUtils.DateOf(2012, 3, 18), series.getFirst().getEnd());
        assertEquals("fotr1_block", series.get(0).getFormat().getCode());
        assertEquals(DateUtils.DateOf(2012, 3, 19), series.get(1).getStart());
        assertEquals(DateUtils.DateOf(2012, 3, 25), series.get(1).getEnd());
        assertEquals("fotr2_block", series.get(1).getFormat().getCode());
        assertEquals(DateUtils.DateOf(2012, 3, 26), series.get(2).getStart());
        assertEquals(DateUtils.DateOf(2012, 4, 1), series.get(2).getEnd());
        assertEquals("fotr_block", series.get(2).getFormat().getCode());
    }
}
