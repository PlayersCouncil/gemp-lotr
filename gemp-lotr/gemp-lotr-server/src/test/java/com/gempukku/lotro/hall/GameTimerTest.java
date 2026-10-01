package com.gempukku.lotro.hall;

import org.junit.Test;

import static org.junit.Assert.*;

/** The Game Timer (i) text of the Play popup (GameTimer.describeLimits, sent as HallTimers[].description). */
public class GameTimerTest {
    @Test
    public void limitsSentence() {
        assertEquals("Each player has a total time bank of 45 minutes, and will time out with a loss if they run out of "
                + "their time bank or take longer than 6 minutes between actions.", GameTimer.DEFAULT_TIMER.describeLimits());
        assertEquals("Each player has a total time bank of 1 day, and will time out with a loss if they run out of "
                + "their time bank or take longer than 1 day between actions.", GameTimer.GLACIAL_TIMER.describeLimits());
        assertTrue(GameTimer.SLOW_TIMER.describeLimits().contains("80 minutes"));
    }

    @Test
    public void toStringKeepsItsLeadIn() {
        assertEquals("This game table uses the 'Default' timer.  " + GameTimer.DEFAULT_TIMER.describeLimits(),
                GameTimer.DEFAULT_TIMER.toString());
    }

    @Test
    public void theGlacialCodeResolvesToTheGlacialTimer() {
        // HallRequestHandler.createTable hides glacial tables by resolving the form's code
        assertSame(GameTimer.GLACIAL_TIMER, GameTimer.ResolveTimer("glacial"));
        assertSame(GameTimer.GLACIAL_TIMER, GameTimer.ResolveTimer("Glacial"));
        assertSame(GameTimer.DEFAULT_TIMER, GameTimer.ResolveTimer(null));
        assertFalse("the old comparison never matched", "glacial".equals(GameTimer.GLACIAL_TIMER.name()));
    }
}
