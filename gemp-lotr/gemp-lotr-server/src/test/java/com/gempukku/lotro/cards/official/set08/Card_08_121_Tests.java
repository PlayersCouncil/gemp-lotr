package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "8_121");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and nothing else
					put("scout", "5_90");       // Rohirrim Scout: [rohan] Man companion, valiant; a regroup ability only
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ALLY; a regroup ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no text

					// Hand cards of every kind, for the cost's choice: a companion, a Shadow minion, an event and a
					// possession of other cultures (all held in hand, where their text does nothing).
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					// Dwarven Armor: cards to hold in hand and nothing else.
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");
					put("filler4", "1_8");
					put("filler5", "1_8");
					put("filler6", "1_8");
					put("filler7", "1_8");
					put("filler8", "1_8");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Merry, Noble Warrior
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 5
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: To play, spot a [rohan] companion.
		 * <b>Skirmish:</b> Discard 4 cards from hand to make Merry strength +2 for each [rohan] companion you spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Noble Warrior", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilARohanCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		// A companion of another culture, and a [rohan] Man who is an ally.
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(merry, guard);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertInZone(Zone.SUPPORT, herdsman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohirrimScoutIsARohanCompanionToSpotAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var scout = scn.GetFreepsCard("scout");
		scn.MoveCardsToHand(merry, scout);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(scout);
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingFourCardsMakesMerryStrongerByTwoForEachRohanCompanionTheFreePeoplesPlayerSpots() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		var filler1 = scn.GetFreepsCard("filler1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, guard, scout, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(filler1, aragorn, runner, deftness, sword);
		assertInZone(Zone.HAND, filler1, aragorn, runner, deftness, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage}, new PhysicalCardImpl[]{guard, troop});
		scn.FreepsResolveSkirmish(merry);

		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		// Cards of every kind and both sides are offered from the hand; a companion in play is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(filler1, aragorn, runner, deftness, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, runner, deftness, filler1);
		assertInZone(Zone.DISCARD, aragorn, runner, deftness, filler1);
		assertInZone(Zone.HAND, sword);

		// He names how many to spot: up to the two [rohan] companions, not the [rohan] ally or the [gondor]
		// companion, and the number comes filled in with all of them (F-0561).
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(2, (int) scn.FreepsGetChoiceDefault());
		// Nothing is given before the number is named.
		assertEquals(5, scn.GetStrength(merry));
		scn.FreepsChoose("1");

		// 5 + 2 for the one spotted
		assertEquals(7, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedOutsideHisOwnSkirmishTheBonusForTwoIsGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, guard, scout, knight);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"),
				scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		// Merry is not assigned: his text says nothing of his own skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{knight, troop});
		scn.FreepsResolveSkirmish(guard);

		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Exactly four cards: they are discarded unasked.
		scn.FreepsUseCardAction(merry);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		// 5 + 2 + 2
		assertEquals(9, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the end of this skirmish; Household Guard 6 beats the Uruk Savage 5.
		scn.ShadowPass();
		assertEquals(9, scn.GetStrength(merry));
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));

		// The next skirmish is a phase of its own: Merry is 5 again.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishing(knight));
		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionToSpotItIsStillOfferedAndTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		// Merry is cheated in: the [rohan] companion his play wanted has gone.  Only the [rohan] ally and the
		// [gondor] companion are left beside him.
		scn.MoveCompanionsToTable(merry, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"),
				scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(0, scn.GetFreepsHandCount());
		// Nothing to spot: no number is asked, and Merry is as he was.
		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(merry, guard);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}
}
