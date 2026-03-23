package com.gempukku.lotro.framework;

import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.packs.ProductLibraryPackOpener;

import java.util.List;

/**
 * Test-only pack opener that restricts available packs to just the deterministic
 * "Test - Booster" pack, ensuring unit tests get predictable card contents.
 */
public class TestPackOpener extends ProductLibraryPackOpener {

    public TestPackOpener(ProductLibrary productLibrary) {
        super(productLibrary);
    }

    @Override
    public List<String> getAvailablePackIds() {
        return List.of("Test - Booster");
    }
}
