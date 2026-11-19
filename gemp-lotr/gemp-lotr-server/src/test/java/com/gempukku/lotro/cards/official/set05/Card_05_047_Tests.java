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

public class Card_05_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slayer", "5_47");
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
					// Elrond, Lord of Rivendell: ally, vitality 4; his fellowship action is never used, and his
					// start-of-turn heal comes before any wound is laid
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// Answer To All Riddles: no response of its own to a wound on the Ring-bearer while it is not worn
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void BerserkSlayerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Berserk Slayer
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * Berserk Slayer is strength +2 for each wound on a character in its skirmish.
		 * <b>Skirmish:</b> Remove 5 [isengard] tokens from a machine and exert Berserk Slayer twice to wound every companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slayer");

		assertEquals("Berserk Slayer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CountsTheWoundsOnTheAllyAndEachMinionInItsSkirmishItselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, savage, troop);
		scn.AddWoundsToChar(slayer, 1);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin exerts so that he may skirmish: his one wound is the exertion.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		// In no skirmish, with wounded characters all about: printed strength.
		assertEquals(12, scn.GetStrength(slayer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, slayer}, new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);

		// 12 + 2 for each wound: 1 on Thrarin, 1 on the Uruk Savage and 1 on the Berserk Slayer himself; not Aragorn's 2, in another
		// skirmish, nor the Knight's 1, in none.
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, slayer));
		assertEquals(18, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TwoWoundsOnTheCompanionItSkirmishesCountTwiceAndAnotherSkirmishCountsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, savage);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(slayer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, slayer}, new PhysicalCardImpl[]{knight, savage});

		// The Knight's skirmish, with his wound in it: the Berserk Slayer is not in it.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(12, scn.GetStrength(slayer));

		// The Knight wins, 6 to 5, and the Uruk Savage takes a wound outside the Berserk Slayer's skirmish.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		// 12 + 2 for each of the 2 wounds on Aragorn
		assertEquals(16, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RemovesFiveTokensFromOneMachineAndExertsTwiceToWoundEveryCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var ladder = scn.GetShadowCard("ladder");
		var wall = scn.GetShadowCard("wall");
		var attack = scn.GetShadowCard("attack");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(ladder, wall, attack);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, savage);
		scn.AddTokensToCard(ladder, 5);
		scn.AddTokensToCard(wall, 5);
		scn.AddTokensToCard(attack, 5);
		// Vitality 3 with 2 wounds: exhausted, so the wound kills him.
		scn.AddWoundsToChar(knight, 2);
		assertEquals(5, scn.GetCultureTokensOn(ladder));
		assertEquals(5, scn.GetCultureTokensOn(wall));
		assertEquals(5, scn.GetCultureTokensOn(attack));
		assertEquals(2, scn.GetWoundsOn(knight));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, slayer);
		// The Uruk Savage is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(12, scn.GetStrength(slayer));
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		// A machine condition or a machine possession; not the [isengard] condition that is no machine.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ladder, wall));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attack));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(wall);

		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertEquals(5, scn.GetCultureTokensOn(ladder));
		assertEquals(5, scn.GetCultureTokensOn(attack));
		assertEquals(2, scn.GetWoundsOn(slayer));
		// One wound on each companion: the Ring-bearer, Aragorn, and the exhausted Knight, who is killed.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DEAD, knight);
		// No ally and no minion is wounded.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(savage));
		// 12 + 2 for each wound in his skirmish: his own 2 and Aragorn's 1.
		assertEquals(18, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TokensSplitBetweenTwoMachinesCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var attack = scn.GetShadowCard("attack");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder, ladder2, attack);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		// Eight [isengard] tokens on machines, four on each, and five on a card that is no machine.
		scn.AddTokensToCard(ladder, 4);
		scn.AddTokensToCard(ladder2, 4);
		scn.AddTokensToCard(attack, 5);
		assertEquals(4, scn.GetCultureTokensOn(ladder));
		assertEquals(4, scn.GetCultureTokensOn(ladder2));
		assertEquals(5, scn.GetCultureTokensOn(attack));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, slayer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(slayer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void NotOfferedWhenHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var ladder = scn.GetShadowCard("ladder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.AddTokensToCard(ladder, 5);
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(slayer, 1);
		assertEquals(5, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetWoundsOn(slayer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, slayer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
		assertEquals(5, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
