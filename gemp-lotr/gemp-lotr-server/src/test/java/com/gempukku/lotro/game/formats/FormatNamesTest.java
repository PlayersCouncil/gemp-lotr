package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.game.LotroFormat;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * The null-safe format-name fallback used by the tournament and league detail views, the tournament report and the
 * event browser: a retired format code resolves to null in the library, and must show as the raw code, not throw.
 */
public class FormatNamesTest {

    private static LotroFormat format(String code, String name) {
        LotroFormat format = Mockito.mock(LotroFormat.class);
        Mockito.when(format.getCode()).thenReturn(code);
        Mockito.when(format.getName()).thenReturn(name);
        return format;
    }

    @Test
    public void aKnownCodeResolvesToItsName() {
        LotroFormatLibrary library = Mockito.mock(LotroFormatLibrary.class);
        LotroFormat pcMovie = format("pc_movie", "PC-Movie");
        Mockito.when(library.getFormat("pc_movie")).thenReturn(pcMovie);

        assertEquals("PC-Movie", FormatNames.nameOrCode(library, "pc_movie"));
    }

    @Test
    public void aRetiredCodeFallsBackToTheRawCode() {
        LotroFormatLibrary library = Mockito.mock(LotroFormatLibrary.class);   // getFormat -> null, as for a retired code

        assertEquals("ancient_block", FormatNames.nameOrCode(library, "ancient_block"));
    }

    @Test
    public void aFailingLookupFallsBackToTheRawCode() {
        LotroFormatLibrary library = Mockito.mock(LotroFormatLibrary.class);
        Mockito.when(library.getFormat("x")).thenThrow(new RuntimeException("FormatLibrary.getFormat() interrupted"));

        assertEquals("x", FormatNames.nameOrCode(library, "x"));
    }

    @Test
    public void noCodeAtAllIsUnknownNeverNull() {
        LotroFormatLibrary library = Mockito.mock(LotroFormatLibrary.class);

        assertEquals(FormatNames.UNKNOWN, FormatNames.nameOrCode(library, null));
        assertEquals(FormatNames.UNKNOWN, FormatNames.nameOrCode(library, "  "));
        assertEquals(FormatNames.UNKNOWN, FormatNames.nameOrCode((LotroFormatLibrary) null, null));
        assertEquals("fotr_block", FormatNames.nameOrCode((LotroFormatLibrary) null, "fotr_block"));
    }

    @Test
    public void aResolvedFormatWinsOverTheCode() {
        assertEquals("Fellowship Block", FormatNames.nameOrCode(format("fotr_block", "Fellowship Block"), "other"));
    }

    @Test
    public void aMissingFormatFallsBackToTheCodeThenUnknown() {
        assertEquals("fotr_block", FormatNames.nameOrCode((LotroFormat) null, "fotr_block"));
        assertEquals(FormatNames.UNKNOWN, FormatNames.nameOrCode((LotroFormat) null, null));
    }

    @Test
    public void aFormatWithABlankNameFallsBackToItsCode() {
        assertEquals("fotr_block", FormatNames.nameOrCode(format("fotr_block", ""), null));
    }

    @Test
    public void codeOrPrefersTheFormatsOwnCode() {
        assertEquals("fotr_block", FormatNames.codeOr(format("fotr_block", "Fellowship Block"), "raw"));
        assertEquals("raw", FormatNames.codeOr(null, "raw"));
        assertEquals(null, FormatNames.codeOr(null, null));
    }
}
