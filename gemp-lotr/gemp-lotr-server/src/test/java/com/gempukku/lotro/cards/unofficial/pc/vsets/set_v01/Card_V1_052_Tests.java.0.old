
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_052_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("merry", "101_52");
					put("shelob", "8_26");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Merry, Of Buckland
		* Side: Free Peoples
		* Culture: shire
		* Twilight Cost: 1
		* Type: companion
		* Subtype: Hobbit
		* Strength: 3
		* Vitality: 4
		* Signet: Frodo
		* Game Text: Assignment: Exert Merry 3 times to prevent a minion from being assigned to a skirmish until the regroup phase.  The Shadow player may exhaust that minion to prevent this.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl merry = scn.GetFreepsCard("merry");

		assertTrue(merry.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, merry.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, merry.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, merry.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, merry.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(merry, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, merry.getBlueprint().getTwilightCost());
		assertEquals(3, merry.getBlueprint().getStrength());
		assertEquals(4, merry.getBlueprint().getVitality());
		//assertEquals(, merry.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, merry.getBlueprint().getSignet());
		//assertEquals(, merry.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void MerryExertsThriceToPreventMinionSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		scn.FreepsMoveCharToTable(merry);

		PhysicalCardImpl shelob = scn.GetShadowCard("shelob");
		scn.ShadowMoveCharToTable(shelob);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.ShadowChooseNo();
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		//immediately skips to the fierce skirmish
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void MinionCanExhaustToPreventAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		scn.FreepsMoveCharToTable(merry);

		PhysicalCardImpl shelob = scn.GetShadowCard("shelob");
		scn.ShadowMoveCharToTable(shelob);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exhaust"));

		scn.ShadowChooseYes();
		assertEquals(7, scn.GetWoundsOn(shelob));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}
}
