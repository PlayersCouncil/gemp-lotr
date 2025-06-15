
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

public class Card_V1_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("myheart", "101_16");
					put("gandalf", "1_364");
					put("greenleaf", "1_50");

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
	public void MyHeartTellsMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *My Heart Tells Me
		* Side: Free Peoples
		* Culture: gandalf
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: Each time a minion is about to take a wound, you may spot Gandalf to prevent that wound and heal a companion (limit once per turn).
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl myheart = scn.GetFreepsCard("myheart");

		assertTrue(myheart.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, myheart.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, myheart.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, myheart.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, myheart.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(myheart, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, myheart.getBlueprint().getTwilightCost());
		//assertEquals(, myheart.getBlueprint().getStrength());
		//assertEquals(, myheart.getBlueprint().getVitality());
		//assertEquals(, myheart.getBlueprint().getResistance());
		//assertEquals(Signet., myheart.getBlueprint().getSignet());
		//assertEquals(, myheart.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void MyHeartSpotsGandalfToTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl myheart = scn.GetFreepsCard("myheart");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl greenleaf = scn.GetFreepsCard("greenleaf");

		scn.MoveCardsToHand(gandalf);
		scn.MoveCompanionToTable(greenleaf);
		scn.MoveCardsToSupportArea(myheart);

		PhysicalCardImpl runner1 = scn.GetShadowCard("runner1");
		PhysicalCardImpl runner2 = scn.GetShadowCard("runner2");

		scn.MoveMinionsToTable(runner1, runner2);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsUseCardAction(greenleaf);
		scn.FreepsChooseCard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.MoveCompanionToTable(gandalf);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(greenleaf);
		//scn.FreepsChooseCard(runner2); //Only one choice, so it's made for us

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void MyHeartConvertsWoundToHealLimitOncePerTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl myheart = scn.GetFreepsCard("myheart");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl greenleaf = scn.GetFreepsCard("greenleaf");

		scn.MoveCardsToHand(gandalf);
		scn.MoveCompanionToTable(greenleaf, gandalf);
		scn.MoveCardsToSupportArea(myheart);

		PhysicalCardImpl runner1 = scn.GetShadowCard("runner1");
		PhysicalCardImpl runner2 = scn.GetShadowCard("runner2");

		scn.MoveMinionsToTable(runner1, runner2);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(greenleaf));

		scn.FreepsUseCardAction(greenleaf);
		scn.FreepsChooseCard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(1, scn.GetWoundsOn(greenleaf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(greenleaf));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(greenleaf);
		scn.FreepsChooseCard(runner1);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(greenleaf));
	}
}
