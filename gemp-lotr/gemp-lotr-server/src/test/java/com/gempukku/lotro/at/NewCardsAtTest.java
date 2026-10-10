package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

public class NewCardsAtTest extends AbstractAtTest {
//    @Test
//    public void choiceEffect() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        PhysicalCardImpl getOutOfTheShire = createCard(P1, "40_320");
//        PhysicalCardImpl merry = createCard(P1, "40_256");
//        PhysicalCardImpl nazgul = createCard(P2, "40_211");
//
//        _game.getGameState().addCardToZone(_game, getOutOfTheShire, Zone.HAND);
//        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
//        _game.getGameState().addCardToZone(_game, nazgul, Zone.SHADOW_CHARACTERS);
//
//        skipMulligans();
//
//        // Pass in fellowship
//        playerDecided(P1, "");
//
//        // Pass in shadow
//        playerDecided(P2, "");
//
//        // Pass in maneuver
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        // Pass in archery
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        // Pass in assignment
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        playerDecided(P1, merry.getCardId() + " " + nazgul.getCardId());
//
//        playerDecided(P1, "" + merry.getCardId());
//
//        // Play Get Out of the Shire
//        playerDecided(P1, "0");
//
//        // Choose to cancel skirmish
//        playerDecided(P1, "1");
//
//        // We're in Fierce skirmishes
//        assertEquals(Phase.ASSIGNMENT, _game.getGameState().getCurrentPhase());
//        assertEquals(0, _game.getGameState().getWounds(merry));
//        assertEquals(Zone.FREE_CHARACTERS, merry.getZone());
//    }

//    @Test
//    public void conditionalEffect() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        PhysicalCardImpl getOutOfTheShire = createCard(P1, "40_320");
//        PhysicalCardImpl nazgul = createCard(P2, "40_211");
//
//        _game.getGameState().addCardToZone(_game, getOutOfTheShire, Zone.HAND);
//        _game.getGameState().addCardToZone(_game, nazgul, Zone.SHADOW_CHARACTERS);
//
//        final PhysicalCard frodo = _game.getGameState().getRingBearer(P1);
//
//        skipMulligans();
//
//        // Pass in fellowship
//        playerDecided(P1, "");
//
//        // Pass in shadow
//        playerDecided(P2, "");
//
//        // Pass in maneuver
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        // Pass in archery
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        // Pass in assignment
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        playerDecided(P1, frodo.getCardId() + " " + nazgul.getCardId());
//
//        playerDecided(P1, "" + frodo.getCardId());
//
//        // Play Get Out of the Shire
//        playerDecided(P1, "0");
//
//        // No choice given
//        assertNull(_userFeedback.getAwaitingDecision(P1));
//    }

//    @Test
//    public void costToEffect() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        PhysicalCardImpl celeborn = createCard(P1, "40_38");
//        PhysicalCardImpl celebornInDeck = createCard(P1, "40_38");
//        PhysicalCardImpl nazgul = createCard(P2, "40_211");
//
//        _game.getGameState().addCardToZone(_game, celeborn, Zone.SUPPORT);
//        _game.getGameState().putCardOnTopOfDeck(celebornInDeck);
//        _game.getGameState().addCardToZone(_game, nazgul, Zone.SHADOW_CHARACTERS);
//
//        skipMulligans();
//
//        // Pass in fellowship
//        playerDecided(P1, "");
//
//        // Pass in shadow
//        playerDecided(P2, "");
//
//        // Use Celeborn
//        playerDecided(P1, "0");
//        // Pass on reveal
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        // Choose to discard
//        playerDecided(P1, "0");
//
//        assertEquals(Zone.DISCARD, celebornInDeck.getZone());
//        assertEquals(1, _game.getGameState().getWounds(nazgul));
//    }

//    @Test
//    public void costToEffectPass() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        PhysicalCardImpl celeborn = createCard(P1, "40_38");
//        PhysicalCardImpl celebornInDeck = createCard(P1, "40_38");
//        PhysicalCardImpl nazgul = createCard(P2, "40_211");
//
//        _game.getGameState().addCardToZone(_game, celeborn, Zone.SUPPORT);
//        _game.getGameState().putCardOnTopOfDeck(celebornInDeck);
//        _game.getGameState().addCardToZone(_game, nazgul, Zone.SHADOW_CHARACTERS);
//
//        skipMulligans();
//
//        // Pass in fellowship
//        playerDecided(P1, "");
//
//        // Pass in shadow
//        playerDecided(P2, "");
//
//        // Use Celeborn
//        playerDecided(P1, "0");
//        // Pass on reveal
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        // Choose not to discard
//        playerDecided(P1, "1");
//
//        assertEquals(Zone.DECK, celebornInDeck.getZone());
//        assertEquals(0, _game.getGameState().getWounds(nazgul));
//    }

//    @Test
//    public void discardCardEffect() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        final PhysicalCardImpl blackBreath = createCard(P2, "40_183");
//        final PhysicalCardImpl athelas = createCard(P1, "40_313");
//        final PhysicalCardImpl athelasInHand = createCard(P1, "40_313");
//        PhysicalCardImpl aragorn = createCard(P1, "40_94");
//
//        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
//        _game.getGameState().attachCard(_game, blackBreath, aragorn);
//        _game.getGameState().addCardToZone(_game, athelas, Zone.HAND);
//        _game.getGameState().addCardToZone(_game, athelasInHand, Zone.HAND);
//
//        skipMulligans();
//
//        // Play athelas
//        playerDecided(P1, "0");
//        // Attach to Aragorn
//        playerDecided(P1, "" + aragorn.getCardId());
//
//        playerDecided(P1, "0");
//
//        playerDecided(P1, "0");
//
//        assertEquals(Zone.DISCARD, blackBreath.getZone());
//    }

    //Conditional strength bonus (+X or +Y if Z)

//    @Test
//    public void strengthBonusDependingOnCharacterPlayedOn() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        final PhysicalCardImpl gandalf = createCard(P1, "40_70");
//        final PhysicalCardImpl bolsteredSpirits = createCard(P1, "40_67");
//        PhysicalCardImpl nazgul = createCard(P2, "40_211");
//
//        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);
//        _game.getGameState().addCardToZone(_game, bolsteredSpirits, Zone.HAND);
//        _game.getGameState().addCardToZone(_game, nazgul, Zone.SHADOW_CHARACTERS);
//
//        skipMulligans();
//
//        // Pass in fellowship
//        playerDecided(P1, "");
//
//        // Pass in shadow
//        playerDecided(P2, "");
//
////Pass in maneuver
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
////Pass in archery
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
////Pass in assignment
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        playerDecided(P1, gandalf.getCardId() + " " + nazgul.getCardId());
//        playerDecided(P1, "" + gandalf.getCardId());
//
//        playerDecided(P1, "0");
//        playerDecided(P1, "");
//
//        assertEquals(7 + 3, _game.getModifiersQuerying().getStrength(_game, gandalf));
//    }
//
//    @Test
//    public void strengthBonusDependingOnCharacterPlayedOn2() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        final PhysicalCardImpl gandalf = createCard(P1, "40_70");
//        final PhysicalCardImpl boromir = createCard(P1, "40_101");
//        final PhysicalCardImpl goBackToTheShadows = createCard(P1, "40_312");
//        final PhysicalCardImpl nazgul = createCard(P2, "40_211");
//
//        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);
//        _game.getGameState().addCardToZone(_game, boromir, Zone.FREE_CHARACTERS);
//        _game.getGameState().addCardToZone(_game, goBackToTheShadows, Zone.HAND);
//        _game.getGameState().addCardToZone(_game, nazgul, Zone.SHADOW_CHARACTERS);
//        _game.getGameState().removeBurdens(_game.getGameState().getBurdens());
//
//        skipMulligans();
//
//        // Pass in fellowship
//        playerDecided(P1, "");
//
//        // Pass in shadow
//        playerDecided(P2, "");
//
////Pass in maneuver
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
////Pass in archery
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
////Pass in assignment
//        playerDecided(P1, "");
//        playerDecided(P2, "");
//
//        playerDecided(P1, boromir.getCardId() + " " + nazgul.getCardId());
//        playerDecided(P1, "" + boromir.getCardId());
//
//        playerDecided(P1, "0");
//
//        assertEquals(14 - 3, _game.getModifiersQuerying().getStrength(_game, nazgul));
//    }

//    @Test
//    public void mathProgrammingDiscardBoth() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        final PhysicalCardImpl gandalf = createCard(P1, "40_70");
//        final PhysicalCardImpl discerment = createCard(P1, "40_68");
//        final PhysicalCardImpl blackBreath = createCard(P2, "1_207");
//        final PhysicalCardImpl blackBreath2 = createCard(P2, "1_207");
//
//        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);
//        _game.getGameState().addCardToZone(_game, discerment, Zone.HAND);
//        _game.getGameState().attachCard(_game, blackBreath, gandalf);
//        _game.getGameState().attachCard(_game, blackBreath2, gandalf);
//
//        skipMulligans();
//
//        playerDecided(P1, "0");
//        playerDecided(P1, "2");
//        playerDecided(P1, "");
//
//        playerDecided(P1, "" + blackBreath.getCardId());
//        playerDecided(P1, "" + blackBreath2.getCardId());
//
//        assertEquals(Zone.DISCARD, blackBreath.getZone());
//        assertEquals(Zone.DISCARD, blackBreath2.getZone());
//    }

    //Max twilight shenanigans

//    @Test
//    public void mathProgrammingDiscardOne() throws DecisionResultInvalidException, CardNotFoundException {
//        initializeSimplestGame();
//
//        final PhysicalCardImpl gandalf = createCard(P1, "1_72");
//        final PhysicalCardImpl discerment = createCard(P1, "40_68");
//        final PhysicalCardImpl blackBreath = createCard(P2, "1_207");
//        final PhysicalCardImpl blackBreath2 = createCard(P2, "1_207");
//
//        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);
//        _game.getGameState().addCardToZone(_game, discerment, Zone.HAND);
//        _game.getGameState().attachCard(_game, blackBreath, gandalf);
//        _game.getGameState().attachCard(_game, blackBreath2, gandalf);
//
//        skipMulligans();
//
//        playerDecided(P1, "0");
//        playerDecided(P1, "1");
//        playerDecided(P1, "");
//
//        playerDecided(P1, "" + blackBreath.getCardId());
//        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, _userFeedback.getAwaitingDecision(P1).getDecisionType());
//
//        assertEquals(Zone.DISCARD, blackBreath.getZone());
//        assertEquals(Zone.ATTACHED, blackBreath2.getZone());
//    }

    @Test
    public void strengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardImpl grimbeorn = createCard(P1, "14_6");
        final PhysicalCardImpl nazgulInHand = createCard(P1, "1_237");
        final PhysicalCardImpl nazgul = createCard(P2, "1_237");

        _game.getGameState().addCardToZone(_game, grimbeorn, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, nazgul, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, nazgulInHand, Zone.HAND);

        skipMulligans();

        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, "0");
        playerDecided(P1, "");
    }
}
