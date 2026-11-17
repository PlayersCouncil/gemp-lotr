package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_187_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("furor", "11_187");

					put("watchman", "11_206");  // Watchman Uruk: an [uruk-hai] Uruk-hai, strength 8, vitality 2; damage +1 and a trigger on its own play, not reached by a cheat
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] Wizard (no Uruk-hai), strength 8; each companion resistance -1, and a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 only
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc of vitality 1, so always exhausted; its play trigger is not reached by a cheat

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4; a maneuver ability only
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FurorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Furor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [uruk-hai] minion strength +2 (or +3 if you can spot an exhausted companion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("furor");

		assertEquals("Furor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnUrukHaiMinionPlusTwoWithNoExhaustedCompanionForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetWoundsOn(bounder));
		// Merry is wounded twice but not exhausted: 2 vitality left.
		scn.AddWoundsToChar(merry, 2);
		assertEquals(2, scn.GetWoundsOn(merry));

		var furor = scn.GetShadowCard("furor");
		var watchman = scn.GetShadowCard("watchman");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToHand(furor);
		scn.MoveMinionsToTable(watchman, saruman, guard, runner);
		assertInHand(furor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, watchman}, new PhysicalCardImpl[]{merry, guard});
		// Saruman and the Runner stay out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The exhausted characters are an ally (vitality 2, one wound) and a minion (vitality 1): no companion.  Merry
		// has 2 vitality left, wounded and not exhausted.
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(1, scn.GetVitality(runner));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(2, scn.GetVitality(merry));
		assertEquals(8, scn.GetStrength(watchman));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.ShadowPlayAvailable(furor));

		scn.ShadowPlayCard(furor);
		// An [uruk-hai] Uruk-hai in this skirmish and an [uruk-hai] Wizard in none; not a [sauron] Uruk-hai, a
		// [moria] Orc or a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(watchman, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, runner, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(watchman);

		// 8 + 2: no exhausted companion to spot.
		assertInDiscard(furor);
		assertEquals(10, scn.GetStrength(watchman));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(watchman));
		scn.ShadowPass();

		// Aragorn's 8 loses to 10; the bonus is gone once the next skirmish has begun.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(merry);
		assertEquals(8, scn.GetStrength(watchman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnExhaustedGondorManCompanionTheMinionIsPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		var furor = scn.GetShadowCard("furor");
		var watchman = scn.GetShadowCard("watchman");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveCardsToHand(furor);
		scn.MoveMinionsToTable(watchman, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, watchman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPass();

		// Aragorn, in no skirmish, has 1 vitality left.
		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(8, scn.GetStrength(saruman));

		scn.ShadowPlayCard(furor);
		scn.ShadowChooseCard(saruman);

		// 8 + 3, though Saruman skirmishes nobody (the text says "an [uruk-hai] minion", not a skirmishing one).
		assertInDiscard(furor);
		assertEquals(11, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(watchman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnExhaustedShireHobbitCompanionTheMinionIsPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));

		var furor = scn.GetShadowCard("furor");
		var watchman = scn.GetShadowCard("watchman");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveCardsToHand(furor);
		scn.MoveMinionsToTable(watchman, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, watchman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Merry, in no skirmish, has 1 vitality left.
		assertEquals(1, scn.GetVitality(merry));
		assertEquals(8, scn.GetStrength(watchman));

		scn.ShadowPlayCard(furor);
		scn.ShadowChooseCard(watchman);

		// 8 + 3.
		assertInDiscard(furor);
		assertEquals(11, scn.GetStrength(watchman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayedInTheAssignmentPhaseOnlyInASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var furor = scn.GetShadowCard("furor");
		var watchman = scn.GetShadowCard("watchman");

		scn.StartGame();
		scn.MoveCardsToHand(furor);
		scn.MoveMinionsToTable(watchman);
		assertInHand(furor);

		// The Shadow player's assignment actions, his last chance to act before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(furor));
		scn.ShadowPass();

		scn.FreepsAssignAndResolve(aragorn, watchman);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(furor));
	}

	@Test
	public void WithNoUrukHaiCultureMinionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var furor = scn.GetShadowCard("furor");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveCardsToHand(furor);
		scn.MoveMinionsToTable(guard);
		assertInHand(furor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));

		// CR 4.2 "effect": a card may be played though it will have no effect.  Only a [sauron] Uruk-hai is there.
		assertTrue(scn.ShadowPlayAvailable(furor));
		scn.ShadowPlayCard(furor);

		assertInDiscard(furor);
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
