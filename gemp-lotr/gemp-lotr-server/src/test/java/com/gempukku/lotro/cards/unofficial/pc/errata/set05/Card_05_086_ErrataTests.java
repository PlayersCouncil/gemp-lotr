package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_086_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weary", "55_86");
					put("guard1", "5_83");      // Household Guard: [rohan] valiant Man companion, strength 6, vitality 3, no text
					put("guard2", "5_83");
					put("guard3", "5_83");
					put("elf", "68_9");         // Elven Defender: [elven] valiant Elf companion; strength +2 only with an [elven] token
					put("rohirrim", "7_246");   // Rohirrim Guard: [rohan] Man companion who is not valiant; a skirmish ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: minion, strength 5, vitality 1; its when-played text does nothing when cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoRestfortheWearyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: No Rest for the Weary
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot 3 valiant companions to make one of those companions <b>defender +1</b> until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weary");

		assertEquals("No Rest for the Weary", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAValiantCompanionDefenderPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetFreepsCard("weary");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var elf = scn.GetFreepsCard("elf");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var frodo = scn.GetRingBearer();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(guard1, guard2, guard3, elf, rohirrim);
		scn.MoveCardsToHand(weary);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertFalse(scn.HasKeyword(guard1, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(weary));

		scn.FreepsPlayCard(weary);
		assertEquals(twilight + 1, scn.GetTwilight());
		// Any of the four valiant companions can be one of the three spotted; the Rohirrim Guard and Frodo are not valiant.
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2, guard3, elf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rohirrim, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(guard1);

		assertInDiscard(weary);
		assertEquals(1, scn.GetKeywordCount(guard1, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(guard2, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(guard3, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(elf, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.SkipToAssignments();
		// Defender +1 lets the first Household Guard take on two minions.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard1, runner1, runner2},
				new PhysicalCardImpl[]{guard2, runner3}
		);
		assertTrue(scn.IsCharAssignedAgainst(guard1, runner1));
		assertTrue(scn.IsCharAssignedAgainst(guard1, runner2));

		// The other skirmish first: strength 6 against 5, and the Goblin Runner dies.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertInDiscard(runner3);

		scn.FreepsResolveSkirmish(guard1);
		assertEquals(1, scn.GetKeywordCount(guard1, Keyword.DEFENDER));
		scn.PassSkirmishActions();
		// Strength 6 against 5 + 5: one wound.
		assertEquals(1, scn.GetWoundsOn(guard1));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(guard1, Keyword.DEFENDER));
	}

	@Test
	public void NotPlayableWithTwoValiantCompanionsAndARohanCompanionWhoIsNotValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetFreepsCard("weary");
		var guard1 = scn.GetFreepsCard("guard1");
		var elf = scn.GetFreepsCard("elf");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(guard1, elf, rohirrim);
		scn.MoveCardsToHand(weary);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(weary));
	}
}
