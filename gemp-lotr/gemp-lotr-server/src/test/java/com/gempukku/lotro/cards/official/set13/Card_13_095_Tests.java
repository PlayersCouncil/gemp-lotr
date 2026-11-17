package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lying", "13_95");
					put("lying2", "13_95");
					put("lying3", "13_95");
					put("lying4", "13_95");

					// Footman of Dunland: a [men] Man, strength 7, vitality 3.  Its text makes the current site a plains,
					// which nothing here reads.
					put("footman", "11_82");
					// Saruman, Coldly Still: a [men] minion who is no Man (Wizard), strength 8, vitality 4.  His +2 is
					// for [men] minions bearing a possession, and none does (this card is a condition); his skirmish
					// ability is never used.
					put("saruman", "17_48");
					// Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, strength 3, vitality 4, cost 1, site 2.
					// A maneuver and a regroup ability, never used.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text.
					put("brigand", "4_10");
					// Goaded to War: a [men] condition, no minion.  Its trigger answers a move to a battleground or
					// plains, and nobody moves after it is laid.
					put("goaded", "12_67");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, vitality 3, damage +1.  A skirmish ability,
					// never used.
					put("gimli", "1_13");
					// Boromir, Son of Denethor: a [gondor] Man, strength 7, vitality 3.  A skirmish ability, never used.
					put("boromir", "1_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LyingInWaitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Lying in Wait
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to your [men] minion. Limit 1 per bearer.<br>Bearer is strength +1 for each other [men] minion you can spot.<br>Each time a [men] minion wins a skirmish, make bearer strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var lying = scn.GetFreepsCard("lying");

		assertEquals("Lying in Wait", lying.getBlueprint().getTitle());
		assertNull(lying.getBlueprint().getSubtitle());
		assertFalse(lying.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, lying.getBlueprint().getSide());
		assertEquals(Culture.MEN, lying.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, lying.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(lying, Keyword.SUPPORT_AREA));
		assertEquals(0, lying.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishActionTransfersItToAMenMinionInOrOutOfTheSkirmishAndNotAgainFromThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lying = scn.GetShadowCard("lying");
		var footman = scn.GetShadowCard("footman");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveCardsToSupportArea(lying);
		assertInZone(Zone.SUPPORT, lying);
		scn.MoveMinionsToTable(footman, saruman, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		// In the support area it makes nobody stronger.
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.ShadowActionAvailable(lying));
		scn.ShadowUseCardAction(lying);

		// The Footman in the skirmish and Saruman out of it are [men] minions; the [dunland] Brigand is not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, aragorn));
		scn.ShadowChooseCard(footman);
		assertAttachedTo(lying, footman);

		// One other [men] minion, Saruman: the Footman is 7 + 1.  The Brigand is not counted, and Saruman himself is
		// no bearer.
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// It is no longer in the support area: not offered again, though Saruman could still take it.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lying));
	}

	@Test
	public void OneCopyToABearerAndNoneOfferedOnceEveryMenMinionBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lying = scn.GetShadowCard("lying");
		var lying2 = scn.GetShadowCard("lying2");
		var lying3 = scn.GetShadowCard("lying3");
		var lying4 = scn.GetShadowCard("lying4");
		var footman = scn.GetShadowCard("footman");
		var saruman = scn.GetShadowCard("saruman");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveCardsToSupportArea(lying, lying2, lying3, lying4);
		assertInZone(Zone.SUPPORT, lying);
		assertInZone(Zone.SUPPORT, lying2);
		assertInZone(Zone.SUPPORT, lying3);
		assertInZone(Zone.SUPPORT, lying4);
		scn.MoveMinionsToTable(footman, saruman, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowUseCardAction(lying);
		scn.ShadowChooseCard(footman);
		assertAttachedTo(lying, footman);

		// Limit 1 per bearer: the second copy is offered to Saruman and Ted, who bear none, and not to the Footman.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(lying2));
		scn.ShadowUseCardAction(lying2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footman));
		scn.ShadowChooseCard(saruman);
		assertAttachedTo(lying2, saruman);
		assertAttachedTo(lying, footman);

		// The third goes to Ted, the one left without a copy.
		scn.FreepsPass();
		scn.ShadowUseCardAction(lying3);
		assertAttachedTo(lying3, ted);
		// Each bearer counts the other two: 7 + 2, 8 + 2 and 3 + 2.
		assertEquals(9, scn.GetStrength(footman));
		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(5, scn.GetStrength(ted));

		// Every [men] minion bears one: the fourth copy is not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lying4));
		assertInZone(Zone.SUPPORT, lying4);
	}

	@Test
	public void BearerIsStrengthPlusOneForEachOtherMenMinionAsTheyArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lying = scn.GetShadowCard("lying");
		var footman = scn.GetShadowCard("footman");
		var saruman = scn.GetShadowCard("saruman");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var goaded = scn.GetShadowCard("goaded");
		scn.MoveCardsToHand(saruman, ted);

		scn.StartGame();
		scn.MoveMinionsToTable(footman, brigand);
		scn.MoveCardsToSupportArea(goaded);
		scn.AttachCardsTo(footman, lying);
		assertAttachedTo(lying, footman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Beside the bearer: a [dunland] Man and a [men] card that is no minion.  Nothing to count.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetStrength(footman));

		// A [men] Wizard played: one to count.
		scn.ShadowPlayCard(saruman);
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(8, scn.GetStrength(saruman));

		// A [men] Hobbit played: two.
		scn.ShadowPlayCard(ted);
		assertEquals(9, scn.GetStrength(footman));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMenMinionWinMakesBearerStrongerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var lying = scn.GetShadowCard("lying");
		var footman = scn.GetShadowCard("footman");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, saruman, brigand);
		scn.AttachCardsTo(footman, lying);
		assertAttachedTo(lying, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, saruman},
				new PhysicalCardImpl[]{boromir, footman},
				new PhysicalCardImpl[]{aragorn, brigand});

		// 7, + 1 for Saruman.
		assertEquals(8, scn.GetStrength(footman));

		// Saruman's 8 beats Gimli's 6: a [men] Wizard who is not the bearer wins.  The bearer is +1; Saruman is not.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(footman));
		assertEquals(8, scn.GetStrength(saruman));

		// The bearer himself, a Man, wins at 9 against Boromir's 7: +1 again.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(footman));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(10, scn.GetStrength(footman));

		// Still in force in the next skirmish.  The Brigand's 8 ties Aragorn's 8 and wins: no [men] minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(footman));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The regroup phase: both bonuses gone, the count of Saruman left.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(footman));
	}

	@Test
	public void BearerLosingAndAMinionOfAnotherCultureWinningMakeNobodyStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var lying = scn.GetShadowCard("lying");
		var footman = scn.GetShadowCard("footman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, brigand);
		scn.AttachCardsTo(footman, lying);
		assertAttachedTo(lying, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, footman},
				new PhysicalCardImpl[]{gimli, brigand});

		// Aragorn's 8 beats the bearer's 7 (no other [men] minion): a [men] minion loses and lives.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(footman));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(footman));

		// The [dunland] Brigand's 8 beats Gimli's 6.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(7, scn.GetStrength(footman));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACopyStillInTheSupportAreaIsNotMovedByAMenWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		var brigand = scn.GetShadowCard("brigand");

		var lying = scn.GetShadowCard("lying");
		var lying2 = scn.GetShadowCard("lying2");
		var footman = scn.GetShadowCard("footman");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, saruman, brigand);
		scn.AttachCardsTo(footman, lying);
		scn.MoveCardsToSupportArea(lying2);
		assertAttachedTo(lying, footman);
		assertInZone(Zone.SUPPORT, lying2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, saruman}, new PhysicalCardImpl[]{aragorn, brigand});
		scn.ShadowDeclineAssignments();

		assertEquals(8, scn.GetStrength(footman));

		// Saruman, a [men] minion, wins.  Only the borne copy answers: the copy in the support area has no bearer.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.FreepsGetBPChoiceCount());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(footman));
		assertInZone(Zone.SUPPORT, lying2);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
