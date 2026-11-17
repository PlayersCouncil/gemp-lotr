package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_242_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "7_242");
					put("merry", "7_320");      // Merry, Rohirrim Squire: [shire], strength 3, vitality 4; a skirmish ability used only on purpose
					put("merry2", "2_104");     // Merry, Horticulturalist: another Merry, never on the same board
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion who is not Merry
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("guard2", "5_83");
					put("guard3", "5_83");
					put("leowyn", "7_239");     // Leowyn: a [rohan] ally, not a companion; fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not [rohan]
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion of vitality 1, so exhausted; its text answers its own play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRohanMerryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "7_242");
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3, vitality 4, enduring; his text answers another [rohan] Man exerting
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerrysSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Merry's Sword
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be Merry.
		 * Maneuver: Exert Merry twice to exert a minion once for each [rohan] companion you spot.
		*/

		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertEquals("Merry's Sword", sword.getBlueprint().getTitle());
		assertNull(sword.getBlueprint().getSubtitle());
		assertTrue(sword.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sword.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getPossessionClasses().size());
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnMerryOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(merry, sam, guard);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);

		// Merry is the one bearer it can go on; the other Hobbit and the [rohan] Man are not asked about.
		assertAttachedTo(sword, merry);
		assertNotAttachedTo(sword, sam);
		assertNotAttachedTo(sword, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutMerryAndAnotherMerryMayBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry2 = scn.GetFreepsCard("merry2");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(sam, guard);
		scn.MoveCardsToHand(sword, merry2);
		assertInHand(sword, merry2);

		scn.StartGame();

		// A Hobbit companion, a [rohan] companion and the Ring-bearer, and no Merry.
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(merry2);
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);

		assertAttachedTo(sword, merry2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverExertsMerryTwiceToExertAMinionOnceForEachRohanCompanionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var leowyn = scn.GetFreepsCard("leowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, guard, guard2, aragorn);
		scn.MoveAlliesToTable(leowyn);
		scn.AttachCardsTo(merry, sword);
		assertAttachedTo(sword, merry);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);
		assertEquals(2, scn.GetWoundsOn(merry));

		// Two [rohan] companions; the [rohan] ally and the [gondor] companion do not count.  The player names how
		// many he spots, from none to both, the question filled in with both.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// Minions of two cultures and races; not the exhausted Runner, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, merry, guard, guard2, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// The same minion may be chosen again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(savage);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ThePlayerMaySpotFewerRohanCompanionsThanThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCompanionsToTable(merry, guard, guard2, guard3);
		scn.AttachCardsTo(merry, sword);
		assertAttachedTo(sword, merry);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sword);
		assertEquals(2, scn.GetWoundsOn(merry));

		// Three [rohan] companions: he names one.
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("1");

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);

		// One exertion, and no second choice.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionMerryStillExertsTwiceAndNoMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leowyn = scn.GetFreepsCard("leowyn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(leowyn);
		scn.AttachCardsTo(merry, sword);
		assertAttachedTo(sword, merry);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// It has a cost, so it is offered though it will exert nothing.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);

		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionThatCanExertMerryStillExertsTwiceForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(merry, guard, guard2);
		scn.AttachCardsTo(merry, sword);
		assertAttachedTo(sword, merry);

		// The only minion is the Runner, exhausted: two [rohan] companions to spot and nothing to exert.
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);

		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedOnceMerryCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(merry, sam, guard);
		scn.AttachCardsTo(merry, sword);
		assertAttachedTo(sword, merry);
		// Vitality 4 with 1 wound: 3 left, enough to exert twice.
		scn.AddWoundsToChar(merry, 1);
		assertEquals(1, scn.GetWoundsOn(merry));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(sword));

		// 2 vitality left: he can exert once, not twice.  Sam, who could exert twice, does not stand in for him.
		scn.AddWoundsToChar(merry, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsActionAvailable(sword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheRohanMerryBearsItAndCountsHimselfAsARohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanMerryScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(merry, guard);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		// This Merry is [rohan], not [shire]; the text names only the title.
		assertEquals(Culture.ROHAN, merry.getBlueprint().getCulture());
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, merry);

		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);
		assertEquals(2, scn.GetWoundsOn(merry));

		// Two [rohan] companions: this Merry, a Hobbit, and the Household Guard, a Man.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
