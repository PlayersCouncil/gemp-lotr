
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("sons", "101_21");
					put("aragorn", "1_89");
					put("boromir", "1_97");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SonsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *The Sons of Gondor Have Returned
		* Side: Free Peoples
		* Culture: gondor
		* Twilight Cost: 2
		* Type: condition
		* Subtype: Support Area
		* Game Text: While Boromir is assigned to skirmish more than one minion, Aragorn is strength +3.
		* 	While Aragorn is assigned to skirmish more than one minion, Boromir is strength +3.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl sons = scn.GetFreepsCard("sons");

		assertTrue(sons.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sons.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, sons.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, sons.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, sons.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sons, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(2, sons.getBlueprint().getTwilightCost());
		//assertEquals(, sons.getBlueprint().getStrength());
		//assertEquals(, sons.getBlueprint().getVitality());
		//assertEquals(, sons.getBlueprint().getResistance());
		//assertEquals(Signet., sons.getBlueprint().getSignet());
		//assertEquals(, sons.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void SonsPumpsAragornAndBoromir() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl sons = scn.GetFreepsCard("sons");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(sons);

		PhysicalCardImpl runner1 = scn.GetShadowCard("runner1");
		PhysicalCardImpl runner2 = scn.GetShadowCard("runner2");
		PhysicalCardImpl runner3 = scn.GetShadowCard("runner3");
		PhysicalCardImpl runner4 = scn.GetShadowCard("runner4");
		scn.MoveMinionsToTable(runner1, runner2, runner3, runner4);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		scn.PassCurrentPhaseActions();
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1, runner2}, new PhysicalCardImpl[]{boromir, runner3, runner4});

		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(boromir));
	}
}
