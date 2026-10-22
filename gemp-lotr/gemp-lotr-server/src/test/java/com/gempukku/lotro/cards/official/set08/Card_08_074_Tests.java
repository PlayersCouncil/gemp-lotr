package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ambusher", "8_74");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, for Calculated Risk's spot; skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, maneuver ability only
					put("guard", "1_7");        // Dwarf Guard: companion; its text is about its own play
					put("risk", "7_4");         // Calculated Risk: Fellowship event, "Spot a Dwarf and add a threat to draw 3 cards"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulAmbusherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Morgul Ambusher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot 3 threats, the minion archery total is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ambusher");

		assertEquals("Morgul Ambusher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TwoThreatsBesideThreeBurdensAddNothingAndTheThirdThreatMakesTheMinionArcheryTotalPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambusher = scn.GetShadowCard("ambusher");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var risk = scn.GetFreepsCard("risk");
		// Four companions with Frodo, so that four threats can lie.
		scn.MoveCompanionsToTable(gimli, aragorn, guard);
		scn.MoveCardsToHand(risk);

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher);
		scn.AddThreats(2);
		// Three burdens beside the threats: a token that is no threat counts for nothing.
		scn.AddBurdens(3);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetThreats());
		assertEquals(3, scn.GetBurdens());
		// No archer on either side: the totals are only what cards add.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(risk);
		assertEquals(3, scn.GetThreats());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// A fourth threat: "3 threats" is at least 3.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
