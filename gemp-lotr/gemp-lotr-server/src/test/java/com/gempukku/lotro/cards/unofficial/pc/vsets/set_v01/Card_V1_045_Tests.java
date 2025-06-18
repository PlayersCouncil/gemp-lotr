
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

public class Card_V1_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("gaze", "101_45");
					put("cond", "1_242");
					put("selfdiscard", "7_268");
					put("orc", "1_270");

					put("gandalf", "1_72");
					put("sleep", "1_84");
					put("introspection", "12_29");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GazeoftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Gaze of the Eye
		* Side: Free Peoples
		* Culture: sauron
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: Response: If your [Sauron] condition is about to be discarded, remove a burden to prevent this.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gaze = scn.GetFreepsCard("gaze");

		assertTrue(gaze.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, gaze.getBlueprint().getSide());
		assertEquals(Culture.SAURON, gaze.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, gaze.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, gaze.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(gaze, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, gaze.getBlueprint().getTwilightCost());
		//assertEquals(, gaze.getBlueprint().getStrength());
		//assertEquals(, gaze.getBlueprint().getVitality());
		//assertEquals(, gaze.getBlueprint().getResistance());
		//assertEquals(Signet., gaze.getBlueprint().getSignet());
		//assertEquals(, gaze.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void GazeProtectsNothingWithNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gaze = scn.GetShadowCard("gaze");
		PhysicalCardImpl cond = scn.GetShadowCard("cond");
		scn.MoveCardsToSupportArea(gaze, cond);

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl sleep1 = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep1);

		scn.StartGame();
		scn.RemoveBurdens(1); // to compensate for the starting bid burden
		scn.FreepsPlayCard(sleep1);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void GazeProtectsOtherConditions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gaze = scn.GetShadowCard("gaze");
		PhysicalCardImpl cond = scn.GetShadowCard("cond");
		scn.MoveCardsToSupportArea(gaze, cond);

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl introspection = scn.GetFreepsCard("introspection");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(introspection);

		scn.StartGame();
		scn.FreepsPlayCard(introspection);
		//scn.ShadowDeclineOptionalTrigger();
		scn.ShadowChooseCard(cond);

		assertEquals(1, scn.GetBurdens()); // from initial starting bid
		assertEquals(Zone.SUPPORT, cond.getZone());
		assertEquals(Zone.SUPPORT, gaze.getZone());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens()); // from initial starting bid
		assertEquals(Zone.SUPPORT, cond.getZone());
		assertEquals(Zone.SUPPORT, gaze.getZone());
	}

	@Test
	public void GazeProtectsSelf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gaze = scn.GetShadowCard("gaze");
		PhysicalCardImpl cond = scn.GetShadowCard("cond");
		scn.MoveCardsToSupportArea(gaze, cond);

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl introspection = scn.GetFreepsCard("introspection");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(introspection);

		scn.StartGame();
		scn.FreepsPlayCard(introspection);
		//scn.ShadowDeclineOptionalTrigger();
		scn.ShadowChooseCard(gaze);

		assertEquals(1, scn.GetBurdens()); // from initial starting bid
		assertEquals(Zone.SUPPORT, cond.getZone());
		assertEquals(Zone.SUPPORT, gaze.getZone());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens()); // from initial starting bid
		assertEquals(Zone.SUPPORT, cond.getZone());
		assertEquals(Zone.SUPPORT, gaze.getZone());
	}

	@Test
	public void GazeProtectsAgainstNukeWithEnoughBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gaze = scn.GetShadowCard("gaze");
		PhysicalCardImpl cond = scn.GetShadowCard("cond");
		scn.MoveCardsToSupportArea(gaze, cond);

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		scn.AddBurdens(1);

		scn.FreepsPlayCard(sleep);
		//scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetBurdens());
		assertEquals(Zone.SUPPORT, cond.getZone());
		assertEquals(Zone.SUPPORT, gaze.getZone());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		scn.ShadowChooseCard(gaze);
		assertEquals(1, scn.GetBurdens()); // from initial starting bid
		assertEquals(Zone.SUPPORT, cond.getZone());
		assertEquals(Zone.SUPPORT, gaze.getZone());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens()); // from initial starting bid
		assertEquals(Zone.SUPPORT, cond.getZone());
		assertEquals(Zone.SUPPORT, gaze.getZone());
	}

	@Test
	public void GazeProtectsAgainstSelfDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gaze = scn.GetShadowCard("gaze");
		PhysicalCardImpl selfdiscard = scn.GetShadowCard("selfdiscard");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToSupportArea(gaze,selfdiscard);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.SUPPORT, selfdiscard.getZone());

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(selfdiscard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens()); // from initial starting bid
		assertEquals(Zone.SUPPORT, selfdiscard.getZone());

	}
}
