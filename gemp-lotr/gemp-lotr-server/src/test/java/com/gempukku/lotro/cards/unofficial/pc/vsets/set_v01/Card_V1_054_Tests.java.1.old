
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

public class Card_V1_054_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("sam", "101_54");
					put("boromir", "1_97");

					put("runner", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Sam, Of Bagshot Row
		* Side: Free Peoples
		* Culture: shire
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Hobbit
		* Strength: 3
		* Vitality: 4
		* Signet: Frodo
		* Game Text: Each time a companion with the Frodo signet wins a skirmish, you may exert Sam or add a burden to heal that companion.
		* 	Response: If Frodo is killed, make Sam the Ring-bearer (resistance 5).
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl sam = scn.GetFreepsCard("sam");

		assertTrue(sam.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sam.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, sam.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, sam.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, sam.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(sam, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(2, sam.getBlueprint().getTwilightCost());
		assertEquals(3, sam.getBlueprint().getStrength());
		assertEquals(4, sam.getBlueprint().getVitality());
		assertEquals(5, sam.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, sam.getBlueprint().getSignet());
		//assertEquals(, sam.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void EachTimeFrodoSignetWinsSkirmishSamCanExertToHealThem() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
		scn.FreepsMoveCharToTable(sam, boromir);

		PhysicalCardImpl runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();

		scn.AddWoundsToChar(boromir, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens()); //1 from the bidding

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("choose action"));
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		scn.FreepsChooseMultipleChoiceOption("exert");

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void EachTimeFrodoSignetWinsSkirmishSamCanAddBurdenToHealThem() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
		scn.FreepsMoveCharToTable(sam, boromir);

		PhysicalCardImpl runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();

		scn.AddWoundsToChar(boromir, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens()); //1 from the bidding

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("choose action"));
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		scn.FreepsChooseMultipleChoiceOption("burden");

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
	}
}
