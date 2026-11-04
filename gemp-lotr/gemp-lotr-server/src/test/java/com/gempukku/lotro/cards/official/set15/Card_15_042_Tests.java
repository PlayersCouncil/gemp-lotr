package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_042_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("move", "15_42");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability only, never used
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: the [gollum] minion who is no Gollum (a Spider); enduring, fierce
					put("stole", "6_46");       // They Stole It: a [gollum] condition in the support area; a skirmish ability, never used
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion; its trigger is on its play only, and it is cheated in

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability, never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability, never used
					put("smeagol", "6_45");     // Sméagol, Poor Creature: the [gollum] companion, a Free Peoples [gollum] card who is no minion
					put("bounder", "1_286");    // Bounder: Hobbit ally; a skirmish ability, never used
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability, never used
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf; +3 only against a Nazgûl
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("secret", "1_71");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesperateMoveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Desperate Move
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Discard a [gollum] minion from play to discard a companion. The Free Peoples player may make the move limit -1 this turn to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("move");

		assertEquals("Desperate Move", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAGollumMinionToDiscardACompanionWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var move = scn.GetShadowCard("move");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var stole = scn.GetShadowCard("stole");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(move);

		scn.StartGame();
		// The second move of the turn is made before it is played: the move limit is reached, and the
		// prevention is offered all the same.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());

		scn.MoveMinionsToTable(gollum, shelob, runner);
		scn.MoveCardsToSupportArea(stole);
		assertInZone(Zone.SUPPORT, stole);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Shelob is fierce: the fierce assignment phase follows, and its assignments are declined as well.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, gollum, shelob, runner);
		assertInZone(Zone.SUPPORT, stole);
		assertTrue(scn.ShadowPlayAvailable(move));
		scn.ShadowPlayCard(move);

		// The cost: either [gollum] minion, of two titles; neither the [moria] minion, nor the [gollum] condition,
		// nor the [gollum] companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, stole, smeagol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gollum);

		assertInDiscard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsChooseNo();

		// Any companion; the ally and the minions are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, smeagol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder, runner, shelob));
		scn.ShadowChooseCard(gimli);

		assertInDiscard(gimli, move);
		assertInZone(Zone.FREE_CHARACTERS, aragorn, smeagol, frodo);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGollumMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var move = scn.GetShadowCard("move");
		var shelob = scn.GetShadowCard("shelob");
		var stole = scn.GetShadowCard("stole");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(move, shelob);
		assertInHand(move, shelob);

		scn.StartGame();
		assertInHand(move, shelob);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(stole);
		assertInZone(Zone.SUPPORT, stole);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// A [gollum] minion in hand, a [gollum] card in play that is no minion, and a minion of another culture.
		assertInHand(move, shelob);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, stole);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(move));
	}

	@Test
	public void WithTheRingBearerTheOnlyCompanionItDiscardsNobodyAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var move = scn.GetShadowCard("move");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(move);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.ShadowPlayAvailable(move));
		scn.ShadowPlayCard(move);

		// The Ring-bearer is never discarded: the cost is paid for nothing, and the Free Peoples player is not
		// asked to prevent what will not happen.
		assertInDiscard(gollum, move);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventedByTheMoveLimitMinusOneForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var move = scn.GetShadowCard("move");
		var gollum = scn.GetShadowCard("gollum");
		var gimli = scn.GetFreepsCard("gimli");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("gandalf", "arwen");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(secret);
		scn.MoveCardsToHand(move);

		scn.StartGame();
		// The limit cannot read below 1, so from 2 a penalty of 1 and of 2 read the same: raise it first.
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit());

		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		scn.ShadowPlayCard(move);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// 2 + 1 - 1.  The Gollum minion paid for it all the same, and nobody is discarded.
		assertEquals(2, scn.GetMoveLimit());
		assertInDiscard(gollum, move);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// One move made of two: the fellowship moves again, and the penalty stands for the turn.
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// On the next turn the limit is its own again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}
}
