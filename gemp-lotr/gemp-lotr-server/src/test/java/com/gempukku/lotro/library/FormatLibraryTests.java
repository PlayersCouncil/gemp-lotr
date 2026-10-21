package com.gempukku.lotro.library;

import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultAdventureLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.SealedLeagueProduct;
import com.gempukku.lotro.league.SealedLeagueType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class FormatLibraryTests extends LoadedLibraries {

    protected static LotroFormatLibrary _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
    protected static SealedLeagueProduct _sealedLeagueProduct = new SealedLeagueProduct();

    @ParameterizedTest(name = "{0} in LotroFormatLibrary matches SealedLeagueProduct.")
    @CsvSource(value = {
            "fotr_block_sealed,fotr_block",
            "ttt_block_sealed,ttt_block",
            "ts_special_sealed,ts_special",
            "rotk_block_sealed,movie",
            "movie_special_sealed,movie_special",
            "wotr_block_sealed,war_block",
            "th_block_sealed,hunters_block",
    })
    public void SealedLeagueProductComparison(String sealedName, String formatCode) {
        var oldDef = _sealedLeagueProduct.getAllSeriesForLeague(formatCode);
        var newDef = _formatLibrary.GetSealedTemplate(sealedName);

        var format = SealedLeagueType.getLeagueType(formatCode);

        assertNotNull(oldDef);
        assertNotNull(newDef);

        assertNotNull(format);
        assertEquals(format.getFormat(), newDef.GetFormat().getCode());

        var oldList = oldDef.stream().map(CardCollection::getAll).toList();
        var newList = newDef.GetAllSeriesProducts();

        assertEquals(oldList.size(), newList.size());

        for (int i = 0; i < oldList.size(); ++i) {
            var oldItem = new ArrayList<>();
            oldList.get(i).forEach(oldItem::add);
            var newItem = newList.get(i);

            assertEquals(oldItem, newItem);
        }
    }

}