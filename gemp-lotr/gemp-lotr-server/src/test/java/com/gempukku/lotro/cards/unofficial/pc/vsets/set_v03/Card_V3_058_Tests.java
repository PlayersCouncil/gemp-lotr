package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sands", "103_58");
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
	public void ShiftingBattlelinesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shifting Battle-lines
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the start of each skirmish involving a minion with ambush, you may remove (2) to hinder a
		 * 		Free Peoples condition.
		 * 		Each time your Southron Man dies, you may remove (1) to stack a Southron from your discard pile on a
		 * 		[raider] item that already has a Southron stacked on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sands");

		assertEquals("Shifting Battle-lines", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShiftingBattlelinesHindersAFreepsConditionEachTimeAmbushTwilightIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
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

		//Old version used to hinder at the time of ambush twilight being added
		assertFalse(scn.ShadowDecisionAvailable("Choose cards to hinder"));

		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to hinder"));
		assertTrue(scn.ShadowHasCardChoicesAvailable(condition1, condition2));
		assertFalse(scn.IsHindered(condition1));
		assertFalse(scn.IsHindered(condition2));

		scn.ShadowChooseCard(condition1);

		assertTrue(scn.IsHindered(condition1));
		assertFalse(scn.IsHindered(condition2));

	}
}
