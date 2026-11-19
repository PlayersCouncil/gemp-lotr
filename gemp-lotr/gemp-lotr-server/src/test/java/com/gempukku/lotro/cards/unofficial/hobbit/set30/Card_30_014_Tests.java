package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_014_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("noble", "30_14");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					// Gandalf, Friend of Thorin: a [gandalf] Wizard companion, 7/4; a fellowship ability and texts
					// about [dwarven] followers and Thorin, none of them in play
					put("gandalf", "30_26");
					// Dain Ironfoot, Lord of the Iron Hills: a [dwarven] Dwarf ALLY, vitality 3; a start-of-regroup
					// offer that wants a [dwarven] follower, and none is in play
					put("dain", "32_2");
					put("runner1", "31_30");    // Goblin Runner: [moria] Orc, strength 5; its when-played text is not used (cheated in)
					put("runner2", "31_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NobleintentionsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Noble intentions
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a companion (except Bilbo) to make Bilbo strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("noble");

		assertEquals("Noble intentions", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsACompanionOtherThanBilboToMakeBilboStrengthPlus3UntilHisSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var noble = scn.GetFreepsCard("noble");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin, gandalf);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(noble);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertInHand(noble);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, runner1}, new PhysicalCardImpl[]{balin, runner2});

		// Bilbo's skirmish first; Balin's follows it.
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertInHand(noble);
		assertTrue(scn.FreepsPlayAvailable(noble));

		scn.FreepsPlayCard(noble);
		// A Dwarf and a Wizard companion may pay; Bilbo is excepted, and Dain is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);

		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
		// 3 + 3
		assertEquals(6, scn.GetStrength(bilbo));
		assertEquals(7, scn.GetStrength(balin));
		assertInDiscard(noble);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus names no duration, so it lasts this skirmish phase: gone in Balin's skirmish, the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, runner2));
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInAnotherCompanionsSkirmishItStillMakesBilboStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var noble = scn.GetFreepsCard("noble");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin, gandalf);
		scn.MoveCardsToHand(noble);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		assertInHand(noble);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner1);
		scn.FreepsResolveSkirmish(balin);

		// The text names Bilbo, not a skirmishing character: Bilbo, in no skirmish, is still made stronger.
		assertFalse(scn.IsCharAssigned(bilbo));
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(noble);
		scn.FreepsPlayCard(noble);
		scn.FreepsChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(bilbo));
		assertEquals(7, scn.GetStrength(balin));
		assertInDiscard(noble);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyCompanionButBilboIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var noble = scn.GetFreepsCard("noble");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(noble);
		// Vitality 3 with 2 wounds: Balin cannot exert.
		scn.AddWoundsToChar(balin, 2);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertInHand(noble);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner1);
		scn.FreepsResolveSkirmish(bilbo);

		// Bilbo and the ally Dain could exert, but neither may pay.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(noble));
		assertInHand(noble);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(3, scn.GetStrength(bilbo));
	}
}
