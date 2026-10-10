package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AssignmentAtTest extends AbstractAtTest {
    @Test
    public void balrogPreventingToAssign() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, LotroDeck> decks = new HashMap<>();
        final LotroDeck p1Deck = createSimplestDeck();
        p1Deck.setRingBearer("9_14");
        decks.put(P1, p1Deck);
        final LotroDeck p2Deck = createSimplestDeck();
        p2Deck.addCard("6_76");
        p2Deck.addCard("2_61");
        decks.put(P2, p2Deck);

        initializeGameWithDecks(decks);

        skipMulligans();

        _game.getGameState().addTwilight(30);

        // End fellowship phase
        playerDecided(P1, "");

        PhysicalCardImpl theBalrog = new PhysicalCardImpl(102, "6_76", P2, _cardLibrary.getLotroCardBlueprint("6_76"));
        PhysicalCardImpl otherMinion = new PhysicalCardImpl(103, "2_61", P2, _cardLibrary.getLotroCardBlueprint("2_61"));

        _game.getGameState().addCardToZone(_game, theBalrog, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, otherMinion, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        playerDecided(P2, "");

        // End maneuvers phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End arhcery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        final List<Assignment> assignmentsBeforeFreePlayer = _game.getGameState().getAssignments();
        assertEquals(0, assignmentsBeforeFreePlayer.size());

        PhysicalCard galadriel = _game.getGameState().getRingBearer(P1);

        AwaitingDecision fpAssignment = _userFeedback.getAwaitingDecision(P1);

        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, fpAssignment.getDecisionType());
        validateContents(toCardIdArray(theBalrog, otherMinion), (String[]) fpAssignment.getDecisionParameters().get("minions"));
        validateContents(toCardIdArray(galadriel), (String[]) fpAssignment.getDecisionParameters().get("freeCharacters"));

        try {
            playerDecided(P1, galadriel.getCardId() + " " + theBalrog.getCardId());
            fail("Can't assign Balrog to Galadriel");
        } catch (DecisionResultInvalidException exp) {
            // Expected
        }
        playerDecided(P1, galadriel.getCardId() + " " + otherMinion.getCardId());

        AwaitingDecision shadowAssignment = _userFeedback.getAwaitingDecision(P2);

        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, shadowAssignment.getDecisionType());
        validateContents(toCardIdArray(theBalrog), (String[]) shadowAssignment.getDecisionParameters().get("minions"));
        validateContents(toCardIdArray(galadriel), (String[]) shadowAssignment.getDecisionParameters().get("freeCharacters"));

        try {
            playerDecided(P2, galadriel.getCardId() + " " + theBalrog.getCardId());
            fail("Can't assign Balrog to Galadriel");
        } catch (DecisionResultInvalidException exp) {
            // Expected
        }
    }

    @Test
    public void dunlendingWarriorAssignsToAlly() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl elrond = new PhysicalCardImpl(101, "1_40", P1, _cardLibrary.getLotroCardBlueprint("1_40"));
        PhysicalCardImpl dunlendingWarrior = new PhysicalCardImpl(102, "4_18", P2, _cardLibrary.getLotroCardBlueprint("4_18"));

        _game.getGameState().addCardToZone(_game, elrond, Zone.SUPPORT);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, dunlendingWarrior, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        playerDecided(P2, "");

        // End maneuvers phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End arhcery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");

        AwaitingDecision assignmentActions = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, assignmentActions.getDecisionType());
        validateContents(toCardIdArray(dunlendingWarrior), (String[]) assignmentActions.getDecisionParameters().get("cardId"));

        playerDecided(P2, "0");

        final List<Assignment> assignmentsAfterFreePlayer = _game.getGameState().getAssignments();
        assertEquals(1, assignmentsAfterFreePlayer.size());
        assertEquals(elrond, assignmentsAfterFreePlayer.get(0).getFellowshipCharacter());
        assertEquals(1, assignmentsAfterFreePlayer.get(0).getShadowCharacters().size());
        assertTrue(assignmentsAfterFreePlayer.get(0).getShadowCharacters().contains(dunlendingWarrior));
    }

    @Test
    public void slowKindledCourageAllowsMinionsToAssign() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl merry = new PhysicalCardImpl(100, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        PhysicalCardImpl slowKindledCourage = new PhysicalCardImpl(101, "7_328", P1, _cardLibrary.getLotroCardBlueprint("7_328"));
        PhysicalCardImpl urukHaiRaidingParty = new PhysicalCardImpl(102, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, slowKindledCourage,  merry);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        playerDecided(P2, "");

        // End maneuvers phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End arhcery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assignment phase
        playerDecided(P1, "");

        AwaitingDecision assignmentActions = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, assignmentActions.getDecisionType());
        validateContents(toCardIdArray(urukHaiRaidingParty), (String[]) assignmentActions.getDecisionParameters().get("cardId"));

        playerDecided(P2, "0");

        assertEquals(1, _game.getGameState().getAssignments().size());
        final Assignment assignment = _game.getGameState().getAssignments().get(0);
        assertEquals(merry, assignment.getFellowshipCharacter());
        assertEquals(1, assignment.getShadowCharacters().size());
        assertTrue(assignment.getShadowCharacters().contains(urukHaiRaidingParty));
    }

    @Test
    public void assignMorgulSpawnByPayingCost() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl gimli = createCard(P1, "5_7");
        addToZone(gimli, Zone.FREE_CHARACTERS);
        PhysicalCardImpl morgulSpawn = createCard(P2, "7_200");
        addToZone(morgulSpawn, Zone.HAND);
        PhysicalCardImpl attea = createCard(P2, "7_210");
        addToZone(attea, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        setTwilightPool(100);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);


        // End shadow phase
        assertEquals(Phase.SHADOW, getPhase());
        selectCardAction(P2, morgulSpawn);
        pass(P2);

        // End maneuver phase
        pass(P1);
        pass(P2);

        // End archery phase
        pass(P1);
        pass(P2);

        // End assignment phase
        pass(P1);
        pass(P2);

        // Assign
        selectYes(P1);
        selectCard(P1, gimli);
        validateContents(new String[]{String.valueOf(attea.getCardId()), String.valueOf(morgulSpawn.getCardId())}, getAwaitingDecision(P1).getDecisionParameters().get("minions"));
        playerDecided(P1, gimli.getCardId() + " " + morgulSpawn.getCardId());
        pass(P2);

        // Start skirmish
        playerDecided(P1, String.valueOf(gimli.getCardId()));
        pass(P1);
    }

    @Test
    public void assignMorgulSpawnByPayingCostCantAssignWhenNotPaid() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl gimli = createCard(P1, "5_7");
        addToZone(gimli, Zone.FREE_CHARACTERS);
        PhysicalCardImpl morgulSpawn = createCard(P2, "7_200");
        addToZone(morgulSpawn, Zone.HAND);
        PhysicalCardImpl attea = createCard(P2, "7_210");
        addToZone(attea, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        setTwilightPool(100);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);


        // End shadow phase
        assertEquals(Phase.SHADOW, getPhase());
        selectCardAction(P2, morgulSpawn);
        pass(P2);

        // End maneuver phase
        pass(P1);
        pass(P2);

        // End archery phase
        pass(P1);
        pass(P2);

        // End assignment phase
        pass(P1);
        pass(P2);

        // Assign
        selectNo(P1);
        validateContents(new String[]{String.valueOf(attea.getCardId())}, getAwaitingDecision(P1).getDecisionParameters().get("minions"));
    }

    @Test
    public void toldeaDarkShadowCantBeAssignedTwiceToSameCompanion() throws Exception {
        initializeSimplestGame();

        PhysicalCard toldeaDarkShadow = addToZone(createCard(P2, "19_39"), Zone.SHADOW_CHARACTERS);
        PhysicalCard aragorn = addToZone(createCard(P1, "1_89"), Zone.FREE_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, aragorn.getCardId() + " " + toldeaDarkShadow.getCardId());
        selectCard(P1, aragorn);
        pass(P1);
        pass(P2);
        // Fierce assignment
        pass(P1);
        pass(P2);
        try {
            playerDecided(P1, aragorn.getCardId() + " " + toldeaDarkShadow.getCardId());
            fail();
        } catch (DecisionResultInvalidException e) {
        }
    }

    @Test
    public void setupExtraAssignmentAndSkirmishes() throws Exception {
        initializeSimplestGame();

        PhysicalCard witchKing = addToZone(createCard(P2, "12_183"), Zone.SHADOW_CHARACTERS);
        PhysicalCard fellBeast = attachTo(createCard(P2, "12_184"), witchKing);

        passUntil(Phase.ASSIGNMENT);
        // Normal skirmishes
        pass(P1);
        pass(P2);
        pass(P1);
        pass(P2);
        // Fierce skirmishes
        pass(P1);
        pass(P2);
        pass(P1);
        pass(P2);
        selectCardAction(P2, fellBeast);
        assertEquals(2, getWounds(witchKing));
        assertEquals(Phase.ASSIGNMENT, getPhase());
        //As of V3, TWK's Beast is converted to use Relentless under the hood
        assertTrue(_game.getGameState().isRelentlessSkirmishes());
    }
}
