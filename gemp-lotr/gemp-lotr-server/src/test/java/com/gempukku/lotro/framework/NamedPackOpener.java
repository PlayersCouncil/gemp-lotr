package com.gempukku.lotro.framework;

import com.gempukku.lotro.packs.PackOpener;

import java.util.List;

/**
 * Test-only pack opener that offers one pack, "Test - Booster", holding exactly the cards a test named, in the
 * order it named them, every time the pack is opened.  See VirtualTableScenario.UseBoosterOf.
 */
public class NamedPackOpener implements PackOpener {

    private final List<String> _blueprintIds;

    public NamedPackOpener(List<String> blueprintIds) {
        _blueprintIds = List.copyOf(blueprintIds);
    }

    @Override
    public List<String> getAvailablePackIds() {
        return List.of("Test - Booster");
    }

    @Override
    public List<String> openPack(String packId) {
        return _blueprintIds;
    }
}
