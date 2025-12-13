
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("past", "101_49");
					put("orc", "1_271");
					put("runner", "1_178");
					put("troll", "8_102");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AShadowofthePastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *A Shadow of the Past
		* Side: Free Peoples
		* Culture: sauron
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: While you can spot 4 burdens, each [sauron] Orc is <b>fierce</b>.
		* 	While you can spot 6 burdens, each [Sauron] Orc is damage +1.
		* 	Discard this condition at the start of the regroup phase.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl past = scn.GetFreepsCard("past");

		assertTrue(past.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, past.getBlueprint().getSide());
		assertEquals(Culture.SAURON, past.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, past.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, past.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(past, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, past.getBlueprint().getTwilightCost());
		//assertEquals(, past.getBlueprint().getStrength());
		//assertEquals(, past.getBlueprint().getVitality());
		//assertEquals(, past.getBlueprint().getResistance());
		//assertEquals(Signet., past.getBlueprint().getSignet());
		//assertEquals(, past.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void FourBurdensMakesSauronOrcsFierce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl past = scn.GetShadowCard("past");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		PhysicalCardImpl runner = scn.GetShadowCard("runner");
		PhysicalCardImpl troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(past);
		scn.MoveMinionsToTable(orc, runner, troll);

		scn.StartGame();

		assertFalse(scn.HasKeyword(orc, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));

		scn.AddBurdens(3); // 4 with the initial starting bid

		assertTrue(scn.HasKeyword(orc, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
	}

	@Test
	public void SixBurdensMakesSauronOrcsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl past = scn.GetShadowCard("past");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		PhysicalCardImpl runner = scn.GetShadowCard("runner");
		PhysicalCardImpl troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(past);
		scn.MoveMinionsToTable(orc, runner, troll);

		scn.StartGame();

		assertFalse(scn.HasKeyword(orc, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(runner, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(troll, Keyword.DAMAGE));

		scn.AddBurdens(5); // 6 with the initial starting bid

		assertTrue(scn.HasKeyword(orc, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(orc, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(runner, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(troll, Keyword.DAMAGE));
	}

	@Test
	public void SelfDiscardsInRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl past = scn.GetShadowCard("past");
		scn.MoveCardsToSupportArea(past);

		scn.StartGame();

		assertEquals(Zone.SUPPORT, past.getZone());

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(Zone.DISCARD, past.getZone());
	}
}
