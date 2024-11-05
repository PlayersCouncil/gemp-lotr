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
    public void putCardsFromDeckOnBottomOfDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard intoTheWest = addToZone(createCard(P1, "7_23"), Zone.SUPPORT);
        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        for (int i=0; i<2; i++) {
            addToZone(createCard(P1, "1_3"), Zone.DECK);
        }
        passUntil(Phase.REGROUP);
        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        PhysicalCard topCardFromDeck = deck.get(0);
        selectCardAction(P1, intoTheWest);
        pass(P1);
        pass(P2);
        assertEquals(deck.get(1), topCardFromDeck);
    }

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
    public void putCardFromDiscardOnBottomOfDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard breedingPit = addToZone(createCard(P2, "1_122"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        setTwilightPool(10);
        PhysicalCard urukSavageInDiscard = addToZone(createCard(P2, "1_151"), Zone.DISCARD);
        PhysicalCard urukSavageInHand = addToZone(createCard(P2, "1_151"), Zone.HAND);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P2, "1_151"), Zone.DECK);

        passUntil(Phase.SHADOW);
        selectCardAction(P2, urukSavageInHand);
        selectCardAction(P2, breedingPit);

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P2);
        assertEquals(urukSavageInDiscard, deck.get(1));
    }

    @Test
    public void putCardsFromDiscardOnTopOfDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard gondorStillStands = addToZone(createCard(P1, "7_95"), Zone.SUPPORT);
        PhysicalCard citadelOfTheStars = addToZone(createCard(P1, "5_32"), Zone.DISCARD);

        passUntil(Phase.REGROUP);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P1, "1_151"), Zone.DECK);
        selectCardAction(P1, gondorStillStands);

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        assertEquals(citadelOfTheStars, deck.get(0));
    }

    @Test
    public void putCardsFromPlayOnBottomOfDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard braga = addToZone(createCard(P2, "33_25"), Zone.SHADOW_CHARACTERS);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P2, "1_151"), Zone.DECK);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId()+" "+braga.getCardId());
        pass(P2);
        selectCard(P1, ringBearer);
        pass(P1);
        selectCardAction(P2, braga);

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P2);
        assertEquals(goblinRunner, deck.get(1));
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

    @Test
    public void putRandomCardFromHandBeneathDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard windInHisFace = addToZone(createCard(P1, "7_259"), Zone.HAND);
        PhysicalCard eomer = addToZone(createCard(P1, "7_365"), Zone.FREE_CHARACTERS);
        PhysicalCard swiftSteed = attachTo(createCard(P1, "7_253"), eomer);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P2, "1_151"), Zone.DECK);
        PhysicalCard urukSavageInHand = addToZone(createCard(P2, "1_151"), Zone.HAND);
        pass(P1);
        pass(P2);
        playerDecided(P1, eomer.getCardId() + " " + goblinRunner.getCardId());
        selectCard(P1, eomer);
        selectCardAction(P1, windInHisFace);

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P2);
        assertEquals(urukSavageInHand, deck.get(1));
    }
}
