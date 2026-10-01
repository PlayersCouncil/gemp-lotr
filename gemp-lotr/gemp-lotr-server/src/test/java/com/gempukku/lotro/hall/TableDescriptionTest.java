package com.gempukku.lotro.hall;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * The "(New Player) " table-description prefix: only for a player with no decks of their own on their account, never
 * merely because a Deck Library deck was chosen, and never on an invite-only table (whose description is the invitee).
 */
public class TableDescriptionTest {

    @Test
    public void playerWithNoDecksIsFlagged() {
        assertEquals("(New Player) anyone?", TableHolder.tableDescription("anyone?", false, false));
    }

    @Test
    public void playerWithDecksIsNotFlaggedEvenWithALibraryDeck() {
        // the handler asks only whether the player owns decks; which deck the table uses does not matter
        assertEquals("anyone?", TableHolder.tableDescription("anyone?", false, true));
    }

    @Test
    public void inviteOnlyTablesAreNeverFlagged() {
        assertEquals("Bob", TableHolder.tableDescription("Bob", true, false));
        assertEquals("Bob", TableHolder.tableDescription("Bob", true, true));
    }

    @Test
    public void emptyDescriptionGetsTheBarePrefix() {
        assertEquals("(New Player)", TableHolder.tableDescription("", false, false));
        assertEquals("(New Player)", TableHolder.tableDescription(null, false, false));
        assertEquals("", TableHolder.tableDescription("", false, true));
        assertEquals("", TableHolder.tableDescription(null, false, true));
    }

    @Test
    public void prefixIsNotDoubled() {
        assertEquals("(New Player) hi", TableHolder.tableDescription("(New Player) hi", false, false));
    }

    @Test
    public void prefixedInviteStillMatchesTheInvitee() {
        // tables from before this rule (or hand-typed) may carry the prefix; isInvitee still strips it
        GameSettings settings = new GameSettings(null, null, null, null, null, false, false, true, false,
                GameTimer.DEFAULT_TIMER, "(New Player) Bob", false);
        assertTrue(TableHolder.isInvitee(settings, "bob"));
        GameSettings plain = new GameSettings(null, null, null, null, null, false, false, true, false,
                GameTimer.DEFAULT_TIMER, TableHolder.tableDescription("Bob", true, false), false);
        assertTrue(TableHolder.isInvitee(plain, "Bob"));
    }
}
