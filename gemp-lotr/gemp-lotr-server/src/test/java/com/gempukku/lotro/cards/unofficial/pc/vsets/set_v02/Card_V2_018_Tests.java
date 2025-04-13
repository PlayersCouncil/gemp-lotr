package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_018_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("menu", "102_18");
					put("uruk", "2_47");
					put("orc", "1_261");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BackontheMenuStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Back on the Menu
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Skirmish: Discard a [Sauron] tracker to make an [Isengard] tracker strength +2.
		* 	Skirmish: Exert an [isengard] tracker to make a [sauron] tracker <b>fierce</b> until the regroup phase. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("menu");

		assertEquals("Back on the Menu", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BackontheMenuDiscardsASauronTrackerToMakeAnIsengardTrackerStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var menu = scn.GetShadowCard("menu");
		var uruk = scn.GetShadowCard("uruk");
		var orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToSupportArea(menu);
		scn.ShadowMoveCharToTable(uruk, orc);

		var frodo = scn.GetRingBearer();

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, uruk);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetStrength(uruk));
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
		assertTrue(scn.ShadowActionAvailable("Discard"));
		scn.ShadowUseCardAction("Discard");

		assertEquals(9, scn.GetStrength(uruk));
		assertEquals(Zone.DISCARD, orc.getZone());
	}

	@Test
	public void BackontheMenuExertsAnIsengardTrackerToMakeASauronTrackerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var menu = scn.GetShadowCard("menu");
		var uruk = scn.GetShadowCard("uruk");
		var orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToSupportArea(menu);
		scn.ShadowMoveCharToTable(uruk, orc);

		var frodo = scn.GetRingBearer();

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(uruk));
		assertFalse(scn.hasKeyword(orc, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable("Exert"));
		scn.ShadowUseCardAction("Exert");

		assertEquals(1, scn.GetWoundsOn(uruk));
		assertTrue(scn.hasKeyword(orc, Keyword.FIERCE));

		//Have to make sure fierce persists or it won't matter lol
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments(); //ring
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}
}
