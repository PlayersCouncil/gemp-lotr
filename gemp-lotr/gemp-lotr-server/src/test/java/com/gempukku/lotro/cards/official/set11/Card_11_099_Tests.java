package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("squad", "11_99");
					// A second copy: "another [men] minion" for the first.
					put("squad2", "11_99");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit.  His abilities are never used.
					put("ted", "18_75");
					put("soldier", "4_254");    // Southron Soldier: a [raider] Man minion; "Southron. Ambush (2)." is its whole text
					// Goaded to War: a [men] condition in the support area; its trigger answers a move to a battleground
					// or plains (site 2 of this path is Ettenmoors, a plains: it adds (1) and nothing reads the pool).
					put("goaded", "12_67");

					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5; a fellowship ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: an ally, resistance 0; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6; a maneuver ability used only on purpose
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: resistance 6; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf of resistance 6; a skirmish ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf of resistance 6; +3 only against a Nazgul
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("secret", "1_71");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SquadofHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Squad of Haradrim
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Spot another [men] minion and 2 Free Peoples characters who each have resistance 5 or less to make the move limit -1 for this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("squad");

		assertEquals("Squad of Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityMakesTheMoveLimitOneLessForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("sam", "gandalf", "gimli", "arwen");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(secret);

		var squad = scn.GetShadowCard("squad");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		// The limit cannot read below 1, so from 2 a penalty of 1 and of 2 read the same: raise it first.
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.MoveMinionsToTable(squad, legion);
		scn.SkipToPhase(Phase.REGROUP);

		// Sam (5) and Barliman, an ally (0); every other companion has resistance 6 or more.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(barliman));
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(squad));
		scn.ShadowUseCardAction(squad);
		// 2 + 1 - 1
		assertEquals(2, scn.GetMoveLimit());

		// One move made of two: the fellowship moves again, and the penalty stands for the turn.
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FromTheUsualLimitTheTurnEndsAtTheRegroupPhaseAndTheNextTurnHasItsOwnLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam");
		scn.MoveAlliesToTable("barliman");

		var squad = scn.GetShadowCard("squad");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(squad, legion);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(squad);
		assertEquals(1, scn.GetMoveLimit());

		// At the limit the turn ends without the question of another move, and the next turn has its own limit.
		scn.FreepsPass();
		scn.ShadowPass();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TwoFreePeoplesCharactersOfResistance5OrLessAreNeededAsTheyStandAndMinionsAreNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var squad = scn.GetShadowCard("squad");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(squad, legion);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));

		// Sam alone has resistance 5 or less; the two minions are characters, but no Free Peoples ones.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(squad));

		// One burden: Aragorn's resistance is 5 as it stands, Sam's 4.
		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));
		assertTrue(scn.ShadowActionAvailable(squad));

		assertEquals(2, scn.GetMoveLimit());
		scn.ShadowUseCardAction(squad);
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherMenMinionIsNeededAndAMenHobbitIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam");
		scn.MoveAlliesToTable("barliman");

		var squad = scn.GetShadowCard("squad");
		var soldier = scn.GetShadowCard("soldier");
		var goaded = scn.GetShadowCard("goaded");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.FreepsPass();
		// The Squad itself, a [raider] Man minion, and a [men] card that is no minion.
		scn.MoveMinionsToTable(squad, soldier);
		scn.MoveCardsToSupportArea(goaded);
		assertInZone(Zone.SUPPORT, goaded);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(squad));

		// Ted Sandyman, a [men] minion who is a Hobbit.
		scn.MoveMinionsToTable(ted);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(squad));

		assertEquals(2, scn.GetMoveLimit());
		scn.ShadowUseCardAction(squad);
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOtherCopyIsAnotherMenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam");
		scn.MoveAlliesToTable("barliman");

		var squad = scn.GetShadowCard("squad");
		var squad2 = scn.GetShadowCard("squad2");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(squad, squad2);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(squad));
		assertTrue(scn.ShadowActionAvailable(squad2));

		assertEquals(2, scn.GetMoveLimit());
		scn.ShadowUseCardAction(squad2);
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
