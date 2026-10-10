package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PutCardsAtTest extends AbstractAtTest {
    @Test
    public void putCardsFromDeckOnTopOfDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard arwen = addToZone(createCard(P1, "1_30"), Zone.FREE_CHARACTERS);
        PhysicalCard backToTheLight = addToZone(createCard(P1, "18_6"), Zone.HAND);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        addToZone(createCard(P1, "1_3"), Zone.DECK);
        addToZone(createCard(P1, "1_4"), Zone.DECK);

        passUntil(Phase.MANEUVER);
        selectCardAction(P1, backToTheLight);
        playerDecided(P1, "1");
        // Select cards to put aside
        selectArbitraryCards(P1, getArbitraryCardId(P1, "1_3"), getArbitraryCardId(P1, "1_4"));
        // Select cards order
        selectArbitraryCards(P1, getArbitraryCardId(P1, "1_3"));

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        assertEquals("1_4", deck.get(0).getBlueprintId());
        assertEquals("1_3", deck.get(1).getBlueprintId());
    }

    @Test
    public void putCardsFromPlayOnTopOfDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard gladdenHomestead = addToZone(createCard(P1, "13_49"), Zone.SUPPORT);
        PhysicalCard sarumansAmbition= addToZone(createCard(P2, "1_133"), Zone.SUPPORT);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.MANEUVER);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P2, "1_151"), Zone.DECK);
        selectCardAction(P1, gladdenHomestead);

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P2);
        assertEquals(sarumansAmbition, deck.get(0));
    }

    @Test
    public void putPlayedEventOnBottomOfDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard tossMe = addToZone(createCard(P1, "6_11"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P1, "1_151"), Zone.DECK);
        selectCardAction(P1, tossMe);
        playerDecided(P1, "1");

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        assertEquals(tossMe, deck.get(1));
    }

    @Test
    public void putPlayedEventOnTopOfDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard tossMe = addToZone(createCard(P1, "6_11"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P1, "1_151"), Zone.DECK);
        selectCardAction(P1, tossMe);
        playerDecided(P1, "0");

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        assertEquals(tossMe, deck.get(0));
    }
}
