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

public class Card_05_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("birchseed", "5_15");
					put("guardian", "5_17");    // Forest Guardian: another unhasty Ent companion with the same assignment ability for itself
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
	public void BirchseedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Birchseed, Tall Statesman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Unhasty</b>.<br><b>Assignment:</b> Exert an unbound Hobbit or discard 2 cards from hand to allow Birchseed to skirmish.<br><b>Fellowship:</b> Exert Birchseed to heal an unbound Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("birchseed");

		assertEquals("Birchseed", card.getBlueprint().getTitle());
		assertEquals("Tall Statesman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.UNHASTY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AssignmentAbilityExertsAnUnboundHobbitToLetTheFreePeoplesPlayerAssignBirchseed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var birchseed = scn.GetFreepsCard("birchseed");
		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(birchseed, guardian, merry, pippin, sam, gimli);
		scn.MoveAlliesToTable(bounder);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Unhasty: not the Free Peoples player's to assign, nor is the other unhasty Ent.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsCanAssign(birchseed));
		assertFalse(scn.FreepsCanAssign(guardian));
		assertTrue(scn.FreepsActionAvailable(birchseed));

		// With no cards in hand the cost is the exertion.
		scn.FreepsUseCardAction(birchseed);
		// Unbound Hobbits of two cultures are offered; the Ring-bearer, a Ring-bound Sam, a Hobbit ally and an
		// unbound companion who is no Hobbit are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.HasKeyword(birchseed, Keyword.UNHASTY));
		assertTrue(scn.FreepsCanAssign(birchseed));
		// Only Birchseed is allowed: the other unhasty Ent is still not his to assign.
		assertFalse(scn.FreepsCanAssign(guardian));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(birchseed));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(guardian));
		scn.FreepsAssignToMinions(birchseed, troop1);
		assertTrue(scn.IsCharAssignedAgainst(birchseed, troop1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithBothCostsPayableThePlayerIsAskedWhichAndMayDiscardTwoCardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birchseed = scn.GetFreepsCard("birchseed");
		var merry = scn.GetFreepsCard("merry");
		var companion = scn.GetFreepsCard("handcompanion");
		var minion = scn.GetFreepsCard("handminion");
		var possession = scn.GetFreepsCard("handpossession");
		var event = scn.GetFreepsCard("handevent");
		scn.MoveCompanionsToTable(birchseed, merry);
		scn.MoveCardsToHand(companion, minion, possession, event);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		assertInZone(Zone.HAND, companion, minion, possession, event);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsCanAssign(birchseed));
		scn.FreepsUseCardAction(birchseed);

		assertEquals(List.of("Exert an unbound Hobbit", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Discard 2 cards from hand");
		// Any two cards of the hand: a companion, a Shadow minion, a possession and an event are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(companion, minion, possession, event));
		scn.FreepsChooseCards(companion, event);

		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, companion, event);
		assertInZone(Zone.HAND, minion, possession);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(birchseed));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitAbleToExertTheTwoCardsAreDiscardedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birchseed = scn.GetFreepsCard("birchseed");
		var merry = scn.GetFreepsCard("merry");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		scn.MoveCompanionsToTable(birchseed, merry);
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
		assertFalse(scn.FreepsCanAssign(birchseed));
		assertTrue(scn.FreepsActionAvailable(birchseed));
		scn.FreepsUseCardAction(birchseed);

		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, card1, card2);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(birchseed));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWithNoUnboundHobbitAbleToExertAndOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birchseed = scn.GetFreepsCard("birchseed");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var card1 = scn.GetFreepsCard("card1");
		scn.MoveCompanionsToTable(birchseed, merry, sam);
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
		assertFalse(scn.FreepsActionAvailable(birchseed));
		assertFalse(scn.FreepsCanAssign(birchseed));
	}

	@Test
	public void TheAllowanceLastsTheAssignmentPhaseItWasUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birchseed = scn.GetFreepsCard("birchseed");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(birchseed, merry);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsUseCardAction(birchseed);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(birchseed));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(birchseed, troop1);
		assertTrue(scn.IsCharAssignedAgainst(birchseed, troop1));

		// Birchseed, 8, loses to the Troop's 13 and lives.
		scn.FreepsResolveSkirmish(birchseed);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(birchseed));

		// A second move, and a second assignment phase in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(troop2);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(birchseed));
		// Offered again, Merry able to exert once more.
		assertTrue(scn.FreepsActionAvailable(birchseed));
	}

	@Test
	public void FellowshipAbilityExertsBirchseedToHealAnUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var birchseed = scn.GetFreepsCard("birchseed");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(birchseed, merry, pippin, sam, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(birchseed));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsActionAvailable(birchseed));
		scn.FreepsUseCardAction(birchseed);

		assertEquals(1, scn.GetWoundsOn(birchseed));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, gimli, bounder));
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileBirchseedIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var birchseed = scn.GetFreepsCard("birchseed");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(birchseed, merry);
		scn.AddWoundsToChar(merry, 1);
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(birchseed, 3);

		scn.StartGame();

		assertEquals(3, scn.GetWoundsOn(birchseed));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(birchseed));
	}

	@Test
	public void WithNoUnboundHobbitWoundedTheFellowshipAbilityIsOfferedAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var birchseed = scn.GetFreepsCard("birchseed");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(birchseed, merry, sam);
		// Only Hobbits who are not unbound are wounded.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(birchseed));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(birchseed));
		scn.FreepsUseCardAction(birchseed);

		assertEquals(1, scn.GetWoundsOn(birchseed));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
