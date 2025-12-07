package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "103_1");
					// Need: Sam with "Response: If Frodo dies, make Sam the Ring-bearer"
					put("sam", "1_311");
					put("pippin", "1_306"); // Unbound Hobbit
					put("gimli", "1_13"); // Non-Hobbit
					// Generic companions for 5+ companion test
					put("companion1", "1_89");
					put("companion2", "1_89");
					put("companion3", "1_89");
					put("companion4", "1_89");

					// Shadow support cards for hinder test
					put("shared1", "1_264"); // Need Shadow support card
					put("shared2", "1_264"); // Same title as shared1
					put("different1", "1_281"); // Different title
					put("runner", "1_178");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PhialofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: V3
		 * Name: Phial of Galadriel, Bane of Darkness
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Phial
		 * Vitality: 1
		 * Game Text: Bearer must be a Ring-bound Hobbit. Discard this if you can spot more than 4 companions.
		 *     Maneuver: Add 2 burdens to hinder all Shadow support cards with a title you spot.
		 *     Response: If The One Ring is transferred, play this on the new Ring-bearer.  You may use this ability from your discard pile.
		 */

		var scn = GetScenario();
		var card = scn.GetFreepsCard("phial");

		assertEquals("Phial of Galadriel", card.getBlueprint().getTitle());
		assertEquals("Bane of Darkness", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PHIAL));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}
	@Test
	public void PhialAttachesToRingBoundHobbitOnly() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("phial");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToHand(phial);
		scn.MoveCompanionsToTable(pippin, gimli);

		scn.StartGame();

		// Frodo is the only ring-bound Hobbit, so Phial auto-attaches to him
		assertTrue(scn.FreepsPlayAvailable(phial));
		scn.FreepsPlayCard(phial);

		assertAttachedTo(phial, frodo);
	}

	@Test
	public void PhialDiscardsWhen5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		scn.AttachCardsTo(frodo, phial);

		// Start with Frodo + 3 companions = 4 total
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();

		// 4 companions total, Phial should remain attached
		assertAttachedTo(phial, frodo);

		// Add 4th companion (5 total with Frodo)
		scn.MoveCompanionsToTable("companion4");
		scn.FreepsPassCurrentPhaseAction();

		// Now 5 total companions, Phial should auto-discard
		assertEquals(Zone.DISCARD, phial.getZone());
	}

	@Test
	public void PhialManeuverAbilityHindersCardsByTitle() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var shared1 = scn.GetShadowCard("shared1");
		var shared2 = scn.GetShadowCard("shared2");
		var different1 = scn.GetShadowCard("different1");

		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToSupportArea(shared1, shared2, different1);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.RemoveBurdens(1); //we start with 1 due to the initial bid
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(shared1));
		assertFalse(scn.IsHindered(shared2));
		assertFalse(scn.IsHindered(different1));

		assertTrue(scn.FreepsActionAvailable(phial));
		scn.FreepsUseCardAction(phial);

		// Choose shared1's title (shared2 has same title, different1 has different title)
		scn.FreepsChooseCard(shared1);

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.IsHindered(shared1));
		assertTrue(scn.IsHindered(shared2)); // Same title as shared1
		assertFalse(scn.IsHindered(different1)); // Different title
	}

	@Test
	public void PhialofGaladrielCanPlayOnSamWhenInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var phial = scn.GetFreepsCard("phial");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(phial);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);
		scn.FreepsPass();
		scn.ShadowPass();

		//The One Ring offering to convert wound to burden
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(sam, ring.getAttachedTo());

		assertInZone(Zone.DISCARD, phial);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsActionAvailable(phial));

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.ATTACHED, phial);
		assertEquals(sam, phial.getAttachedTo());
	}

	@Test
	public void PhialofGaladrielCanPlayOnSamWhenOnFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var phial = scn.GetFreepsCard("phial");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, phial);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 4);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);
		scn.FreepsPass();
		scn.ShadowPass();

		//The One Ring offering to convert wound to burden
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(sam, ring.getAttachedTo());

		assertInZone(Zone.DISCARD, phial);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsActionAvailable(phial));

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.ATTACHED, phial);
		assertEquals(sam, phial.getAttachedTo());
	}
}
