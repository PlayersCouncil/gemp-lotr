package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fates", "13_148");

					// Sam, Son of Hamfast: a [shire] Hobbit companion, strength 3; a fellowship ability and a
					// response to Frodo's death
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion; +2 only while he bears a weapon
					put("merry", "1_303");
					// Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, and a skirmish ability used only
					// on purpose
					put("pippin", "103_31");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3; an assignment ability
					// used only on purpose
					put("smeagol", "103_116");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					// Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Bilbo, Melancholy Hobbit: a Bilbo who is a companion, strength 3; at the start of his skirmish
					// he may exert to take a [shire] skirmish event from the discard pile
					put("bilbo", "12_119");
					// Bilbo, Retired Adventurer: a Bilbo who is an ally, home site 3; a fellowship ability
					put("oldbilbo", "1_284");

					// Crebain Flock: [isengard] Crow, strength 2, vitality 3; its Shadow ability is used only on purpose
					put("crebain1", "101_25");
					put("crebain2", "101_25");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3, vitality 4; his
					// abilities are used only on purpose
					put("ted", "18_75");
					// Mordor Soldier: [sauron] Orc, strength 3, vitality 2; a skirmish ability that removes a threat
					put("soldier", "7_290");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FatesEntwinedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fates Entwined
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a Hobbit wins a skirmish, you may heal another Hobbit once (or heal another Hobbit twice if the Hobbit that won the skirmish was Frodo or Bilbo).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fates");

		assertEquals("Fates Entwined", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeAHobbitWinsASkirmishAnotherHobbitMayBeHealedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fates = scn.GetFreepsCard("fates");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(sam, merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(bounder, 1);

		var crebain1 = scn.GetShadowCard("crebain1");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(crebain1, ted);
		scn.AddWoundsToChar(ted, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, crebain1);
		// Ted Sandyman stays out of the skirmishes: a Hobbit of the opponent's, wounded.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		// Sam, 3, beats the Crebain, 2.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fates));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsAcceptOptionalTrigger();
		// Any other Hobbit: a [shire] companion, a [gondor] one, an ally and the opponent's minion. Not Sam, who
		// won; not Smeagol, a [shire] companion who is no Hobbit; not Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		// Healed once.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOpponentsHobbitWinningASkirmishAlsoLetsAnotherHobbitBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fates = scn.GetFreepsCard("fates");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(sam, merry);
		scn.AddWoundsToChar(merry, 2);

		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, ted);
		scn.FreepsResolveSkirmish(sam);
		// Ted Sandyman, 3, beats Sam, 3: a tie goes to the Shadow side.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fates));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry));
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningHealsNobodyAndASecondHobbitWinningOffersAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fates = scn.GetFreepsCard("fates");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(sam, merry);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(merry, 1);

		var crebain1 = scn.GetShadowCard("crebain1");
		var crebain2 = scn.GetShadowCard("crebain2");

		scn.StartGame();
		scn.MoveMinionsToTable(crebain1, crebain2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, crebain1}, new PhysicalCardImpl[]{merry, crebain2});
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fates));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Merry wins the second skirmish: offered again, and Sam is healed.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fates));
		// Sam is the only other Hobbit wounded, and is healed without being asked for.
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenFrodoWinsAnotherHobbitIsHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fates = scn.GetFreepsCard("fates");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(sam, merry);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(merry, 3);

		var crebain1 = scn.GetShadowCard("crebain1");

		scn.StartGame();
		scn.MoveMinionsToTable(crebain1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, crebain1);
		scn.FreepsResolveSkirmish(frodo);
		// Frodo, 3 + 1 for The Ruling Ring, beats the Crebain, 2.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fates));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(merry);

		// Twice, and both times Merry: not one wound each from two Hobbits.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBilboWinsAnotherHobbitIsHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fates = scn.GetFreepsCard("fates");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(bilbo, merry);
		scn.AddWoundsToChar(merry, 3);

		var crebain1 = scn.GetShadowCard("crebain1");

		scn.StartGame();
		scn.MoveMinionsToTable(crebain1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, crebain1);
		scn.FreepsResolveSkirmish(bilbo);
		// His own start-of-skirmish offer, which has an exertion for its cost, is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		// Bilbo, 3, beats the Crebain, 2.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fates));
		assertEquals(3, scn.GetWoundsOn(merry));
		// Merry is the only other Hobbit wounded, and is healed without being asked for.
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBilboTheAllyWinsAnotherHobbitIsHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fates = scn.GetFreepsCard("fates");
		var oldbilbo = scn.GetFreepsCard("oldbilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(oldbilbo);

		var crebain1 = scn.GetShadowCard("crebain1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(merry, 3);
		scn.MoveMinionsToTable(crebain1);
		// Strength 2 by print: +2 by hand so that he beats the Crebain, 2.
		scn.ApplyAdHocModifier(new StrengthModifier(null, oldbilbo, null, 2));
		// The fellowship moves to site 3, Bilbo's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oldbilbo, crebain1);
		scn.FreepsResolveSkirmish(oldbilbo);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fates));
		assertEquals(3, scn.GetWoundsOn(merry));
		// Merry is the only other Hobbit wounded, and is healed without being asked for.
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAHobbitLosesNorWhenACompanionWhoIsNoHobbitWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fates = scn.GetFreepsCard("fates");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(sam, smeagol, merry);
		scn.AddWoundsToChar(merry, 2);

		var soldier = scn.GetShadowCard("soldier");
		var crebain1 = scn.GetShadowCard("crebain1");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, crebain1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, soldier}, new PhysicalCardImpl[]{smeagol, crebain1});

		// Sam, 3, loses to the Mordor Soldier, 3.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Smeagol, 3, a [shire] companion who is no Hobbit, beats the Crebain, 2.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoOtherHobbitIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fates = scn.GetFreepsCard("fates");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fates);
		scn.MoveCompanionsToTable(sam, smeagol, merry);
		// The winner and a companion who is no Hobbit are wounded; no other Hobbit is.
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(smeagol, 1);

		var crebain1 = scn.GetShadowCard("crebain1");

		scn.StartGame();
		scn.MoveMinionsToTable(crebain1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, crebain1);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
