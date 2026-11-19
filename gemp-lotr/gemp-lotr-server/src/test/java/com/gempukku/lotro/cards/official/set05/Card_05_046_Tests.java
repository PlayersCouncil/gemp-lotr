package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserk", "5_46");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text

					// Scaling Ladder: an [isengard] machine condition; its own abilities are used only on purpose
					put("ladder", "5_57");
					put("ladder2", "5_57");
					// Uruk Shield Wall: an [isengard] machine possession; its wound trigger is off during skirmishes and
					// its maneuver ability is used only on purpose
					put("wall", "102_35");
					// Attack on Helm's Deep: an [isengard] support-area condition that holds [isengard] tokens and is
					// no machine; its abilities are used only on purpose
					put("attack", "4_137");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text but knight
					// Thrarin, Dwarven Smith: ally, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerserkSavageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Berserk Savage
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * Berserk Savage is strength +1 for each wound on a character in its skirmish.
		 * <b>Skirmish:</b> Remove 4 [isengard] tokens from a machine and exert Berserk Savage twice to make it strength +8.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("berserk");

		assertEquals("Berserk Savage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CountsTheWoundsOnTheAllyAndEachMinionInItsSkirmishItselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserk = scn.GetShadowCard("berserk");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(berserk, savage, troop);
		scn.AddWoundsToChar(berserk, 1);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(berserk));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin exerts so that he may skirmish: his one wound is the exertion.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		// In no skirmish, with wounded characters all about: printed strength.
		assertEquals(12, scn.GetStrength(berserk));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, berserk}, new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);

		// 12 + 1 on Thrarin + 1 on the Uruk Savage + 1 on the Berserk Savage himself; not Aragorn's 2, in another
		// skirmish, nor the Knight's 1, in none.
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, berserk));
		assertEquals(15, scn.GetStrength(berserk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TwoWoundsOnTheCompanionItSkirmishesCountTwiceAndAnotherSkirmishCountsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserk = scn.GetShadowCard("berserk");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(berserk, savage);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(berserk));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, berserk}, new PhysicalCardImpl[]{knight, savage});

		// The Knight's skirmish, with his wound in it: the Berserk Savage is not in it.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(12, scn.GetStrength(berserk));

		// The Knight wins, 6 to 5, and the Uruk Savage takes a wound outside the Berserk Savage's skirmish.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		// 12 + 2 on Aragorn
		assertEquals(14, scn.GetStrength(berserk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RemovesFourTokensFromOneMachineAndExertsTwiceToBeStrengthPlusEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserk = scn.GetShadowCard("berserk");
		var ladder = scn.GetShadowCard("ladder");
		var wall = scn.GetShadowCard("wall");
		var attack = scn.GetShadowCard("attack");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder, wall, attack);

		scn.StartGame();
		scn.MoveMinionsToTable(berserk);
		scn.AddTokensToCard(ladder, 4);
		scn.AddTokensToCard(wall, 4);
		scn.AddTokensToCard(attack, 4);
		assertEquals(4, scn.GetCultureTokensOn(ladder));
		assertEquals(4, scn.GetCultureTokensOn(wall));
		assertEquals(4, scn.GetCultureTokensOn(attack));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, berserk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(berserk));
		assertEquals(12, scn.GetStrength(berserk));
		assertTrue(scn.ShadowActionAvailable(berserk));
		scn.ShadowUseCardAction(berserk);
		// A machine condition or a machine possession; not the [isengard] condition that is no machine.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ladder, wall));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attack));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(wall);

		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertEquals(4, scn.GetCultureTokensOn(ladder));
		assertEquals(4, scn.GetCultureTokensOn(attack));
		assertEquals(2, scn.GetWoundsOn(berserk));
		// 12 + 8, and + 2 for his own two wounds, now on a character in his skirmish.
		assertEquals(22, scn.GetStrength(berserk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TokensSplitBetweenTwoMachinesCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserk = scn.GetShadowCard("berserk");
		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var attack = scn.GetShadowCard("attack");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder, ladder2, attack);

		scn.StartGame();
		scn.MoveMinionsToTable(berserk);
		// Six [isengard] tokens on machines, three on each, and four on a card that is no machine.
		scn.AddTokensToCard(ladder, 3);
		scn.AddTokensToCard(ladder2, 3);
		scn.AddTokensToCard(attack, 4);
		assertEquals(3, scn.GetCultureTokensOn(ladder));
		assertEquals(3, scn.GetCultureTokensOn(ladder2));
		assertEquals(4, scn.GetCultureTokensOn(attack));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, berserk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(berserk));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserk));
	}

	@Test
	public void NotOfferedWhenHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserk = scn.GetShadowCard("berserk");
		var ladder = scn.GetShadowCard("ladder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(berserk);
		scn.AddTokensToCard(ladder, 4);
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(berserk, 1);
		assertEquals(4, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetWoundsOn(berserk));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, berserk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserk));
		assertEquals(4, scn.GetCultureTokensOn(ladder));
	}

	@Test
	public void UsedOutsideHisOwnSkirmishTheBonusLastsUntilThatSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserk = scn.GetShadowCard("berserk");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var ladder = scn.GetShadowCard("ladder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(berserk, savage, troop);
		scn.AddTokensToCard(ladder, 4);
		assertEquals(4, scn.GetCultureTokensOn(ladder));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{knight, savage});
		// The Berserk Savage is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(berserk));
		assertTrue(scn.ShadowActionAvailable(berserk));
		scn.ShadowUseCardAction(berserk);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(berserk));
		// 12 + 8: his own wounds are on no character in a skirmish of his.
		assertEquals(20, scn.GetStrength(berserk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's skirmish resolves; the Knight's is a phase of its own, and the +8 is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(12, scn.GetStrength(berserk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
