package com.gempukku.lotro.game.state;

/**
 * Base class for extra per-game information that varies by league or game type.
 * Subclass this for league-specific data (e.g., RTMDGameInfo for Race to Mount Doom).
 * Consumers should use instanceof checks to access subclass-specific data.
 */
public class GameExtraInfo {
}
