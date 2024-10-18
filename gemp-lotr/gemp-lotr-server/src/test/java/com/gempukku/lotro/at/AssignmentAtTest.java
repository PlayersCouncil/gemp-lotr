package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AssignmentAtTest extends AbstractAtTest {
    @Test
    public void orcAssassinAssignNotToAlly() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl merry = new PhysicalCardImpl(100, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        PhysicalCardImpl hobbitPartyGuest = new PhysicalCardImpl(101, "1_297", P1, _cardLibrary.getLotroCardBlueprint("1_297"));
        PhysicalCardImpl orcAssassin = new PhysicalCardImpl(102, "1_262", P2, _cardLibrary.getLotroCardBlueprint("1_262"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, hobbitPartyGuest, Zone.SUPPORT);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, orcAssassin, Zone.SHADOW_CHARACTERS);

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
        playerDecided(P2, "0");

        AwaitingDecision assignCharacter = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, assignCharacter.getDecisionType());
        validateContents(new String[]{String.valueOf(merry.getCardId()), String.valueOf(_game.getGameState().getRingBearer(P1).getCardId())}, (String[]) assignCharacter.getDecisionParameters().get("cardId"));
    }

    @Test
    public void sarumanAssignsToCompanion() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl merry = new PhysicalCardImpl(100, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        PhysicalCardImpl pippin = new PhysicalCardImpl(101, "1_306", P1, _cardLibrary.getLotroCardBlueprint("1_306"));
        PhysicalCardImpl saruman = new PhysicalCardImpl(102, "3_69", P2, _cardLibrary.getLotroCardBlueprint("3_69"));
        PhysicalCardImpl urukHaiRaidingParty = new PhysicalCardImpl(103, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, pippin, Zone.FREE_CHARACTERS);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, saruman, Zone.SHADOW_CHARACTERS);
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
        validateContents(toCardIdArray(saruman), (String[]) assignmentActions.getDecisionParameters().get("cardId"));
        playerDecided(P2, "0");

        AwaitingDecision chooseCompanion = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, chooseCompanion.getDecisionType());
        validateContents(new String[]{String.valueOf(merry.getCardId()), String.valueOf(pippin.getCardId())}, (String[]) chooseCompanion.getDecisionParameters().get("cardId"));
        playerDecided(P2, String.valueOf(merry.getCardId()));

        AwaitingDecision preventDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.MULTIPLE_CHOICE, preventDecision.getDecisionType());
        playerDecided(P1, "1");

        final List<Assignment> assignments = _game.getGameState().getAssignments();
        assertEquals(1, assignments.size());
        assertEquals(merry, assignments.get(0).getFellowshipCharacter());
        assertEquals(1, assignments.get(0).getShadowCharacters().size());
        assertTrue(assignments.get(0).getShadowCharacters().contains(urukHaiRaidingParty));

        playerDecided(P1, "");

        AwaitingDecision assignmentActions2 = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, assignmentActions2.getDecisionType());
        validateContents(toCardIdArray(saruman), (String[]) assignmentActions.getDecisionParameters().get("cardId"));
        playerDecided(P2, "0");

        // This effect fails, as there is no assignable minion anymore
        assertNull(_userFeedback.getAwaitingDecision(P2));
    }

    @Test
    public void normalAssignment() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl merry = new PhysicalCardImpl(100, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        PhysicalCardImpl pippin = new PhysicalCardImpl(101, "1_306", P1, _cardLibrary.getLotroCardBlueprint("1_306"));
        PhysicalCardImpl urukHaiRaidingParty1 = new PhysicalCardImpl(102, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));
        PhysicalCardImpl urukHaiRaidingParty2 = new PhysicalCardImpl(103, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, pippin, Zone.FREE_CHARACTERS);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty1, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty2, Zone.SHADOW_CHARACTERS);

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

        AwaitingDecision assignmentDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, assignmentDecision.getDecisionType());
        validateContents(toCardIdArray(urukHaiRaidingParty1, urukHaiRaidingParty2), (String[]) assignmentDecision.getDecisionParameters().get("minions"));
        validateContents(toCardIdArray(merry, pippin, _game.getGameState().getRingBearer(P1)), (String[]) assignmentDecision.getDecisionParameters().get("freeCharacters"));
        playerDecided(P1, merry.getCardId() + " " + urukHaiRaidingParty1.getCardId());

        final List<Assignment> assignmentsAfterFreePlayer = _game.getGameState().getAssignments();
        assertEquals(1, assignmentsAfterFreePlayer.size());
        assertEquals(merry, assignmentsAfterFreePlayer.get(0).getFellowshipCharacter());
        assertEquals(1, assignmentsAfterFreePlayer.get(0).getShadowCharacters().size());
        assertTrue(assignmentsAfterFreePlayer.get(0).getShadowCharacters().contains(urukHaiRaidingParty1));

        AwaitingDecision shadowAssignmentDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, shadowAssignmentDecision.getDecisionType());
        validateContents(toCardIdArray(urukHaiRaidingParty2), (String[]) shadowAssignmentDecision.getDecisionParameters().get("minions"));
        validateContents(toCardIdArray(merry, pippin, _game.getGameState().getRingBearer(P1)), (String[]) shadowAssignmentDecision.getDecisionParameters().get("freeCharacters"));
        playerDecided(P2, pippin.getCardId() + " " + urukHaiRaidingParty2.getCardId());

        final List<Assignment> assignmentsAfterShadowPlayer = _game.getGameState().getAssignments();
        assertEquals(2, assignmentsAfterFreePlayer.size());
        assertEquals(merry, assignmentsAfterFreePlayer.get(0).getFellowshipCharacter());
        assertEquals(1, assignmentsAfterFreePlayer.get(0).getShadowCharacters().size());
        assertTrue(assignmentsAfterFreePlayer.get(0).getShadowCharacters().contains(urukHaiRaidingParty1));
        assertEquals(pippin, assignmentsAfterFreePlayer.get(1).getFellowshipCharacter());
        assertEquals(1, assignmentsAfterFreePlayer.get(1).getShadowCharacters().size());
        assertTrue(assignmentsAfterFreePlayer.get(1).getShadowCharacters().contains(urukHaiRaidingParty2));

        AwaitingDecision skirmishChoice = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, skirmishChoice.getDecisionType());
    }

    @Test
    public void assignmentsWithDefender() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl merry = new PhysicalCardImpl(100, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        PhysicalCardImpl pippin = new PhysicalCardImpl(101, "1_306", P1, _cardLibrary.getLotroCardBlueprint("1_306"));
        PhysicalCardImpl urukHaiRaidingParty1 = new PhysicalCardImpl(102, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));
        PhysicalCardImpl urukHaiRaidingParty2 = new PhysicalCardImpl(103, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, pippin, Zone.FREE_CHARACTERS);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty1, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty2, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        playerDecided(P2, "");

        // End maneuvers phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End arhcery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Merry gets Defender +1
        _game.getModifiersEnvironment().addUntilEndOfPhaseModifier(
                new AddKeywordModifier(null, merry, null, Keyword.DEFENDER, 1), Phase.ASSIGNMENT);

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        final List<Assignment> assignmentsBeforeFreePlayer = _game.getGameState().getAssignments();
        assertEquals(0, assignmentsBeforeFreePlayer.size());

        AwaitingDecision assignmentDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, assignmentDecision.getDecisionType());
        validateContents(toCardIdArray(urukHaiRaidingParty1, urukHaiRaidingParty2), (String[]) assignmentDecision.getDecisionParameters().get("minions"));
        validateContents(toCardIdArray(merry, pippin, _game.getGameState().getRingBearer(P1)), (String[]) assignmentDecision.getDecisionParameters().get("freeCharacters"));
        try {
            playerDecided(P1, pippin.getCardId() + " " + urukHaiRaidingParty1.getCardId() + " " + urukHaiRaidingParty2.getCardId());
            fail("Pippin can't have multiple minions assigned by FP player");
        } catch (DecisionResultInvalidException exp) {
            // Expected
        }
        // Merry gets two minions (he has Defender +1)
        playerDecided(P1, merry.getCardId() + " " + urukHaiRaidingParty1.getCardId() + " " + urukHaiRaidingParty2.getCardId());

        final List<Assignment> assignmentsAfterFreePlayer = _game.getGameState().getAssignments();
        assertEquals(1, assignmentsAfterFreePlayer.size());
        assertEquals(merry, assignmentsAfterFreePlayer.get(0).getFellowshipCharacter());
        assertEquals(2, assignmentsAfterFreePlayer.get(0).getShadowCharacters().size());
        assertTrue(assignmentsAfterFreePlayer.get(0).getShadowCharacters().contains(urukHaiRaidingParty1));
        assertTrue(assignmentsAfterFreePlayer.get(0).getShadowCharacters().contains(urukHaiRaidingParty2));

        AwaitingDecision skirmishChoice = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, skirmishChoice.getDecisionType());
    }

    @Test
    public void assignmentOfFierceMinions() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl merry = new PhysicalCardImpl(100, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        PhysicalCardImpl pippin = new PhysicalCardImpl(101, "1_306", P1, _cardLibrary.getLotroCardBlueprint("1_306"));
        PhysicalCardImpl urukHaiRaidingParty = new PhysicalCardImpl(102, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));
        PhysicalCardImpl gateTroll = new PhysicalCardImpl(103, "6_103", P2, _cardLibrary.getLotroCardBlueprint("6_103"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, pippin, Zone.FREE_CHARACTERS);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, gateTroll, Zone.SHADOW_CHARACTERS);

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

        AwaitingDecision assignmentDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, assignmentDecision.getDecisionType());
        validateContents(toCardIdArray(urukHaiRaidingParty, gateTroll), (String[]) assignmentDecision.getDecisionParameters().get("minions"));
        validateContents(toCardIdArray(merry, pippin, _game.getGameState().getRingBearer(P1)), (String[]) assignmentDecision.getDecisionParameters().get("freeCharacters"));

        // No assignment from FP player
        playerDecided(P1, "");

        final List<Assignment> assignmentsAfterFreePlayer = _game.getGameState().getAssignments();
        assertEquals(0, assignmentsAfterFreePlayer.size());

        // No assignment from Shadow player
        playerDecided(P2, "");

        // End fierce assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        AwaitingDecision fierceAssignmentDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, fierceAssignmentDecision.getDecisionType());
        validateContents(toCardIdArray(gateTroll), (String[]) fierceAssignmentDecision.getDecisionParameters().get("minions"));
        validateContents(toCardIdArray(merry, pippin, _game.getGameState().getRingBearer(P1)), (String[]) fierceAssignmentDecision.getDecisionParameters().get("freeCharacters"));
    }

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
    public void twoOrcAssassinsAtCarasGaladhon() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl merry = new PhysicalCardImpl(100, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        PhysicalCardImpl carasGaladhon = new PhysicalCardImpl(100, "3_115", P1, _cardLibrary.getLotroCardBlueprint("3_115"));
        PhysicalCardImpl orcAssassin1 = new PhysicalCardImpl(101, "1_262", P2, _cardLibrary.getLotroCardBlueprint("1_262"));
        PhysicalCardImpl orcAssassin2 = new PhysicalCardImpl(102, "1_262", P2, _cardLibrary.getLotroCardBlueprint("1_262"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);

        carasGaladhon.setSiteNumber(2);
        _game.getGameState().addCardToZone(_game, carasGaladhon, Zone.ADVENTURE_PATH);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, orcAssassin1, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, orcAssassin2, Zone.SHADOW_CHARACTERS);

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
        validateContents(toCardIdArray(orcAssassin1, orcAssassin2), (String[]) assignmentActions.getDecisionParameters().get("cardId"));

        playerDecided(P2, "0");

        AwaitingDecision fpFirstAssassinDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, fpFirstAssassinDecision.getDecisionType());
        validateContents(toCardIdArray(merry, _game.getGameState().getRingBearer(P1)), (String[]) fpFirstAssassinDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, String.valueOf(merry.getCardId()));

        assertEquals(1, _game.getGameState().getAssignments().size());
        final Assignment firstAssignment = _game.getGameState().getAssignments().get(0);
        assertEquals(merry, firstAssignment.getFellowshipCharacter());
        assertEquals(1, firstAssignment.getShadowCharacters().size());
        assertTrue(firstAssignment.getShadowCharacters().contains(orcAssassin1));

        playerDecided(P1, "");

        assignmentActions = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, assignmentActions.getDecisionType());
        validateContents(toCardIdArray(orcAssassin2), (String[]) assignmentActions.getDecisionParameters().get("cardId"));

        playerDecided(P2, "0");

        assertEquals(2, _game.getGameState().getAssignments().size());
        final Assignment secondAssignment = _game.getGameState().getAssignments().get(1);
        assertEquals(_game.getGameState().getRingBearer(P1), secondAssignment.getFellowshipCharacter());
        assertEquals(1, secondAssignment.getShadowCharacters().size());
        assertTrue(secondAssignment.getShadowCharacters().contains(orcAssassin2));
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
        assertTrue(_game.getGameState().isExtraSkirmishes());
    }
}
