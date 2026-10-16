package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_259_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vision", "4_259");
					put("vision2", "4_259");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer

					put("axeman", "4_224");     // Easterling Axeman: [raider] Easterling Man; his text wants 2 burdens
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: [raider] Easterling who is a Nazgul; fierce, his text wants 3 burdens
					put("soldier", "4_220");    // Desert Soldier: [raider] Man who is a Southron and no Easterling
					put("brutal", "12_55");     // Brutal Easterling: [men] Man with the word in his title only, no Easterling keyword
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VisionFromAfarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Vision From Afar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: <b>Assignment:</b> Assign an Easterling to the Ring-bearer. The Free Peoples player may add a burden to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vision");

		assertEquals("Vision From Afar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AssignsTheChosenEasterlingToTheRingBearerWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vision = scn.GetShadowCard("vision");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");
		var brutal = scn.GetShadowCard("brutal");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(vision);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, khamul, soldier, brutal);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.ShadowPlayAvailable(vision));
		scn.ShadowPlayCard(vision);

		// Easterlings of two races; not a [raider] Man who is no Easterling, nor a Man with the word in his title only.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, brutal));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(frodo, khamul));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(axeman));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, vision);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddABurdenToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vision = scn.GetShadowCard("vision");
		var frodo = scn.GetRingBearer();
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(vision);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(vision);
		// The one Easterling is chosen unasked.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(axeman));
		assertInZone(Zone.DISCARD, vision);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithTheRingBearerAlreadyAssignedASecondCopyAssignsNobodyAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vision = scn.GetShadowCard("vision");
		var vision2 = scn.GetShadowCard("vision2");
		var frodo = scn.GetRingBearer();
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(vision, vision2);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, khamul);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowPlayCard(vision);
		scn.ShadowChooseCard(axeman);
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(frodo, axeman));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		// CR 4.2 "assignment phase": an assignment action assigns a minion to a companion only if both are
		// unassigned. With the Ring-bearer assigned there is nothing to do; the event is still played, for nothing.
		assertTrue(scn.ShadowPlayAvailable(vision2));
		scn.ShadowPlayCard(vision2);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(khamul));
		assertTrue(scn.IsCharAssignedAgainst(frodo, axeman));
		assertInZone(Zone.DISCARD, vision2);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoEasterlingInPlayTheEventIsStillPlayedForNothingAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vision = scn.GetShadowCard("vision");
		var frodo = scn.GetRingBearer();
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(vision);

		scn.StartGame();
		// The only minion is a [raider] Man who is no Easterling.
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.ShadowPlayAvailable(vision));
		scn.ShadowPlayCard(vision);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(soldier));
		assertInZone(Zone.DISCARD, vision);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vision = scn.GetShadowCard("vision");
		var frodo = scn.GetRingBearer();
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(vision);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.ShadowPlayAvailable(vision));
	}
}
