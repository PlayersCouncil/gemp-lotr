package com.gempukku.lotro.game;

import com.gempukku.lotro.logic.GameSeed;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * A server started with a seed (GameSeed, -Dgemp.game.randomSeed) gives every game the same draws, so that the same
 * answers give the same game: the game screen's browser checks play one game on two pages and compare them.  A game
 * at a mediator keeps a journal of its draws and has them watched, which wraps the game's random; what is wrapped
 * has to be the seeded one.
 */
public class SeededGamesTest {
    @After
    public void noSeed() {
        System.clearProperty(GameSeed.SEED_PROPERTY);
    }

    private static String firstTurn() {
        MediatorSitting sitting = new MediatorSitting();
        String first = sitting.toFirstTurn();
        //Who goes first, and every card on the table and in each hand
        return first + " " + sitting.board();
    }

    @Test
    public void OnASeededServerTheSameAnswersGiveTheSameGame() {
        System.setProperty(GameSeed.SEED_PROPERTY, "20261112");
        String game = firstTurn();
        assertEquals(game, firstTurn());
        assertEquals(game, firstTurn());
    }

    @Test
    public void WithoutASeedTwoGamesAreDealtDifferently() {
        //(the check above would pass idly if the boards compared did not show the hands: this is its control.
        // Three games dealt alike by chance, out of 8 cards from 60 each, does not happen)
        String game = firstTurn();
        String another = firstTurn();
        if (game.equals(another))
            assertNotEquals(game, firstTurn());
    }
}
