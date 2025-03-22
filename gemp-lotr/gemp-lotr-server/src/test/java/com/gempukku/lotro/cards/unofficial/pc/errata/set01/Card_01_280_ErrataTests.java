package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_280_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("lt", "51_280");
					put("filler1", "1_281");
					put("filler2", "1_282");


					put("sam", "1_311");
					put("aragorn", "1_89");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TowerLieutenantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Tower Lieutenant
		* Unique: True
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 4
		* Type: minion
		* Subtype: Orc
		* Strength: 10
		* Vitality: 3
		* Site Number: 6
		* Game Text: Each time this minion wins a skirmish, you may exhaust a companion (except the Ring-bearer).
		 * The Free Peoples player may discard 2 cards at random from hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var lt = scn.GetFreepsCard("lt");

		assertTrue(lt.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, lt.getBlueprint().getSide());
		assertEquals(Culture.SAURON, lt.getBlueprint().getCulture());
		assertEquals(CardType.MINION, lt.getBlueprint().getCardType());
		assertEquals(Race.ORC, lt.getBlueprint().getRace());
		assertEquals(4, lt.getBlueprint().getTwilightCost());
		assertEquals(10, lt.getBlueprint().getStrength());
		assertEquals(3, lt.getBlueprint().getVitality());
		assertEquals(6, lt.getBlueprint().getSiteNumber());
	}

	@Test
	public void TowerLieutenantWinningASkirmishExhaustsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		scn.ShadowMoveCharToTable(lt);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);
		scn.FreepsMoveCardToHand("filler1");

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void TowerLieutenantOffersChoiceOfNonRBCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		scn.ShadowMoveCharToTable(lt);

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(aragorn, sam);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.ShadowGetCardChoiceCount());
	}

	@Test
	public void TowerLieutenantExhaustCanBePreventedByDiscarding2RandomCardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		scn.ShadowMoveCharToTable(lt);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);
		scn.FreepsMoveCardToHand("filler1", "filler2");

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent exhausting"));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
	}
}
