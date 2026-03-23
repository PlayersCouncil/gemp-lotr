package com.gempukku.lotro.packs;

import java.util.List;

/**
 * Interface for opening booster packs during gameplay.
 * Implemented by the server layer (ProductLibraryPackOpener) to bridge
 * the pack system into the game logic layer without a direct dependency.
 */
public interface PackOpener {
    /**
     * Returns the IDs of all available booster packs (e.g. "FotR - Booster").
     */
    List<String> getAvailablePackIds();

    /**
     * Opens a pack and returns the card blueprint IDs it contains.
     * Does NOT modify any player collection — cards are ephemeral.
     *
     * @param packId one of the IDs returned by getAvailablePackIds()
     * @return list of card blueprint IDs (e.g. "1_89", "2_51", ...)
     */
    List<String> openPack(String packId);
}
