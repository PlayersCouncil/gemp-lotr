package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

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

public class Card_15_064_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("madril", "65_64");
					put("arwen", "1_30");
					put("ranger1", "7_116");
					put("ranger2", "7_116");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MadrilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 15
		* Title: *Madril, Defender of Osgiliath
		* Side: Free Peoples
		* Culture: Gondor
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Man
		* Strength: 5
		* Vitality: 3
		* Resistance: 6
		* Game Text: <b>Ranger. Hunter 1.</b>
		* 	While you can spot 2 [gondor] rangers, Madril is twilight cost -2.
		* 	At the start of the maneuver phase, you may spot a threat to make each minion's site number +1 until the regroup phase.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl madril = scn.GetFreepsCard("madril");

		assertTrue(madril.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, madril.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, madril.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, madril.getBlueprint().getCardType());
		assertEquals(Race.MAN, madril.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(madril, Keyword.RANGER));
		assertTrue(scn.HasKeyword(madril, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(madril, Keyword.HUNTER));
		assertEquals(2, madril.getBlueprint().getTwilightCost());
		assertEquals(5, madril.getBlueprint().getStrength());
		assertEquals(3, madril.getBlueprint().getVitality());
		assertEquals(6, madril.getBlueprint().getResistance());
		//assertEquals(Signet., madril.getBlueprint().getSignet());
		//assertEquals(, madril.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void MadrilCosts2LessWith2GondorRangers() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var arwen = scn.GetFreepsCard("arwen");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCardsToHand(madril, arwen, ranger1, ranger2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(madril);
		assertEquals(2, scn.GetTwilight());

		scn.MoveCardsToHand(madril);
		scn.SetTwilight(0);
		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPlayCard(madril);
		assertEquals(4, scn.GetTwilight()); // no discount from arwen

		scn.MoveCardsToHand(madril);
		scn.SetTwilight(0);
		scn.FreepsPlayCard(ranger1);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPlayCard(madril);
		assertEquals(4, scn.GetTwilight()); // no discount from arwen + gondor ranger

		scn.MoveCardsToHand(madril);
		scn.SetTwilight(0);
		scn.FreepsPlayCard(ranger2);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPlayCard(madril);
		assertEquals(2, scn.GetTwilight()); // discount from 2 gondor rangers
	}

	@Test
	public void ManeuverAbilityAllowsDistributionOfSiteNumberIncreases() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(madril);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveMinionsToTable(runner1, runner2, runner3);

		scn.StartGame();

		scn.AddThreats(5);

		assertEquals(4, scn.GetMinionSiteNumber(runner1));
		assertEquals(4, scn.GetMinionSiteNumber(runner2));
		assertEquals(4, scn.GetMinionSiteNumber(runner3));


		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetThreats());

		scn.FreepsChooseCard(runner1);
		scn.FreepsChooseCard(runner1);
		scn.FreepsChooseCard(runner1);
		scn.FreepsChooseCard(runner2);
		scn.FreepsChooseCard(runner2);

		assertEquals(7, scn.GetMinionSiteNumber(runner1));
		assertEquals(6, scn.GetMinionSiteNumber(runner2));
		assertEquals(4, scn.GetMinionSiteNumber(runner3));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(7, scn.GetMinionSiteNumber(runner1));
		assertEquals(6, scn.GetMinionSiteNumber(runner2));
		assertEquals(4, scn.GetMinionSiteNumber(runner3));

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(4, scn.GetMinionSiteNumber(runner1));
		assertEquals(4, scn.GetMinionSiteNumber(runner2));
		assertEquals(4, scn.GetMinionSiteNumber(runner3));
	}

	@Test
	public void ManeuverAbilityDoesNothingWithZeroThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		scn.MoveCardsToHand(madril);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveMinionsToTable(runner1, runner2, runner3);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsDecisionAvailable("Play Maneuver action or Pass"));
	}
}
