
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

public class Card_V1_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("walks", "101_42");
					put("twigul1", "2_82");
					put("twigul2", "2_84");
					put("nazgul", "1_232");

					put("filler1", "2_75");
					put("filler2", "2_75");

					put("guard1", "1_7");
					put("guard2", "1_7");
					put("arwen", "1_30");
					put("gwemegil", "1_47");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WalksinTwilightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Walks in Twilight
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 2
		* Type: condition
		* Subtype: Support Area
		* Game Text: Each time a twilight Nazgul wins a skirmish, you may exert a twilight Nazgul to shuffle a [ringwraith] walks from your discard pile into your draw deck.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var walks = scn.GetFreepsCard("walks");

		assertTrue(walks.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, walks.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, walks.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, walks.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, walks.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(walks, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(2, walks.getBlueprint().getTwilightCost());
		//assertEquals(, walks.getBlueprint().getStrength());
		//assertEquals(, walks.getBlueprint().getVitality());
		//assertEquals(, walks.getBlueprint().getResistance());
		//assertEquals(Signet., walks.getBlueprint().getSignet());
		//assertEquals(, walks.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void EachTwigulSkirmishVictoryShufflesARingwraithCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionToTable(guard1, guard2, arwen);
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("gwemegil"));

		var walks = scn.GetShadowCard("walks");
		var twigul1 = scn.GetShadowCard("twigul1");
		var twigul2 = scn.GetShadowCard("twigul2");
		var nazgul = scn.GetShadowCard("nazgul");
		var filler1 = scn.GetShadowCard("filler1");
		scn.MoveCardsToSupportArea(walks);
		scn.MoveMinionsToTable(twigul1, twigul2, nazgul);
		scn.MoveCardsToDiscard(filler1);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard1, twigul1},
				new PhysicalCardImpl[]{arwen, twigul2},
				new PhysicalCardImpl[]{guard2, nazgul});

		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowDecisionAvailable(""));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(0, scn.GetWoundsOn(twigul1));
		assertEquals(Zone.DISCARD, filler1.getZone());
		scn.ShadowChooseCard(twigul1);
		assertEquals(1, scn.GetWoundsOn(twigul1));
		assertEquals(Zone.DECK, filler1.getZone());

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(11, scn.GetStrength(arwen));
		assertEquals(10, scn.GetStrength(twigul2));
		scn.PassCurrentPhaseActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(guard2);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
