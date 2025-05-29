package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_02_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ferny", "2_75");
					put("runner", "1_178");

					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BillFernyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Bill Ferny, Swarthy Sneering Fellow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 2
		 * Game Text: Nazgûl are not roaming.
		 * The Free Peoples player may not assign a character to skirmish Bill Ferny. Discard Bill Ferny if underground.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ferny");

		assertEquals("Bill Ferny", card.getBlueprint().getTitle());
		assertEquals("Swarthy Sneering Fellow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void BillFernyCannotBeAssignedByFreepsWhenAlone() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveMinionsToTable(ferny);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());

		scn.ShadowAssignToMinions(frodo, ferny);
		scn.FreepsResolveSkirmish(frodo);
	}

	@Test
	public void BillFernyCannotBeAssignedByFreepsWhenWithOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionToTable(gimli);

		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(ferny, runner);

		scn.StartGame();

		scn.SkipToAssignments();

		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		scn.FreepsAssignToMinions(frodo, runner);

		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());

		scn.ShadowAssignToMinions(frodo, ferny);
		scn.FreepsResolveSkirmish(frodo);
	}
}
