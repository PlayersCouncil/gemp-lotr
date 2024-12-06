package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_112_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<String, String>()
				{{
					put("troll", "15_112");
					put("orc1", "13_118");
					put("orc2", "13_118");
					put("orc3", "13_118");
					put("orc4", "13_118");
					put("orc5", "13_118");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void MountaintrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mountain-troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 22
		 * Vitality: 6
		 * Site Number: 5
		 * Game Text: When you play this minion, you may discard 5 [orc] minions from play to make it twilight cost
		 *   -10 and <b>fierce</b>.
		 *   <b>Shadow:</b> Remove (3) to play an [orc] Orc from your discard pile. Its twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Mountain-troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(22, card.getBlueprint().getStrength());
		assertEquals(6, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OnPlayTrollDoesNothingIfLessThan5OrcMinionsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var orc4 = scn.GetShadowCard("orc4");
		var orc5 = scn.GetShadowCard("orc5");
		scn.ShadowMoveCardToHand(troll);
		scn.ShadowMoveCharToTable(orc1, orc2, orc3, orc4);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(troll);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void OnPlayTrollOptionallDiscards5OrcMinionsForMinus10TwilightAndFierceUntilEndOfTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var orc4 = scn.GetShadowCard("orc4");
		var orc5 = scn.GetShadowCard("orc5");
		scn.ShadowMoveCardToHand(troll);
		scn.ShadowMoveCharToTable(orc1, orc2, orc3, orc4, orc5);

		scn.StartGame();

		scn.SetTwilight(17);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(20, scn.GetTwilight());
		scn.ShadowPlayCard(troll);

		assertTrue(scn.ShadowDecisionAvailable("choose cards to discard"));
		assertFalse(scn.hasKeyword(troll, Keyword.FIERCE));

		scn.ShadowChooseYes();
		assertTrue(scn.hasKeyword(troll, Keyword.FIERCE));
		assertEquals(Zone.DISCARD, orc1.getZone());
		assertEquals(Zone.DISCARD, orc2.getZone());
		assertEquals(Zone.DISCARD, orc3.getZone());
		assertEquals(Zone.DISCARD, orc4.getZone());
		assertEquals(Zone.DISCARD, orc5.getZone());
		assertEquals(18, scn.GetTwilight()); //20 initial -2 for roaming, troll was otherwise free

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.hasKeyword(troll, Keyword.FIERCE));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.hasKeyword(troll, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertTrue(scn.hasKeyword(troll, Keyword.FIERCE));
	}

	@Test
	public void ShadowAbilityRemoves3AndPlaysDiscountedOrcFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var orc4 = scn.GetShadowCard("orc4");
		var orc5 = scn.GetShadowCard("orc5");
		scn.ShadowMoveCharToTable(troll);
		scn.ShadowMoveCardToDiscard(orc1, orc2, orc3, orc4, orc5);

		scn.StartGame();

		scn.SetTwilight(17);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(20, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(troll));
		assertEquals(Zone.DISCARD, orc1.getZone());

		scn.ShadowUseCardAction(troll);
		assertEquals(5, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(orc1);

		// -3 for ability, -3 for orc, +2 for discount, -2 for roaming
		assertEquals(14, scn.GetTwilight());
		assertEquals(Zone.SHADOW_CHARACTERS, orc1.getZone());
		assertEquals(0, scn.GetWoundsOn(orc1));
	}
}
