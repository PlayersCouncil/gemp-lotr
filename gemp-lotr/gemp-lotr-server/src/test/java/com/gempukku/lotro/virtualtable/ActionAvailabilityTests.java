package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the checks that ask whether one particular card's action is on offer, and what the
 * decision readers answer when a player is not being asked anything.
 */
public class ActionAvailabilityTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");            // Sam, Son of Hamfast
					put("sword1", "1_299");         // Hobbit Sword: bearer must be a Hobbit
					put("sword2", "1_299");

					put("taskmaster1", "2_90");     // Orc Taskmaster: 3 twilight, vitality 3. Shadow: Exert this minion to add (1).
					put("taskmaster2", "2_90");
					put("gollum", "7_58");          // Gollum, Plotting Deceiver: may be played from the discard pile with initiative

					// Library of Rivendell.  "Fellowship: Stack your tale or artifact from play or from hand here to
					// draw a card."  Its definition words the ability itself, so the game does not offer it as
					// "Use Library of Rivendell".
					put("library", "101_11");
					put("staff", "2_22");           // Gandalf's Staff: an artifact

					// Athelas.  "Bearer must be a [gondor] Man.  Fellowship: Discard this possession to heal a
					// companion or to remove a Shadow condition from a companion."  Cost 1.
					put("athelas", "1_94");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability only
					put("boromir", "1_97");         // Boromir, Son of Denethor: a [gondor] Man; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4
					// Armored Easterling: three skirmish abilities, each "Remove (2) to make a lurker ...":
					// strength +1, fierce until the regroup phase, damage +1.
					put("easterling", "11_68");
					// Emboldened Orc: a lurker, strength 11, vitality 2, stronger for each companion who is not
					// assigned to a skirmish.  It has no ability to use.
					put("emboldened", "11_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UseAvailabilityAnswersForTheCardAskedAboutAndNotForAnotherCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fresh = scn.GetShadowCard("taskmaster1");
		var exhausted = scn.GetShadowCard("taskmaster2");
		scn.MoveMinionsToTable(fresh, exhausted);
		// Vitality 3 with 2 wounds: exhausted, so this copy cannot pay its own exertion.
		scn.AddWoundsToChar(exhausted, 2);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// The two copies are described identically, and only one of them is on offer.
		assertTrue(scn.ShadowActionAvailable("Use Orc Taskmaster"));
		assertTrue(scn.ShadowActionAvailable(fresh));
		assertFalse(scn.ShadowActionAvailable(exhausted));

		// The Free Peoples player is not being asked anything, and the card is not theirs to use in any case.
		assertFalse(scn.FreepsActionAvailable(fresh));
	}

	@Test
	public void PlayAvailabilityAnswersForTheCardAskedAboutAndNotForAnotherCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inHand = scn.GetShadowCard("taskmaster1");
		var inDiscard = scn.GetShadowCard("taskmaster2");
		scn.MoveCardsToHand(inHand);
		scn.MoveCardsToDiscard(inDiscard);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.ShadowPlayAvailable(inHand));
		assertFalse(scn.ShadowPlayAvailable(inDiscard));
		// Playing is not using.
		assertFalse(scn.ShadowActionAvailable(inHand));

		scn.ShadowPlayCard(inHand);
		assertInZone(Zone.SHADOW_CHARACTERS, inHand);
		assertFalse(scn.ShadowPlayAvailable(inHand));
		assertTrue(scn.ShadowActionAvailable(inHand));
		assertFalse(scn.ShadowActionAvailable(inDiscard));
	}

	@Test
	public void TransferAvailabilityAnswersForTheCardAskedAboutAndNotForAnotherCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var borne = scn.GetFreepsCard("sword1");
		var inHand = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, borne);
		scn.MoveCardsToHand(inHand);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		// Frodo's sword can go to Sam; the copy in hand can only be played.
		assertTrue(scn.FreepsTransferAvailable(borne));
		assertFalse(scn.FreepsTransferAvailable(inHand));
		assertTrue(scn.FreepsPlayAvailable(inHand));
		assertFalse(scn.FreepsPlayAvailable(borne));

		scn.FreepsTransferCard(borne);
		assertAttachedTo(borne, sam);
	}

	@Test
	public void UseAvailabilitySeesTheActionOfACardThatIsNotInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToDiscard(gollum);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// The Free Peoples player's hand is empty, so the Shadow player has initiative.
		assertEquals(0, scn.GetFreepsHandCount());
		// The engine offers this as the use of the card in the discard pile, and that card is what the offer names.
		assertTrue(scn.ShadowActionAvailable(gollum));
		assertFalse(scn.ShadowPlayAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
	}

	@Test
	public void UseAvailabilitySeesAnAbilityThatHasWordingOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var staff = scn.GetFreepsCard("staff");
		var sword = scn.GetFreepsCard("sword1");
		scn.MoveCardsToSupportArea(library);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		// A Hobbit Sword in hand is neither a tale nor an artifact: there is nothing to stack, and the sword's own
		// play is not the Library's ability.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertFalse(scn.FreepsActionAvailable("Use Library of Rivendell"));
		assertFalse(scn.FreepsActionAvailable(library));
		assertFalse(scn.FreepsActionAvailable(sword));

		// With an artifact in hand the ability is on offer, under its own wording.
		scn.MoveCardsToHand(staff);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsActionAvailable("Stack your tale or artifact from hand"));
		assertFalse(scn.FreepsActionAvailable("Use Library of Rivendell"));
		assertTrue(scn.FreepsActionAvailable(library));
		// It is the Library's and nobody else's, and it is a use and not a play.
		assertFalse(scn.FreepsActionAvailable(staff));
		assertFalse(scn.FreepsPlayAvailable(library));
		assertFalse(scn.ShadowActionAvailable(library));
	}

	@Test
	public void TheNoEffectFlagIsReadForOneActionOfOneCardAndTheReadingFailsOtherwise() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var easterling = scn.GetShadowCard("easterling");
		var emboldened = scn.GetShadowCard("emboldened");

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, emboldened);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, emboldened);

		// The Free Peoples player is asked first, and nothing of the Easterling's is his to use.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertThrows(AssertionError.class, () -> scn.FreepsActionNoEffect(easterling));
		// The Shadow player is not being asked anything yet.
		assertThrows(AssertionError.class, () -> scn.ShadowActionNoEffect(easterling, "fierce"));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// The Easterling's three and the site's (Ettenmoors: "Skirmish: Exert your companion or minion ...").
		assertEquals(4, scn.ShadowGetAvailableActions().size());
		// Three actions of the one card: which is meant has to be said.
		assertThrows(AssertionError.class, () -> scn.ShadowActionNoEffect(easterling));
		assertThrows(AssertionError.class, () -> scn.ShadowActionNoEffect(easterling, "lurker"));
		// The lurker is the minion fighting, so each of the three does something.
		assertEquals("false", scn.ShadowActionNoEffect(easterling, "strength"));
		assertEquals("false", scn.ShadowActionNoEffect(easterling, "FIERCE"));
		assertEquals("false", scn.ShadowActionNoEffect(easterling, "damage"));
		// A card that is offering nothing, and words that none of the card's actions has.
		assertThrows(AssertionError.class, () -> scn.ShadowActionNoEffect(emboldened));
		assertThrows(AssertionError.class, () -> scn.ShadowActionNoEffect(easterling, "archer"));
		// Reading changes nothing: the same four are still on offer.
		assertEquals(4, scn.ShadowGetAvailableActions().size());
	}

	@Test
	public void UseCardActionTakesTheUseOfACardThatCouldAlsoBeTransferred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas);
		// The only wounded companion, so the heal is his without a question
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();

		// Boromir could bear it too: the game lists the transfer of a card before its ability.
		assertTrue(scn.FreepsTransferAvailable(athelas));
		assertTrue(scn.FreepsActionAvailable(athelas));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(athelas);
		// Used and not transferred: discarded for its cost, nothing paid for a transfer, Boromir healed
		assertInZone(Zone.DISCARD, athelas);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowUseCardActionTakesTheUseOfACardThatCouldAlsoBeTransferred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Player 2's own fellowship: on his turn the Shadow methods are the Free Peoples player's
		var aragorn = scn.GetShadowCard("aragorn");
		var boromir = scn.GetShadowCard("boromir");
		var athelas = scn.GetShadowCard("athelas");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertTrue(scn.ShadowTransferAvailable(athelas));
		assertTrue(scn.ShadowActionAvailable(athelas));
		assertEquals(0, scn.GetTwilight());

		scn.ShadowUseCardAction(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void UseCardActionTakesTheFirstActionListedForACardThatOffersNoUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var borne = scn.GetFreepsCard("sword1");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, borne);

		scn.StartGame();

		// A Hobbit Sword has no ability: the one action it offers is its transfer, and that is what is taken,
		// as it was before the one-argument form learned to look for a "Use".
		assertTrue(scn.FreepsTransferAvailable(borne));
		assertFalse(scn.FreepsActionAvailable(borne));

		scn.FreepsUseCardAction(borne);
		assertAttachedTo(borne, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChecksAnswerFalseWhenThePlayerIsNotBeingAskedAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taskmaster = scn.GetShadowCard("taskmaster1");
		var sword = scn.GetFreepsCard("sword1");
		scn.MoveMinionsToTable(taskmaster);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		// The fellowship phase is the Free Peoples player's alone.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertFalse(scn.ShadowActionAvailable(taskmaster));
		assertFalse(scn.ShadowPlayAvailable(taskmaster));
		assertFalse(scn.ShadowTransferAvailable(taskmaster));
		assertFalse(scn.ShadowActionAvailable("Use Orc Taskmaster"));
		assertFalse(scn.ShadowHasCardChoiceAvailable(taskmaster));
		assertFalse(scn.ShadowHasBPChoiceAvailable("2_90"));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(taskmaster));
		assertNull(scn.ShadowGetADParam("actionText"));
		assertNull(scn.GetAwaitingDecisionParams(scn.P2()));

		// The player who is being asked still gets an answer.
		assertTrue(scn.FreepsPlayAvailable(sword));
	}
}
