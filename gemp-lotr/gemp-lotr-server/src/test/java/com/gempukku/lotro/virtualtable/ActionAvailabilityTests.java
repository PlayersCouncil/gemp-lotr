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
		scn.FreepsPassCurrentPhaseAction();

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
		scn.FreepsPassCurrentPhaseAction();

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
		scn.FreepsPassCurrentPhaseAction();

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
