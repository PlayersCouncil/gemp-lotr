package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_242_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "57_242");
					put("merry", "7_320");      // Merry, Rohirrim Squire: [shire], strength 3, vitality 4; a skirmish ability used only on purpose
					put("merry2", "2_104");     // Merry, Horticulturalist: another Merry, never on the same board
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion who is not Merry
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("guard2", "5_83");
					put("guard3", "5_83");
					put("leowyn", "7_239");     // Leowyn: a [rohan] ally, not a companion; fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not [rohan]
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, vitality 3, no text
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
					put("sword", "57_242");
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3, vitality 4, enduring; his text answers another [rohan] Man exerting
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, vitality 3, no text
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
		 * Maneuver: Exert Merry X times to exert a minion once for each [rohan] companion you spot (limit X).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Merry's Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnMerryOnlyAndMakesHimStrength5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(merry, sam, guard);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(sword);

		// Merry is the one bearer it can go on; the other Hobbit and the [rohan] Man are not asked about.
		assertAttachedTo(sword, merry);
		assertNotAttachedTo(sword, sam);
		assertNotAttachedTo(sword, guard);
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
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

		scn.StartGame();

		// A Hobbit companion, a [rohan] companion and the Ring-bearer, and no Merry.
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(merry2);
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);

		assertAttachedTo(sword, merry2);
		assertEquals(5, scn.GetStrength(merry2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverExertsMerryXTimesToExertAMinionOnceForEachRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCompanionsToTable(merry, guard, guard2, guard3);
		scn.AttachCardsTo(merry, sword);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);

		// X from 1 to Merry's 3 spare vitality.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsChoose("3");
		assertEquals(3, scn.GetWoundsOn(merry));

		// Three [rohan] companions and X of 3: three exertions, a minion chosen for each.
		// Minions of two cultures; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, guard, guard2, guard3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// The same minion may be chosen again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));

		// The Uruk Savage is now exhausted: the third exertion can only go on the Goblin Patrol Troop.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NoMoreMinionsAreExertedThanTheTimesMerryExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCompanionsToTable(merry, guard, guard2, guard3);
		scn.AttachCardsTo(merry, sword);
		scn.AddWoundsToChar(merry, 1);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sword);
		// One wound already: 2 spare vitality.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(3, scn.GetWoundsOn(merry));

		// Three [rohan] companions, limit 2.
		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoUsesInOnePhaseEachExertUpToTheirOwnX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCompanionsToTable(merry, guard, guard2, guard3);
		scn.AttachCardsTo(merry, sword);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sword);
		scn.FreepsChoose("1");
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.ShadowPass();

		// The same maneuver phase: a second use, X of 2, exerts two more minions.
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);
		scn.FreepsChoose("2");
		scn.FreepsChooseCard(savage);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoRohanCompanionsAnXOfThreeExertsOnlyTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
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

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sword);
		scn.FreepsChoose("3");
		assertEquals(3, scn.GetWoundsOn(merry));

		// Two [rohan] companions; the [rohan] ally and the [gondor] companion do not count.
		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneRohanCompanionAnXOfTwoExertsOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(merry, guard);
		scn.AttachCardsTo(merry, sword);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sword);
		scn.FreepsChoose("2");
		assertEquals(2, scn.GetWoundsOn(merry));

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionMerryMayStillExertButNoMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leowyn = scn.GetFreepsCard("leowyn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(leowyn);
		scn.AttachCardsTo(merry, sword);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// It has a cost, so it is offered though it will exert nothing.
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);
		scn.FreepsChoose("1");

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileMerryIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(merry, guard);
		scn.AttachCardsTo(merry, sword);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(merry, 3);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(sword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheRohanMerryBearsItAndExertsForItAsTheShireOnesDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanMerryScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(merry, guard);
		scn.MoveCardsToHand(sword);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		// This Merry is [rohan], not [shire]; the text names only the title.
		assertEquals(Culture.ROHAN, merry.getBlueprint().getCulture());
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, merry);
		assertEquals(5, scn.GetStrength(merry));

		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsChoose("3");
		assertEquals(3, scn.GetWoundsOn(merry));

		// Two [rohan] companions (this Merry is one of them, and the Household Guard) against an X of 3: two exertions.
		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
