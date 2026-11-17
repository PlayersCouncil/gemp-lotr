package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_17_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horse", "17_103");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside 3 [rohan]
					// companions or Eowyn); his trigger answers an exertion, and nobody exerts here
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: a [rohan] Man ally (cheated in); a regroup ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; adds (2) on its own play only
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,   // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.GreatRing    // adds no strength, and answers no wound while it is not worn
		);
	}

	protected VirtualTableScenario GetSamScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horse", "17_103");

					// Frodo, Frenzied Fighter: "This version of Frodo cannot become the Ring-bearer"; strength 3,
					// vitality 4; a skirmish ability used only on purpose
					put("frodo", "13_149");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; Valiant is its whole text

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; adds (2) on its own play only
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,       // Sam, Bearer of Great Need: the Ring-bearer, strength 3
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void WhereNowTheHorseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Where Now The Horse
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a [rohan] companion to make Frodo strength +5. If Frodo loses the skirmish, discard that [rohan] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horse");

		assertEquals("Where Now The Horse", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingARohanCompanionMakesFrodoStrengthPlus5ForHisSkirmishAndHisWinDiscardsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horse = scn.GetFreepsCard("horse");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		scn.MoveCardsToHand(horse);
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{guard, savage});
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(3, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(horse));

		scn.FreepsPlayCard(horse);
		// [rohan] companions of two titles and two races; not the [rohan] ally, nor the companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(8, scn.GetStrength(frodo));
		assertInDiscard(horse);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 8 against 5: Frodo wins, and nobody is discarded.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, merry, guard);

		// The bonus was for that skirmish alone.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, savage));
		assertEquals(3, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IfFrodoLosesTheSkirmishTheSpottedRohanCompanionIsDiscardedAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horse = scn.GetFreepsCard("horse");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, merry);

		scn.StartGame();
		scn.MoveCardsToHand(horse);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		scn.FreepsPlayCard(horse);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		scn.FreepsChooseCard(guard);
		assertEquals(8, scn.GetStrength(frodo));
		assertInZone(Zone.FREE_CHARACTERS, guard, merry);
		assertEquals(0, scn.GetWoundsOn(frodo));

		// 8 against 13: Frodo loses and takes a wound, short of being overwhelmed.  The discard is ordered with
		// the rule's skirmish damage.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(guard);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutARohanCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horse = scn.GetFreepsCard("horse");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		scn.MoveCardsToHand(horse);
		assertInHand(horse);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);

		// A [rohan] Man who is an ally, and a Man companion of another culture.
		assertInHand(horse);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(horse));

		scn.MoveCompanionsToTable(guard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(horse));
	}

	@Test
	public void FrodoInNoSkirmishIsMadeStrongerAndALostSkirmishThatIsNotHisDiscardsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horse = scn.GetFreepsCard("horse");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(guard, merry);

		scn.StartGame();
		scn.MoveCardsToHand(horse);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, brigand);
		scn.FreepsResolveSkirmish(guard);

		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(3, scn.GetStrength(frodo));

		// Built as printed (F-0527): the event names no skirmish of Frodo's.
		scn.FreepsPlayCard(horse);
		scn.FreepsChooseCard(merry);
		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));

		// 6 against 8: the Guard loses, and Merry stays.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.FREE_CHARACTERS, merry, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFrodoWhoIsNotTheRingBearerIsTheOneMadeStrongerAndHisLossDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamScenario();

		var horse = scn.GetFreepsCard("horse");
		var sam = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(frodo, guard);

		scn.StartGame();
		scn.MoveCardsToHand(horse);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));

		// The Guard is the one [rohan] companion, spotted unasked.
		scn.FreepsPlayCard(horse);
		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertInZone(Zone.FREE_CHARACTERS, guard);

		// 8 against 13.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoFrodoInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamScenario();

		var horse = scn.GetFreepsCard("horse");
		var sam = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveCardsToHand(horse);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsPlayAvailable(horse));

		scn.FreepsPlayCard(horse);
		assertInDiscard(horse);
		assertEquals(3, scn.GetStrength(sam));

		// 3 against 5: the Ring-bearer, no Frodo, loses, and the Guard stays.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
