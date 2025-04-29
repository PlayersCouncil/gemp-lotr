package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
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
		 * Name: Merry, Of Buckland
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Assignment: Exert Merry twice to prevent a minion from being assigned to a skirmish until the regroup phase.  The Shadow player may exhaust that minion to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Of Buckland", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}

	@Test
	public void MerryExertsTwiceToPreventMinionSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.FreepsMoveCharToTable(merry);

		var shelob = scn.GetShadowCard("shelob");
		scn.ShadowMoveCharToTable(shelob);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
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
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.FreepsMoveCharToTable(merry);

		var shelob = scn.GetShadowCard("shelob");
		scn.ShadowMoveCharToTable(shelob);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exhaust"));

		scn.ShadowChooseYes();
		assertEquals(7, scn.GetWoundsOn(shelob));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

//	@Test
//	public void ExhaustedMinionCannotExertToPreventAbility() throws DecisionResultInvalidException, CardNotFoundException {
//		//Pre-game setup
//		var scn = GetScenario();
//
//		var merry = scn.GetFreepsCard("merry");
//		scn.FreepsMoveCharToTable(merry);
//
//		var shelob = scn.GetShadowCard("shelob");
//		scn.ShadowMoveCharToTable(shelob);
//
//		scn.StartGame();
//
//		scn.SkipToPhase(Phase.ASSIGNMENT);
//		assertEquals(0, scn.GetWoundsOn(merry));
//		assertEquals(0, scn.GetWoundsOn(shelob));
//		assertTrue(scn.FreepsActionAvailable(merry));
//
//		scn.FreepsUseCardAction(merry);
//		assertEquals(2, scn.GetWoundsOn(merry));
//		assertTrue(scn.ShadowDecisionAvailable("Would you like to exhaust"));
//
//		scn.ShadowChooseYes();
//		assertEquals(7, scn.GetWoundsOn(shelob));
//
//		scn.PassAssignmentActions();
//		//Cheating to do it again during fierce
//		scn.RemoveWoundsFromChar(merry, 2);
//
//		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
//		scn.FreepsDeclineAssignments();
//		scn.ShadowDeclineAssignments();
//
//		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
//	}
}
