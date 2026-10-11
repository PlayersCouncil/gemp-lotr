package com.gempukku.lotro.framework;

import com.gempukku.lotro.game.DefaultAdventureLibrary;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;

/**
 * The card, format and product libraries as the server loads them, built once for a run of tests.
 *
 * The base of a test of a server class that needs the real libraries and no game: a league, a tournament, a
 * merchant, a pack, a format definition, the libraries themselves.  A test that plays a game stands on
 * VirtualTableScenario instead, which loads the same libraries for itself.  Either way the card library holds
 * the printed cards and nothing else.
 */
public abstract class LoadedLibraries {
    public static LotroCardBlueprintLibrary _cardLibrary;
    public static LotroFormatLibrary _formatLibrary;
    public static ProductLibrary _productLibrary;

    static {
        _cardLibrary = new LotroCardBlueprintLibrary();
        _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        _productLibrary = new ProductLibrary(_cardLibrary);
    }
}
