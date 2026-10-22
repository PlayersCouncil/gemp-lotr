package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fate", "19_14");
					put("fate2", "19_14");
					put("boromir", "1_97");       // Boromir, Son of Denethor: a [gondor] Man companion, strength 7; a skirmish ability never used
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability never used
					put("merchant", "7_97");      // Gondorian Merchant: the [gondor] Man ally; a maneuver ability never used
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability never used
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion, strength 6; valiant is its whole text

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("pursuer", "4_190");      // Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits discarded or killed
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotBoundtoHisFateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Not Bound to His Fate
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Strength: +1
		 * Game Text: Bearer must be a [gondor] Man.
		 * 	Each time bearer wins a skirmish, you may transfer this condition to another [gondor] Man.
		 * 	At the end of each turn, if bearer is not Aragorn, return this condition to hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fate");

		assertEquals("Not Bound to His Fate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(fate);
		scn.MoveCompanionsToTable(boromir, aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);

		assertInZone(Zone.HAND, fate);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, merchant);
		assertTrue(scn.FreepsPlayAvailable(fate));

		scn.FreepsPlayCard(fate);
		// Two [gondor] Man companions and the [gondor] Man ally; not the [gondor] Hobbit, the [rohan] Man or Frodo.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));

		scn.FreepsChooseCard(boromir);
		assertAttachedTo(fate, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishItMayBeTransferredToAnotherGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var fate2 = scn.GetFreepsCard("fate2");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(boromir, aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		// A second copy on a [gondor] Man who fights no skirmish.
		scn.AttachCardsTo(boromir, fate);
		scn.AttachCardsTo(aragorn, fate2);

		assertAttachedTo(fate, boromir);
		assertAttachedTo(fate2, aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { guard, savage }, new PhysicalCardImpl[] { boromir, pursuer });

		// Household Guard (6), who bears no copy, beats Uruk Savage (5): nothing is offered.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Boromir (7 + 1) beats Uruk Pursuer (5), who lives through it: only the copy he bears is offered.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fate));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fate2));

		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(fate, boromir);
		assertAttachedTo(fate2, aragorn);

		// The Pursuer is fierce: Boromir beats him a second time, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fate));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fate2));

		scn.FreepsAcceptOptionalTrigger();
		// Another [gondor] Man, companion or ally: not the bearer, the [gondor] Hobbit, the [rohan] Man or Frodo.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, pippin, guard, frodo));

		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(fate, aragorn);
		assertAttachedTo(fate2, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, fate);

		assertAttachedTo(fate, boromir);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();

		// Goblin Patrol Troop (13) beats Boromir (7 + 1) without overwhelming him.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(fate, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheBearerIsTheOnlyGondorManTheTransferIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		// A [gondor] companion who is no Man and a Man who is not [gondor], neither able to bear it.
		scn.MoveCompanionsToTable(boromir, pippin, guard);
		scn.AttachCardsTo(boromir, fate);

		assertAttachedTo(fate, boromir);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();

		// Boromir (7 + 1) beats Uruk Savage (5), and no other [gondor] Man could bear the condition.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(fate, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheEndOfTheTurnItReturnsToHandUnlessAragornBearsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var fate2 = scn.GetFreepsCard("fate2");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, fate);
		scn.AttachCardsTo(aragorn, fate2);

		scn.StartGame();

		assertAttachedTo(fate, boromir);
		assertAttachedTo(fate2, aragorn);

		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The regroup phase is over, and the turn is not: both copies are where they were.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(fate, boromir);
		assertAttachedTo(fate2, aragorn);

		scn.SkipToOtherPlayersTurn();

		// Boromir's copy is back in hand, though Aragorn is in play; Aragorn's stays.
		assertInZone(Zone.HAND, fate);
		assertAttachedTo(fate2, aragorn);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
