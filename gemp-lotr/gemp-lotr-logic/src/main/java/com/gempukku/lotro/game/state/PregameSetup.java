package com.gempukku.lotro.game.state;

/**
 * Arranges the board of a game that begins with a prepared position rather than with the players building one.
 *
 * When a game has one of these (see GameState.setPregameSetup), the opening procedure runs as usual up to the point
 * where each Ring-bearer has been put on the table.  The starting fellowships, the opening draw and the mulligans are
 * then skipped; this is called instead, and the first turn begins.
 */
public interface PregameSetup {
    /**
     * Called once, with every card of both decks created and no card yet affecting the game.  Cards placed by this
     * call start affecting the game when the first turn starts, the same as cards played into a starting fellowship.
     */
    void arrangeBoard(LotroGame game);
}
