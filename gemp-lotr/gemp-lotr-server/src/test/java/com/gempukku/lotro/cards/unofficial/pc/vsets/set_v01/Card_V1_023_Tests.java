package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_023_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gone", "101_23");

					// Aragorn, King in Exile: Aragorn signet, strength 8.  At the start of the turn he may heal
					// another companion with his signet, so he is put on the table after the turn has started.
					put("aragorn", "1_365");
					// Aragorn, Defender of Rohan: an Aragorn with no signet, and the one Aragorn who is not a
					// [gondor] card.  Strength 8.  His text gives [rohan] companions hunter while the
					// Ring-bearer is assigned to a skirmish, and Frodo never is.
					put("rohanAragorn", "17_93");
					// Three companions with the Aragorn signet, of three cultures:
					// Boromir, Lord of Gondor: strength 7.  His text is about being overwhelmed.
					put("boromir", "1_96");
					// Arwen, Daughter of Elrond: strength 6.  Her text needs a Nazgul.
					put("arwen", "1_30");
					// Merry, From O'er the Brandywine: strength 3.  His text needs a weapon.
					put("merry", "1_303");
					// Gimli, Son of Gloin: Gandalf signet, strength 6, damage +1.  A skirmish ability, never used.
					put("gimli", "1_13");

					// Skirmish events, each of which only adds strength:
					// Swordarm of the White Tower: [gondor], cost 0.  A [gondor] companion is strength +2.
					put("swordarm", "1_116");
					put("swordarm2", "1_116");
					put("swordarm3", "1_116");
					// Defiance: [elven], cost 1.  An Elf is strength +2.
					put("defiance", "1_37");
					put("defiance2", "1_37");
					// Axe Strike: [dwarven], cost 0.  A Dwarf is strength +2.
					put("axe", "1_3");
					put("axe2", "1_3");
					// Strength of Kings: a [gondor] response event.  If a skirmish event is played during a
					// skirmish involving a [gondor] Man, cancel that event.
					put("kings", "1_115");
					// Drums in the Deep: a Shadow skirmish event, cost 0.  A [moria] Orc is strength +2.
					put("drums", "1_168");

					// Deepest Delvings: another of my conditions that cards can be stacked on.  Its text is about
					// [dwarven] cards discarded from the draw deck and a maneuver action.
					put("delvings", "101_2");

					// Uruk Savage: strength 5, vitality 3, damage +1.
					put("savage1", "1_151");
					put("savage2", "1_151");
					// Goblin Runner: a [moria] Orc, strength 5.
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IWouldHaveGoneWithYoutotheEndStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: I Would Have Gone With You to the End
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time you play a skirmish event during a skirmish involving a companion with the Aragorn
		 * 	signet, you may spot Aragorn to stack that event here.
		 * 	Skirmish: Spot Aragorn and discard X cards here to make a skirmishing companion with the Aragorn signet
		 * 	strength +X (limit +3).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gone");

		assertEquals("I Would Have Gone With You to the End", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		scn.MoveCardsToHand(gone);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInHand(gone);
		assertTrue(scn.FreepsPlayAvailable(gone));

		scn.FreepsPlayCard(gone);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, gone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachSkirmishEventPlayedInASignetCompanionsSkirmishMayBeStackedHereAndStillTakesEffect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("rohanAragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var defiance = scn.GetFreepsCard("defiance");
		var swordarm = scn.GetFreepsCard("swordarm");
		// The Aragorn who is spotted has no signet himself; the skirmish is Arwen's, who has it.
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveCardsToSupportArea(gone);
		scn.MoveCardsToHand(defiance, swordarm);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(arwen, savage);

		assertEquals(0, scn.GetStackedCards(gone).size());
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetFreepsDiscardCount());

		// An [elven] event.  Arwen is the only Elf, so it is hers.
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(8, scn.GetStrength(arwen));        // 6 + 2 from the event
		assertInZone(Zone.STACKED, defiance);
		assertEquals(1, scn.GetStackedCards(gone).size());
		assertTrue(scn.GetStackedCards(gone).contains(defiance));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A [gondor] event in the same skirmish, on Boromir, who is the only [gondor] companion and is not in it.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPlayCard(swordarm);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(9, scn.GetStrength(boromir));      // 7 + 2 from the event
		assertInZone(Zone.STACKED, swordarm);
		assertEquals(2, scn.GetStackedCards(gone).size());
		assertTrue(scn.GetStackedCards(gone).contains(swordarm));
		assertTrue(scn.GetStackedCards(gone).contains(defiance));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DecliningToStackLeavesTheEventToBeDiscardedAsUsual() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("rohanAragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(gone);
		scn.MoveCardsToHand(swordarm);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(boromir, savage);

		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(swordarm);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gone));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(9, scn.GetStrength(boromir));      // 7 + 2 from the event
		assertInDiscard(swordarm);
		assertEquals(0, scn.GetStackedCards(gone).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAragornInTheDeadPileAnEventIsNotOfferedForStackingAndTheSkirmishActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var merry = scn.GetFreepsCard("merry");
		var defiance = scn.GetFreepsCard("defiance");
		var swordarm = scn.GetFreepsCard("swordarm");
		var axe = scn.GetFreepsCard("axe");
		// Two companions with the Aragorn signet, and Aragorn himself dead: a card in the dead pile is not in
		// play and cannot be spotted.
		scn.MoveCompanionsToTable(arwen, merry);
		scn.MoveCardsToDeadPile(aragorn);
		scn.MoveCardsToSupportArea(gone);
		scn.StackCardsOn(gone, swordarm, axe);
		scn.MoveCardsToHand(defiance);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(arwen, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(2, scn.GetStackedCards(gone).size());
		assertFalse(scn.FreepsActionAvailable(gone));

		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsPlayCard(defiance);
		// Nothing is asked: the event has its effect and is discarded, and it is the Shadow player's action.
		assertEquals(8, scn.GetStrength(arwen));        // 6 + 2 from the event
		assertInDiscard(defiance);
		assertEquals(2, scn.GetStackedCards(gone).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnEventPlayedInTheSkirmishOfACompanionWithAnotherSignetIsNotOfferedAndOneInAragornsOwnSkirmishIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(gone);
		scn.MoveCardsToHand(axe, swordarm);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage1},
				new PhysicalCardImpl[]{aragorn, savage2});

		// Gimli has the Gandalf signet.  Aragorn, with his own signet, is assigned to a skirmish that has not
		// begun.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(axe);
		assertEquals(8, scn.GetStrength(gimli));        // 6 + 2 from the event
		assertInDiscard(axe);
		assertEquals(0, scn.GetStackedCards(gone).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gimli's 8 beats the Savage's 5.  Then Aragorn's own skirmish, on the same board.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(swordarm);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2 from the event
		assertInZone(Zone.STACKED, swordarm);
		assertEquals(1, scn.GetStackedCards(gone).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOpponentsSkirmishEventAndMyOwnResponseEventAreNotOfferedForStacking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("rohanAragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var kings = scn.GetFreepsCard("kings");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(gone);
		scn.MoveCardsToHand(kings);

		var runner = scn.GetShadowCard("runner");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(boromir, runner);

		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsPassCurrentPhaseAction();

		// The Shadow player's skirmish event, in the skirmish of a companion with the Aragorn signet.  I am
		// asked only whether to respond with Strength of Kings.
		scn.ShadowPlayCard(drums);
		assertTrue(scn.FreepsPlayAvailable(kings));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gone));

		// My own event, played in that skirmish, which is a response event and not a skirmish event.  It
		// cancels the Drums.
		scn.FreepsPlayCard(kings);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gone));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(5, scn.GetStrength(runner));
		assertInDiscard(kings);
		assertInDiscard(drums);
		assertEquals(0, scn.GetStackedCards(gone).size());
	}

	@Test
	public void SkirmishActionDiscardsChosenCardsFromHereToMakeTheSkirmishingSignetCompanionStrongerByThatMany() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var delvings = scn.GetFreepsCard("delvings");
		var aragorn = scn.GetFreepsCard("rohanAragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var swordarm = scn.GetFreepsCard("swordarm");
		var defiance = scn.GetFreepsCard("defiance");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveCardsToSupportArea(gone, delvings);
		scn.StackCardsOn(gone, swordarm, defiance, axe);
		// A card stacked on another of my conditions is not a card "here".
		scn.StackCardsOn(delvings, axe2);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(arwen, savage);

		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStackedCards(gone).size());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(gone));

		scn.FreepsUseCardAction(gone);
		assertTrue(scn.FreepsHasCardChoiceAvailable(swordarm, defiance, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe2));
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(swordarm, defiance);

		// X is 2.  Arwen is the only skirmishing companion with the signet.
		assertEquals(8, scn.GetStrength(arwen));        // 6 + 2
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(swordarm, defiance);
		assertEquals(1, scn.GetStackedCards(gone).size());
		assertTrue(scn.GetStackedCards(gone).contains(axe));
		assertTrue(scn.GetStackedCards(delvings).contains(axe2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NoMoreThanPlus3IsAddedInOneSkirmishPhaseAndTheLimitIsNewInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var swordarm = scn.GetFreepsCard("swordarm");
		var swordarm2 = scn.GetFreepsCard("swordarm2");
		var swordarm3 = scn.GetFreepsCard("swordarm3");
		var defiance = scn.GetFreepsCard("defiance");
		var defiance2 = scn.GetFreepsCard("defiance2");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(gone);
		scn.StackCardsOn(gone, swordarm, swordarm2, swordarm3, defiance, defiance2, axe, axe2);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{merry, savage2});

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStackedCards(gone).size());

		scn.FreepsUseCardAction(gone);
		// X may be any number of the seven cards here.  The limit is on the bonus and not on X: what is added
		// beyond +3 is ignored (CR 4.2 "limit": "the last +1 is ignored").
		assertEquals(7, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(swordarm, swordarm2);
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2
		assertEquals(5, scn.GetStackedCards(gone).size());

		// Two more cards in the same skirmish phase: only 1 of the limit of +3 is left, and the rest is ignored.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(gone);
		scn.FreepsChooseCards(defiance, defiance2);
		assertEquals(11, scn.GetStrength(aragorn));     // 8 + 3
		assertEquals(3, scn.GetStackedCards(gone).size());
		assertInDiscard(swordarm, swordarm2, defiance, defiance2);

		// Aragorn's 11 overwhelms the Savage's 5.  Merry's skirmish is a new skirmish phase.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));

		// X is 3, the whole of the limit in one use.
		scn.FreepsUseCardAction(gone);
		scn.FreepsChooseCards(swordarm3, axe, axe2);
		assertEquals(6, scn.GetStrength(merry));        // 3 + 3
		assertEquals(0, scn.GetStackedCards(gone).size());
		assertInDiscard(swordarm3, axe, axe2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OnlyTheSkirmishingCompanionWithTheAragornSignetIsMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var swordarm = scn.GetFreepsCard("swordarm");
		var defiance = scn.GetFreepsCard("defiance");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(gone);
		scn.StackCardsOn(gone, swordarm, defiance, axe);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage1},
				new PhysicalCardImpl[]{aragorn, savage2});

		// Gimli, with the Gandalf signet, is the one skirmishing.  Aragorn, with his own signet, is assigned to
		// a skirmish that has not begun.  Nobody is a skirmishing companion with the Aragorn signet.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(gone));

		// The cost is paid and there is nobody for the effect.
		scn.FreepsUseCardAction(gone);
		scn.FreepsChooseCards(swordarm);
		assertInDiscard(swordarm);
		assertEquals(2, scn.GetStackedCards(gone).size());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(savage1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gimli's 6 beats the Savage's 5.  In Aragorn's own skirmish he is the one made stronger.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(gone);
		scn.FreepsChooseCards(defiance);
		assertEquals(9, scn.GetStrength(aragorn));      // 8 + 1
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(defiance);
		assertEquals(1, scn.GetStackedCards(gone).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnEventStackedInAragornsSkirmishCanBeDiscardedInTheSameSkirmishForPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gone = scn.GetFreepsCard("gone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var swordarm = scn.GetFreepsCard("swordarm");
		var delvings = scn.GetFreepsCard("delvings");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToSupportArea(gone, delvings);
		// A card stacked on another of my conditions, and none here.
		scn.StackCardsOn(delvings, axe);
		scn.MoveCardsToHand(swordarm);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, savage);

		// With nothing stacked here the action is not offered.
		assertEquals(0, scn.GetStackedCards(gone).size());
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.FreepsActionAvailable(gone));

		scn.FreepsPlayCard(swordarm);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2 from the event
		assertEquals(1, scn.GetStackedCards(gone).size());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsActionAvailable(gone));
		scn.FreepsUseCardAction(gone);
		assertEquals(11, scn.GetStrength(aragorn));     // 8 + 2 from the event + 1 for the card discarded
		assertEquals(0, scn.GetStackedCards(gone).size());
		assertInDiscard(swordarm);

		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gone));
		assertTrue(scn.GetStackedCards(delvings).contains(axe));
	}
}
