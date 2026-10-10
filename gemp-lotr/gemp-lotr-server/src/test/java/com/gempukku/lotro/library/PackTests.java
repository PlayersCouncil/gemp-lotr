package com.gempukku.lotro.library;

import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.packs.ProductLibrary;
import org.junit.Test;


public class PackTests extends LoadedLibraries {

    protected static ProductLibrary _productLib = new ProductLibrary(_cardLibrary);

    @Test
    public void Test1()  {
        _productLib.GetProduct("Random Rare Foil");
    }
    //protected static ProductLibrary _productLib = new ProductLibrary(_cardLibrary);
}
