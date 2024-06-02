
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_034_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("darkwaters", "101_34");
                    put("ftentacle1", "2_58");
                    put("ftentacle2", "2_58");
                    put("song", "3_5");
                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DarkWatersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: Out of Dark Waters
		 * Side: Free Peoples
		 * Culture: moria
		 * Twilight Cost: 0
		 * Type: condition
		 * Subtype: Support Area
		 * Game Text: Shadow: Remove (1) to stack a tentacle from hand here.
		 * 	Shadow: Remove (1) to play a tentacle from here as if from hand.
		 * 	Response: If this condition is about to be discarded, discard a tentacle stacked here to prevent that.
		 */

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl darkwaters = scn.GetFreepsCard("darkwaters");

		assertFalse(darkwaters.getBlueprint().isUnique());
		assertTrue(scn.hasKeyword(darkwaters, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(0, darkwaters.getBlueprint().getTwilightCost());
		assertEquals(CardType.CONDITION, darkwaters.getBlueprint().getCardType());
		assertEquals(Culture.MORIA, darkwaters.getBlueprint().getCulture());
		assertEquals(Side.SHADOW, darkwaters.getBlueprint().getSide());
	}

	@Test
	public void DarkWatersCanAdd1ToStackOrPlayTentacles() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl darkwaters = scn.GetShadowCard("darkwaters");
		PhysicalCardImpl ftentacle1 = scn.GetShadowCard("ftentacle1");
		PhysicalCardImpl ftentacle2 = scn.GetShadowCard("ftentacle2");
		scn.ShadowMoveCardToHand(darkwaters, ftentacle1, ftentacle2);

		scn.StartGame();
		scn.SetTwilight(4);
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(darkwaters);

		assertTrue(scn.ShadowActionAvailable("Dark Waters"));
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetStackedCards(darkwaters).size());
		scn.ShadowUseCardAction(darkwaters);
		scn.ShadowChooseCard(ftentacle1);

		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetStackedCards(darkwaters).size());
		assertTrue(scn.ShadowActionAvailable("Dark Waters"));
		scn.ShadowUseCardAction(darkwaters);
		assertEquals(2, scn.ShadowGetMultipleChoices().size());
		scn.ShadowChooseMultipleChoiceOption("stack");
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetStackedCards(darkwaters).size());

		assertTrue(scn.ShadowActionAvailable("Dark Waters"));
		scn.ShadowUseCardAction(darkwaters);
		scn.ShadowChooseCard(ftentacle1);
		assertEquals(1, scn.GetStackedCards(darkwaters).size());
		assertEquals(0, scn.GetTwilight()); // -1 for ability, -2 minion, -2 roaming
	}


	@Test
	public void DarkWatersCanBurnAStackedTentacleToPreventSelfDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl song = scn.GetFreepsCard("song");
		scn.FreepsMoveCardToSupportArea(song);

		PhysicalCardImpl darkwaters = scn.GetShadowCard("darkwaters");
		PhysicalCardImpl ftentacle1 = scn.GetShadowCard("ftentacle1");
		PhysicalCardImpl ftentacle2 = scn.GetShadowCard("ftentacle2");
		scn.ShadowMoveCardToSupportArea(darkwaters);
		//scn.ShadowMoveCardToHand(ftentacle1, ftentacle2);

		scn.StartGame();
		scn.StackCardsOn(darkwaters, ftentacle1, ftentacle2);

		scn.FreepsUseCardAction(song);
		scn.FreepsChooseCard(darkwaters);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(ftentacle1);
		assertEquals(Zone.DISCARD, ftentacle1.getZone());
		assertEquals(Zone.SUPPORT, darkwaters.getZone());

	}
}
