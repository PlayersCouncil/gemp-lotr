package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_157_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("rush", "11_157");
					put("scout", "10_78");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void RushofSteedsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rush of Steeds
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [rohan] Man.
		 * Each time a Shadow condition is played, you may exert a minion.
		 * <b>Response:</b> If a minion exerts as a cost of its special ability, discard this condition to prevent that and return that minion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rush");

		assertEquals("Rush of Steeds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RushofSteedsDoesntCrashWhenUsedAgainstAdvanceScout() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rush = scn.GetFreepsCard("rush");
		scn.FreepsMoveCardToSupportArea(rush);

		var scout = scn.GetShadowCard("scout");
		scn.ShadowMoveCharToTable(scout);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);
		scn.ShadowChooseMultipleChoiceOption("exert");

		assertEquals(Zone.SUPPORT, rush.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, scout.getZone());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(Zone.DISCARD, rush.getZone());
		assertEquals(Zone.HAND, scout.getZone());
	}
}
