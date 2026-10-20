package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Optional actions that answer one trigger.
 *
 * Comprehensive Rules 4.2, "action": "All required actions responding to a particular trigger are performed
 * before any optional actions.  After all such required actions have resolved, players may perform optional
 * actions responding to that same trigger using the action procedure."
 *
 * So when one moment (the start of a turn, a skirmish won, the start of the regroup phase) is answered by a
 * "you may" on more than one card of a player's, that player is given all of them in one choice, and taking one
 * leaves the others on offer.  They are taken one at a time, so each is judged when its turn comes, on the board
 * the one before it left, and not once for all when the moment arrives.
 *
 * The offer a keyword makes is one of them.  Comprehensive Rules 4.2, "muster": "At the start of the regroup
 * phase, for each of your characters who has the muster keyword, you may discard a card from hand, then draw a
 * card."  A card with muster and a start-of-regroup "you may" of its own makes two offers, and neither uses the
 * other up.
 */
public class OptionalActionsAnsweringOneTriggerTests
{
	protected VirtualTableScenario GetStartOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, King in Exile: "At the start of each of your turns, you may heal another companion
					// who has the Aragorn signet."
					put("aragorn", "1_365");
					// Gandalf, The Grey Pilgrim: "At the start of each of your turns, you may draw a card."  He
					// has the Aragorn signet and vitality 4.
					put("gandalf", "2_122");
					put("axe", "1_3");          // Axe Strike: the card on top of the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishWinScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Skilled Defender: strength 6, damage +1.  "Each time Gimli wins a skirmish, you may
					// wound a minion assigned to skirmish an unbound companion."
					put("gimli", "5_7");
					// Stout and Strong, in the support area: "Each time a Dwarf wins a skirmish, you may place a
					// [dwarven] token on this card."  Its skirmish ability is never used.
					put("stout", "4_57");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, strength 8, vitality 4
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; Gimli beats and kills one in each round
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text; the minion Aragorn fights
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetMusterScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dervorin: "At the start of the regroup phase, if you have only 4 cards in hand, you may spot
					// another [gondor] Man to discard a minion."  The tests give him muster.
					put("dervorin", "7_88");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: the other [gondor] Man
					put("axe1", "1_3");         // Axe Strike: a skirmish event that needs a Dwarf; five cards to hold
					put("axe2", "1_3");
					put("axe3", "1_3");
					put("axe4", "1_3");
					put("axe5", "1_3");
					put("guard", "1_7");        // Dwarf Guard: a card for the draw deck
					put("troop", "1_177");      // Goblin Patrol Troop: the minion Dervorin's text may discard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TwoStartOfTurnMaysAreOfferedTogetherAndTheOtherIsStillThereOnceOneIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartOfTurnScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		// Somebody for Aragorn to heal, with a wound more than one heal takes off
		scn.AddWoundsToChar(gandalf, 2);

		// The turn begins, and the game stops at the two offers.
		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axe);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		// Gandalf's is taken: a card is drawn, nobody is healed yet, and Aragorn's is still on offer.
		scn.FreepsChooseOptionalTrigger(gandalf);
		assertInZone(Zone.HAND, axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));

		// Gandalf is the only wounded companion with the Aragorn signet.
		scn.FreepsChooseOptionalTrigger(aragorn);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCardsThatAnswerOneSkirmishWinAreOfferedTogetherAndEachIsStillThereOnceTheOtherIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishWinScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(stout);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, troop);
		// A second round of skirmishes, so that Gimli wins twice and the two offers can be taken in each order:
		// the second Runner sits the first round out and fights Gimli in the second.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, runner2, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner1}, new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowDeclineAssignments();

		// Gimli 6 against the Runner's 5
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The wound for losing, which is required, has been placed before anything optional is offered.  Then
		// both cards that answer the win are on offer at once.
		assertInZone(Zone.DISCARD, runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stout));
		assertEquals(0, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Stout and Strong's first: Gimli's is still there.
		scn.FreepsChooseOptionalTrigger(stout);
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stout));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));

		// The Troop, waiting on Aragorn, is the one minion assigned to skirmish an unbound companion.
		scn.FreepsChooseOptionalTrigger(gimli);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Aragorn 8 against the Troop's 13: he loses and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce round: the second Runner to Gimli, the Troop to Aragorn again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner2}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stout));

		// Gimli's first this time: Stout and Strong's is still there.
		scn.FreepsChooseOptionalTrigger(gimli);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stout));

		scn.FreepsChooseOptionalTrigger(stout);
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void MusterAndTheSameCardsOwnStartOfRegroupTriggerAreTwoOffersAndTakingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMusterScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		var axe1 = scn.GetFreepsCard("axe1");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(axe1, scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Muster on the very card that has a start-of-regroup text of its own: both offers come from one card.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, dervorin, null, Keyword.MUSTER));
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToTopOfDeck(guard);
		// Nobody is assigned to the Troop, so it is still in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Optional trigger"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Use Muster"));

		// Dervorin's own is taken, and muster is still on offer.
		scn.FreepsChooseOptionalTrigger(dervorin, "Optional trigger");
		assertInZone(Zone.DISCARD, troop);
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Optional trigger"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Use Muster"));

		// One card discarded and one drawn
		scn.FreepsChooseOptionalTrigger(dervorin, "Use Muster");
		scn.FreepsChooseCard(axe1);
		assertInZone(Zone.DISCARD, axe1);
		assertInZone(Zone.HAND, guard);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFiveCardsInHandMusterDiscardingOneBringsTheOnlyFourCardsTriggerOnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMusterScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		var axe1 = scn.GetFreepsCard("axe1");
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(axe1, scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"),
				scn.GetFreepsCard("axe5"));
		// An empty draw deck: muster then discards a card and draws none, and the hand is a card smaller.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("guard"), scn.GetFreepsCard("troop"));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, dervorin, null, Keyword.MUSTER));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		// Five cards in hand are not "only 4": muster alone is offered.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Use Muster"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Optional trigger"));

		scn.FreepsChooseOptionalTrigger(dervorin, "Use Muster");
		scn.FreepsChooseCard(axe1);
		assertInZone(Zone.DISCARD, axe1);
		assertEquals(4, scn.GetFreepsHandCount());

		// Now there are only 4, and Dervorin's own text is offered, though the phase started with 5.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Optional trigger"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Use Muster"));

		scn.FreepsChooseOptionalTrigger(dervorin, "Optional trigger");
		assertInZone(Zone.DISCARD, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCardsInHandMusterDiscardingOneTakesTheOnlyFourCardsTriggerOffOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMusterScenario();

		var dervorin = scn.GetFreepsCard("dervorin");
		var boromir = scn.GetFreepsCard("boromir");
		var axe1 = scn.GetFreepsCard("axe1");
		scn.MoveCompanionsToTable(dervorin, boromir);
		scn.MoveCardsToHand(axe1, scn.GetFreepsCard("axe2"), scn.GetFreepsCard("axe3"), scn.GetFreepsCard("axe4"));
		// An empty draw deck: muster then discards a card and draws none, and the hand is a card smaller.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("axe5"), scn.GetFreepsCard("guard"), scn.GetFreepsCard("troop"));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, dervorin, null, Keyword.MUSTER));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Use Muster"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Optional trigger"));

		// Muster first: three cards are left, and Dervorin's own text, on offer a moment ago, is offered no more.
		scn.FreepsChooseOptionalTrigger(dervorin, "Use Muster");
		scn.FreepsChooseCard(axe1);
		assertInZone(Zone.DISCARD, axe1);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dervorin, "Optional trigger"));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
