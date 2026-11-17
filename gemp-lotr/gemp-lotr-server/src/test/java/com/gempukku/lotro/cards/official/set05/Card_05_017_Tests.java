package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guardian", "5_17");
					put("guardian2", "5_17");   // a second copy: "this companion" is the one whose ability is used
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] unbound Hobbit, vitality 4; no weapon here, so no text
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] unbound Hobbit, vitality 4; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion who is no Hobbit; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally (no ally is unbound); a skirmish ability used only on purpose
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text at all
					put("troop2", "1_177");
					put("card1", "1_177");      // cards in hand to discard
					put("card2", "1_177");
					put("handcompanion", "1_89"); // Aragorn, Ranger of the North: a Free Peoples companion, held in hand
					put("handminion", "1_178");   // Goblin Runner: a Shadow minion, held in hand
					put("handpossession", "1_299"); // Hobbit Sword: a [shire] possession, held in hand
					put("handevent", "1_293");    // Halfling Deftness: a Skirmish event, held in hand (neither character nor item)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForestGuardianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Forest Guardian
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Unhasty</b>.<br><b>Assignment:</b> Exert an unbound Hobbit or discard 2 cards from hand to allow this companion to skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guardian");

		assertEquals("Forest Guardian", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.UNHASTY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AssignmentAbilityExertsAnUnboundHobbitToLetTheFreePeoplesPlayerAssignThisGuardian() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guardian = scn.GetFreepsCard("guardian");
		var guardian2 = scn.GetFreepsCard("guardian2");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(guardian, guardian2, merry, pippin, sam, gimli);
		scn.MoveAlliesToTable(bounder);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Unhasty: neither copy is the Free Peoples player's to assign.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsCanAssign(guardian));
		assertFalse(scn.FreepsCanAssign(guardian2));
		assertTrue(scn.FreepsActionAvailable(guardian));

		// With no cards in hand the cost is the exertion.
		scn.FreepsUseCardAction(guardian);
		// Unbound Hobbits of two cultures are offered; the Ring-bearer, a Ring-bound Sam, a Hobbit ally and an
		// unbound companion who is no Hobbit are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, gimli, guardian2));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.HasKeyword(guardian, Keyword.UNHASTY));
		assertTrue(scn.FreepsCanAssign(guardian));
		// Only the copy whose ability was used is allowed.
		assertFalse(scn.FreepsCanAssign(guardian2));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(guardian));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(guardian2));
		scn.FreepsAssignToMinions(guardian, troop1);
		assertTrue(scn.IsCharAssignedAgainst(guardian, troop1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithBothCostsPayableThePlayerIsAskedWhichAndMayDiscardTwoCardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		var companion = scn.GetFreepsCard("handcompanion");
		var minion = scn.GetFreepsCard("handminion");
		var possession = scn.GetFreepsCard("handpossession");
		var event = scn.GetFreepsCard("handevent");
		scn.MoveCompanionsToTable(guardian, merry);
		scn.MoveCardsToHand(companion, minion, possession, event);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		assertInZone(Zone.HAND, companion, minion, possession, event);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsCanAssign(guardian));
		scn.FreepsUseCardAction(guardian);

		assertEquals(List.of("Exert an unbound Hobbit", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Discard 2 cards from hand");
		// Any two cards of the hand: a companion, a Shadow minion, a possession and an event are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(companion, minion, possession, event));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(companion, event);

		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, companion, event);
		assertInZone(Zone.HAND, minion, possession);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(guardian));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitAbleToExertTheTwoCardsAreDiscardedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		scn.MoveCompanionsToTable(guardian, merry);
		scn.MoveCardsToHand(card1, card2);
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(merry, 3);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		assertInZone(Zone.HAND, card1, card2);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsCanAssign(guardian));
		assertTrue(scn.FreepsActionAvailable(guardian));
		scn.FreepsUseCardAction(guardian);

		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, card1, card2);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(guardian));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWithNoUnboundHobbitAbleToExertAndOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var card1 = scn.GetFreepsCard("card1");
		scn.MoveCompanionsToTable(guardian, merry, sam);
		scn.MoveCardsToHand(card1);
		// Merry exhausted; Sam, who could exert, is Ring-bound.
		scn.AddWoundsToChar(merry, 3);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guardian));
		assertFalse(scn.FreepsCanAssign(guardian));
	}

	@Test
	public void TheAllowanceLastsTheAssignmentPhaseItWasUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(guardian, merry);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsUseCardAction(guardian);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(guardian));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(guardian, troop1);
		assertTrue(scn.IsCharAssignedAgainst(guardian, troop1));

		// The Guardian, 8, loses to the Troop's 13 and lives (vitality 3).
		assertEquals(0, scn.GetWoundsOn(guardian));
		scn.FreepsResolveSkirmish(guardian);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guardian));

		// A second move, and a second assignment phase in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(troop2);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(guardian));
		// Offered again, Merry able to exert once more.
		assertTrue(scn.FreepsActionAvailable(guardian));
	}
}
