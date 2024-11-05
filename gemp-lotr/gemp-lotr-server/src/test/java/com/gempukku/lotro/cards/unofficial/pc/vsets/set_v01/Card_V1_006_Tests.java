
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

public class Card_V1_006_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("bold", "101_6");
					put("legolas", "1_50");
					put("gimli", "1_13");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void SoBoldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *So Bold and So Courteous
		* Side: Free Peoples
		* Culture: dwarven
		* Twilight Cost: 0
		* Type: condition
		* Subtype: Support Area
		* Game Text: Each time the fellowship moves you may exert an Elf to heal Gimli.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl bold = scn.GetFreepsCard("bold");

		assertTrue(bold.getBlueprint().isUnique());
		assertTrue(scn.hasKeyword(bold, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(0, bold.getBlueprint().getTwilightCost());
		assertEquals(CardType.CONDITION, bold.getBlueprint().getCardType());
		assertEquals(Culture.DWARVEN, bold.getBlueprint().getCulture());
		assertEquals(Side.FREE_PEOPLE, bold.getBlueprint().getSide());
	}

	@Test
	public void SoBoldOffersDefenderEachMove() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl bold = scn.GetFreepsCard("bold");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl legolas = scn.GetFreepsCard("legolas");
		scn.FreepsMoveCardToHand(bold);
		scn.FreepsMoveCharToTable(gimli, legolas);

		scn.StartGame();
		scn.FreepsPlayCard(bold);

		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
	}
}
