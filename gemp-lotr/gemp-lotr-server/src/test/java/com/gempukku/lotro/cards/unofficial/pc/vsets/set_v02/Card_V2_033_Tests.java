package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_033_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("grumbler", "102_33");
					put("orc", "1_261");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void UrukGrumblerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uruk Grumbler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: Tracker. Fierce.
		* 	Each time this minion is assigned to skirmish a companion, you may spot a [sauron] tracker
		 * 	and discard this minion to wound that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grumbler");

		assertEquals("Uruk Grumbler", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.hasKeyword(card, Keyword.FIERCE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukGrumblerSpotsASauronTrackerAndSelfDiscardsToWoundCompanionUponAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grumbler = scn.GetShadowCard("grumbler");
		var orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCharToTable(grumbler, orc);

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, grumbler);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Zone.SHADOW_CHARACTERS, grumbler.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.DISCARD, grumbler.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
		assertEquals(1, scn.GetWoundsOn(frodo));
	}
}
