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

public class Card_05_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rager", "5_45");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text

					// Scaling Ladder: an [isengard] machine; its own abilities are used only on purpose
					put("ladder", "5_57");
					put("ladder2", "5_57");
					// Attack on Helm's Deep: an [isengard] support-area condition that holds [isengard] tokens and is
					// no machine; its abilities are used only on purpose
					put("attack", "4_137");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text but knight
					// Thrarin, Dwarven Smith: ally, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
					// Elrond, Lord of Rivendell: ally, vitality 4; his fellowship action is never used, and his
					// start-of-turn heal comes before any wound is laid
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerserkRagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Berserk Rager
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * Berserk Rager is strength +1 for each wound on a character in its skirmish.
		 * <b>Skirmish:</b> Remove 3 [isengard] tokens from a machine and exert Berserk Rager twice to wound every ally twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rager");

		assertEquals("Berserk Rager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CountsTheWoundsOnTheAllyAndEachMinionInItsSkirmishItselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(rager, savage, troop);
		scn.AddWoundsToChar(rager, 1);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(rager));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin exerts so that he may skirmish: his one wound is the exertion.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		// In no skirmish, with wounded characters all about: printed strength.
		assertEquals(10, scn.GetStrength(rager));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, rager}, new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);

		// 10 + 1 on Thrarin + 1 on the Savage + 1 on the Rager himself; not Aragorn's 2, in another skirmish, nor
		// the Knight's 1, in none.
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, rager));
		assertEquals(13, scn.GetStrength(rager));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TwoWoundsOnTheCompanionItSkirmishesCountTwiceAndAnotherSkirmishCountsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(rager, savage);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(rager));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rager}, new PhysicalCardImpl[]{knight, savage});

		// The Knight's skirmish, with his wound in it: the Rager is not in it.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(10, scn.GetStrength(rager));

		// The Knight wins, 6 to 5, and the Savage takes a wound outside the Rager's skirmish.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		// 10 + 2 on Aragorn
		assertEquals(12, scn.GetStrength(rager));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RemovesThreeTokensFromOneMachineAndExertsTwiceToWoundEveryAllyTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager");
		var savage = scn.GetShadowCard("savage");
		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var attack = scn.GetShadowCard("attack");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(thrarin, elrond);
		scn.MoveCardsToSupportArea(ladder, ladder2, attack);

		scn.StartGame();
		scn.MoveMinionsToTable(rager, savage);
		scn.AddTokensToCard(ladder, 3);
		scn.AddTokensToCard(ladder2, 3);
		scn.AddTokensToCard(attack, 3);
		assertEquals(3, scn.GetCultureTokensOn(ladder));
		assertEquals(3, scn.GetCultureTokensOn(ladder2));
		assertEquals(3, scn.GetCultureTokensOn(attack));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(elrond));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(rager));
		assertTrue(scn.ShadowActionAvailable(rager));
		scn.ShadowUseCardAction(rager);
		// Either machine; not the [isengard] condition that is no machine.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ladder, ladder2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attack));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ladder);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(3, scn.GetCultureTokensOn(ladder2));
		assertEquals(3, scn.GetCultureTokensOn(attack));
		assertEquals(2, scn.GetWoundsOn(rager));
		// Two wounds on each ally: Elrond, of vitality 4, lives; Thrarin, of vitality 2, is killed.
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertInZone(Zone.DEAD, thrarin);
		// No companion and no minion is wounded.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TokensSplitBetweenTwoMachinesCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager");
		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var attack = scn.GetShadowCard("attack");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(ladder, ladder2, attack);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		// Four [isengard] tokens on machines, two on each, and three on a card that is no machine.
		scn.AddTokensToCard(ladder, 2);
		scn.AddTokensToCard(ladder2, 2);
		scn.AddTokensToCard(attack, 3);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetCultureTokensOn(ladder2));
		assertEquals(3, scn.GetCultureTokensOn(attack));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(rager));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rager));
	}

	@Test
	public void NotOfferedWhenHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager");
		var ladder = scn.GetShadowCard("ladder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		scn.AddTokensToCard(ladder, 3);
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(rager, 1);
		assertEquals(3, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetWoundsOn(rager));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rager));
		assertEquals(3, scn.GetCultureTokensOn(ladder));
	}

	@Test
	public void WithNoAllyItIsPaidForAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager");
		var ladder = scn.GetShadowCard("ladder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		scn.AddTokensToCard(ladder, 3);
		assertEquals(3, scn.GetCultureTokensOn(ladder));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(rager));
		assertTrue(scn.ShadowActionAvailable(rager));
		scn.ShadowUseCardAction(rager);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(rager));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
