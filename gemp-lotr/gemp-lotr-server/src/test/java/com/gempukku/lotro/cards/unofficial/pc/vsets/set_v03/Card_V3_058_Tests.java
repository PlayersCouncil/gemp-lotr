package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sands", "103_56");
					put("scout", "4_252");

					put("runner", "1_178");

					put("condition1", "1_16");
					put("condition2", "1_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShiftingSandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shifting Sands
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time ambush twilight is added, hinder a Free Peoples condition.
		* 	Each time your Southron Man dies, you may remove (1) to stack a Southron from your discard pile on a [raider] support item or [raider] minion that already has a Southron stacked on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sands");

		assertEquals("Shifting Sands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShiftingSandsHindersAFreepsConditionEachTimeAmbushTwilightIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var condition1 = scn.GetFreepsCard("condition1");
		var condition2 = scn.GetFreepsCard("condition2");
		scn.MoveCardsToSupportArea(condition1, condition2);

		var sands = scn.GetShadowCard("sands");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(sands);
		scn.MoveMinionsToTable(scout);

		scn.StartGame();

		scn.SkipToAssignments();

		assertTrue(scn.HasKeyword(scout, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(scout, Keyword.AMBUSH));
		assertEquals(3, scn.GetTwilight());

		scn.FreepsAssignToMinions(frodo, scout);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowActionAvailable("Ambush - add 2"));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowDecisionAvailable("Choose cards to hinder"));
		assertTrue(scn.ShadowHasCardChoicesAvailable(condition1, condition2));
		assertFalse(scn.IsHindered(condition1));
		assertFalse(scn.IsHindered(condition2));

		scn.ShadowChooseCard(condition1);

		assertTrue(scn.IsHindered(condition1));
		assertFalse(scn.IsHindered(condition2));

	}

	@Test
	public void ShiftingSandsDoesNotRespondToNonAmbushTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var condition1 = scn.GetFreepsCard("condition1");
		var condition2 = scn.GetFreepsCard("condition2");
		scn.MoveCardsToSupportArea(condition1, condition2);

		var sands = scn.GetShadowCard("sands");

		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(sands);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(runner);
		
		assertFalse(scn.ShadowDecisionAvailable("Choose cards to hinder"));
	}
}
