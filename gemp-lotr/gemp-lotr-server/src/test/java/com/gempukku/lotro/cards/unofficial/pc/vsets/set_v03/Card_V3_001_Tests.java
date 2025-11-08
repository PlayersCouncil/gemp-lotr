package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "103_1");
					put("sam", "1_311");

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
		* 	Maneuver: Add 2 burdens to hinder all Shadow support cards with a title you spot.
		* 	Response: If The One Ring is transferred, play this on the new Ring-bearer.  You may use this ability from your discard pile.
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
