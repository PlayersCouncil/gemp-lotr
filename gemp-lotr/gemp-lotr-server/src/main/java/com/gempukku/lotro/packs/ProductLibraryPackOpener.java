package com.gempukku.lotro.packs;

import com.gempukku.lotro.game.CardCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridges the ProductLibrary (server layer) to the PackOpener interface (logic layer)
 * for use during gameplay. Opening packs via this class does NOT modify any player collection.
 */
public class ProductLibraryPackOpener implements PackOpener {

    // The 16 official booster packs, in release order
    private static final List<String> OFFICIAL_BOOSTERS = List.of(
            "FotR - Booster", "MoM - Booster", "RotEL - Booster",
            "TTT - Booster", "BoHD - Booster", "EoF - Booster",
            "RotK - Booster", "SoG - Booster", "REF - Booster",
            "MD - Booster", "SH - Booster", "BR - Booster",
            "BL - Booster", "HU - Booster", "RoS - Booster", "TaD - Booster"
    );

    private final ProductLibrary _productLibrary;

    public ProductLibraryPackOpener(ProductLibrary productLibrary) {
        _productLibrary = productLibrary;
    }

    @Override
    public List<String> getAvailablePackIds() {
        return OFFICIAL_BOOSTERS;
    }

    @Override
    public List<String> openPack(String packId) {
        PackBox pack = _productLibrary.GetProduct(packId);
        if (pack == null) {
            return List.of();
        }

        List<String> blueprintIds = new ArrayList<>();
        for (CardCollection.Item item : pack.openPack()) {
            if (item.getType() == CardCollection.Item.Type.CARD) {
                blueprintIds.add(item.getBlueprintId());
            }
            // Ignore nested packs — we only want card blueprint IDs
        }
        return blueprintIds;
    }
}
