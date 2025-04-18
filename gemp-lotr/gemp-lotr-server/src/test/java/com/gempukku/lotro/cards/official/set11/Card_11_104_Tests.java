package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_104_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("death", "11_104");
					put("mouth", "12_73");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void WhistlingDeathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Whistling Death
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Wound a character skirmishing a [men] minion.
		 * If the fellowship is at a battleground site, you may remove (3) to wound that character again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("death");

		assertEquals("Whistling Death", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhistlingDeathWoundsThenRemoves3MoreToWoundAgain() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetShadowCard("death");
		var mouth = scn.GetShadowCard("mouth");
		scn.ShadowMoveCharToTable(mouth);
		scn.ShadowMoveCardToHand(death);

		var frodo = scn.GetRingBearer();

		//cheating to ensure site 2 qualifies
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.BATTLEGROUND));

		scn.StartGame();

		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, mouth);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.hasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowPlayAvailable(death));

		scn.ShadowPlayCard(death);
		scn.FreepsDeclineOptionalTrigger(); //the one ring
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(7, scn.GetTwilight());

		assertTrue(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		scn.ShadowChooseYes();
		scn.FreepsDeclineOptionalTrigger(); //the one ring
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetTwilight());
	}

	@Test
	public void WhistlingDeathSecondWoundIsNotOfferedAtNonBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetShadowCard("death");
		var mouth = scn.GetShadowCard("mouth");
		scn.ShadowMoveCharToTable(mouth);
		scn.ShadowMoveCardToHand(death);

		var frodo = scn.GetRingBearer();

		scn.StartGame();

		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, mouth);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.hasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowPlayAvailable(death));

		scn.ShadowPlayCard(death);
		scn.FreepsDeclineOptionalTrigger(); //the one ring
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(7, scn.GetTwilight());

		assertFalse(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void WhistlingDeathSecondWoundIsNotOfferedWhenLessThan3TwilightAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetShadowCard("death");
		var mouth = scn.GetShadowCard("mouth");
		scn.ShadowMoveCharToTable(mouth);
		scn.ShadowMoveCardToHand(death);

		var frodo = scn.GetRingBearer();

		//cheating to ensure site 2 qualifies
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.BATTLEGROUND));

		scn.StartGame();

		scn.SetTwilight(2);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, mouth);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.hasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowPlayAvailable(death));

		scn.ShadowPlayCard(death);
		scn.FreepsDeclineOptionalTrigger(); //the one ring
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetTwilight());

		assertFalse(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}
}
