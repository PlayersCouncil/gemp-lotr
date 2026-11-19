package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("creeper", "8_75");
					put("smeagol", "7_71");     // Smeagol, Always Helps: companion, "To play, add a burden"; assignment ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion; skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulCreeperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Morgul Creeper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot 3 burdens, the minion archery total is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("creeper");

		assertEquals("Morgul Creeper", card.getBlueprint().getTitle());
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
	public void TwoBurdensBesideThreeThreatsAddNothingAndTheThirdBurdenMakesTheMinionArcheryTotalPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var creeper = scn.GetShadowCard("creeper");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Three companions with Frodo, so that three threats can lie.
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(creeper);
		scn.AddBurdens(2);
		// Three threats beside the burdens: a token that is no burden counts for nothing.
		scn.AddThreats(3);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		// No archer on either side: the totals are only what cards add.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(smeagol);
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// A fourth burden: "3 burdens" is at least 3.
		scn.AddBurdens(1);
		assertEquals(4, scn.GetBurdens());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
