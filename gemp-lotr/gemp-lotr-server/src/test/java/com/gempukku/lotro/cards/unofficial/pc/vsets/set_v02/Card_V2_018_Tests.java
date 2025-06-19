package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("menu", "102_18");
					put("uruk", "2_47");
					put("orc", "1_261");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
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
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BackontheMenuDiscardsASauronTrackerToMakeAnIsengardTrackerStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var menu = scn.GetShadowCard("menu");
		var uruk = scn.GetShadowCard("uruk");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(menu);
		scn.MoveMinionsToTable(uruk, orc);

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
		scn.ShadowChooseAction("Discard");

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
		scn.MoveCardsToSupportArea(menu);
		scn.MoveMinionsToTable(uruk, orc);

		var frodo = scn.GetRingBearer();

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(uruk));
		assertFalse(scn.HasKeyword(orc, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable("Exert"));
		scn.ShadowChooseAction("Exert");

		assertEquals(1, scn.GetWoundsOn(uruk));
		assertTrue(scn.HasKeyword(orc, Keyword.FIERCE));

		//Have to make sure fierce persists or it won't matter lol
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments(); //ring
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}
}
