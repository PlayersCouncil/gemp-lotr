package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_028_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("machinery1", "102_28");
					put("machinery2", "102_28");

					// Saruman, Servant of the Eye: an [isengard] Wizard minion who may not be assigned to a skirmish
					put("saruman", "3_69");
					// [isengard] Orcs with regroup abilities that are only ever used on purpose
					put("worker1", "3_62");      // Isengard Worker: strength 5, vitality 3
					put("worker2", "3_62");
					put("worker3", "3_62");
					put("servant", "3_58");      // Isengard Servant: strength 7, vitality 3
					put("retainer", "3_57");     // Isengard Retainer: strength 8, vitality 3
					put("savage", "1_151");      // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, strength 5, vitality 1

					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard1", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text once in play
					put("guard2", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A format in which a skirmish involving the Ring-bearer may be cancelled.  The default one does not allow it.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("machinery1", "102_28");
					put("worker1", "3_62");      // Isengard Worker: an [isengard] Orc, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void MachineryofWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Machinery of War
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, spot a Wizard to add 2 [isengard] tokens here.
		 * 	Each time an [isengard] Orc loses a skirmish, add an [isengard] token here.
		 * 	Skirmish: Discard this condition (or remove 2 [isengard] tokens here) to cancel a skirmish involving an [isengard] Orc.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("machinery1");

		assertEquals("Machinery of War", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForOneToTheSupportAreaAndWithNoWizardInPlayGetsNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker1");
		var theirGandalf = scn.GetFreepsCard("gandalf");
		// A Wizard in each player's hand, and none in play.
		scn.MoveCardsToHand(machinery, saruman, theirGandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(machinery));

		scn.ShadowPlayCard(machinery);
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, machinery);
		assertEquals(0, scn.GetCultureTokensOn(machinery));
		assertInZone(Zone.HAND, saruman, theirGandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedWithSarumanToSpotGetsTwoTokensAndTheCopyAlreadyInPlayGetsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inPlay = scn.GetShadowCard("machinery1");
		var played = scn.GetShadowCard("machinery2");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(inPlay);
		scn.MoveCardsToHand(played);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(inPlay));
		assertEquals(0, scn.GetCultureTokensOn(played));

		scn.ShadowPlayCard(played);
		assertInZone(Zone.SUPPORT, played);
		assertEquals(2, scn.GetCultureTokensOn(played));
		assertEquals(0, scn.GetCultureTokensOn(inPlay));
		// There is no "may": nothing is asked.
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedWithTheOtherPlayersGandalfToSpotGetsTwoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var worker = scn.GetShadowCard("worker1");
		var theirGandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(machinery);
		scn.MoveCompanionsToTable(theirGandalf);

		scn.StartGame();
		// The Shadow player's only card in play is an [isengard] Orc, who is no Wizard.
		scn.MoveMinionsToTable(worker);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(machinery));

		scn.ShadowPlayCard(machinery);
		assertInZone(Zone.SUPPORT, machinery);
		assertEquals(2, scn.GetCultureTokensOn(machinery));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachIsengardOrcThatLosesASkirmishAddsATokenHereAndOneThatWinsAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var retainer = scn.GetShadowCard("retainer");
		var worker1 = scn.GetShadowCard("worker1");
		var worker2 = scn.GetShadowCard("worker2");
		var worker3 = scn.GetShadowCard("worker3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToSupportArea(machinery);
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(retainer, worker1, worker2, worker3);
		// Each Dwarf Guard is 4 + 8 = 12: more than two Workers' 5 + 5 and less than double it, and more than
		// double one Worker's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard1, null, 8));
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard2, null, 8));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, retainer},
				new PhysicalCardImpl[]{guard1, worker1},
				new PhysicalCardImpl[]{guard2, worker3});
		scn.ShadowAssignToMinions(guard1, worker2);
		assertEquals(0, scn.GetCultureTokensOn(machinery));

		// An [isengard] Orc wins: 8 against Aragorn's 8, and a tie is the Shadow's.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(retainer));
		assertEquals(0, scn.GetCultureTokensOn(machinery));

		// Two [isengard] Orcs lose one skirmish, 10 to 12: a token for each.
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(12, scn.GetStrength(guard1));
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(worker1));
		assertEquals(1, scn.GetWoundsOn(worker2));
		assertEquals(2, scn.GetCultureTokensOn(machinery));

		// An [isengard] Orc that loses by being overwhelmed, 5 to 12, has lost all the same.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, worker3);
		assertEquals(3, scn.GetCultureTokensOn(machinery));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnIsengardUrukHaiOrAMoriaOrcThatLosesAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var worker = scn.GetShadowCard("worker1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToSupportArea(machinery);
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, worker);
		// Each Dwarf Guard is 4 + 4 = 8: more than 5 and less than double it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard1, null, 4));
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard2, null, 4));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{guard1, runner},
				new PhysicalCardImpl[]{guard2, worker});
		assertEquals(0, scn.GetCultureTokensOn(machinery));

		// An [isengard] minion that is no Orc loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetCultureTokensOn(machinery));

		// An Orc that is not an [isengard] card loses.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetCultureTokensOn(machinery));

		// And then an [isengard] Orc does.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetCultureTokensOn(machinery));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCopyInPlayGetsATokenOfItsOwnWhenAnIsengardOrcLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery1 = scn.GetShadowCard("machinery1");
		var machinery2 = scn.GetShadowCard("machinery2");
		var worker = scn.GetShadowCard("worker1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(machinery1, machinery2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, worker);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetCultureTokensOn(machinery1));
		assertEquals(0, scn.GetCultureTokensOn(machinery2));
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// 5 against 8
		assertEquals(1, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetCultureTokensOn(machinery1));
		assertEquals(1, scn.GetCultureTokensOn(machinery2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RemovesTwoTokensHereToCancelASkirmishInvolvingAnIsengardOrcAndWithOnlyOneLeftIsDiscardedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var worker = scn.GetShadowCard("worker1");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCardsToSupportArea(machinery);
		scn.AddTokensToCard(machinery, 3);
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, savage, servant);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: nothing is offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(machinery));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, worker}, new PhysicalCardImpl[]{guard, servant});
		// An [isengard] Uruk-hai fights beside the Worker: the skirmish involves an [isengard] Orc all the same.
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(worker));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertEquals(3, scn.GetCultureTokensOn(machinery));
		assertTrue(scn.ShadowActionAvailable(machinery));

		scn.ShadowUseCardAction(machinery);
		// With 2 tokens here they are what is paid: the player is not asked which, and the condition stays.
		assertEquals(1, scn.GetCultureTokensOn(machinery));
		assertInZone(Zone.SUPPORT, machinery);
		// The skirmish is over for everyone in it, with no winner or loser: no wound, and no token for the Worker.
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(worker));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(worker));
		assertFalse(scn.IsCharAssigned(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(savage));

		// One token is not two: this time the condition itself is discarded.
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(servant));
		assertEquals(1, scn.GetCultureTokensOn(machinery));
		assertTrue(scn.ShadowActionAvailable(machinery));

		scn.ShadowUseCardAction(machinery);
		assertInZone(Zone.DISCARD, machinery);
		assertFalse(scn.IsCharSkirmishing(guard));
		assertFalse(scn.IsCharSkirmishing(servant));
		assertFalse(scn.IsCharAssigned(servant));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithExactlyTwoTokensHereTheConditionStaysAndTokensOnAnotherCopyAreNotHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bare = scn.GetShadowCard("machinery1");
		var loaded = scn.GetShadowCard("machinery2");
		var worker1 = scn.GetShadowCard("worker1");
		var worker2 = scn.GetShadowCard("worker2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCardsToSupportArea(bare, loaded);
		scn.AddTokensToCard(loaded, 2);
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(worker1, worker2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, worker1}, new PhysicalCardImpl[]{guard, worker2});

		// The copy with no tokens is used: it is discarded, and the 2 tokens on the other copy are left alone.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetCultureTokensOn(bare));
		assertEquals(2, scn.GetCultureTokensOn(loaded));
		assertTrue(scn.ShadowActionAvailable(bare));
		assertTrue(scn.ShadowActionAvailable(loaded));

		scn.ShadowUseCardAction(bare);
		assertInZone(Zone.DISCARD, bare);
		assertInZone(Zone.SUPPORT, loaded);
		assertEquals(2, scn.GetCultureTokensOn(loaded));
		assertFalse(scn.IsCharSkirmishing(worker1));
		assertEquals(0, scn.GetWoundsOn(worker1));

		// The copy with exactly 2 is used: both are removed and it stays.
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(worker2));

		scn.ShadowUseCardAction(loaded);
		assertEquals(0, scn.GetCultureTokensOn(loaded));
		assertInZone(Zone.SUPPORT, loaded);
		assertFalse(scn.IsCharSkirmishing(worker2));
		assertEquals(0, scn.GetWoundsOn(worker2));
		assertEquals(0, scn.GetWoundsOn(guard));

		// And it is a skirmish ability: in the regroup phase that follows nothing is offered.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(loaded));
	}

	@Test
	public void InASkirmishWithNoIsengardOrcTheCostIsPaidAndNothingIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var worker = scn.GetShadowCard("worker1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCardsToSupportArea(machinery);
		scn.AddTokensToCard(machinery, 3);
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		// The Worker is an [isengard] Orc in play and in neither skirmish.
		scn.MoveMinionsToTable(savage, runner, worker);
		// 4 + 4 = 8
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 4));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{guard, runner});
		scn.ShadowDeclineAssignments();

		// An [isengard] Uruk-hai alone.  The ability has a cost and that can be paid, so it is there to be used.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(3, scn.GetCultureTokensOn(machinery));
		assertTrue(scn.ShadowActionAvailable(machinery));

		scn.ShadowUseCardAction(machinery);
		assertEquals(1, scn.GetCultureTokensOn(machinery));
		assertInZone(Zone.SUPPORT, machinery);
		// Nothing is cancelled: the skirmish goes on and has a loser.
		assertTrue(scn.IsCharSkirmishing(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(machinery));

		// A [moria] Orc alone, and one token here: the condition is discarded for nothing.
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(machinery));

		scn.ShadowUseCardAction(machinery);
		assertInZone(Zone.DISCARD, machinery);
		assertTrue(scn.IsCharSkirmishing(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InFellowshipBlockCancelsASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var worker = scn.GetShadowCard("worker1");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(machinery);
		scn.AddTokensToCard(machinery, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, worker);

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.ShadowActionAvailable(machinery));

		scn.ShadowUseCardAction(machinery);
		assertEquals(0, scn.GetCultureTokensOn(machinery));
		assertInZone(Zone.SUPPORT, machinery);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertFalse(scn.IsCharSkirmishing(worker));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathPaysItsCostAndDoesNotCancelASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var machinery = scn.GetShadowCard("machinery1");
		var worker = scn.GetShadowCard("worker1");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(machinery);
		scn.AddTokensToCard(machinery, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, worker);

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(frodo));
		// The cost can be paid, so the ability is there to be used.
		assertTrue(scn.ShadowActionAvailable(machinery));

		scn.ShadowUseCardAction(machinery);
		assertEquals(0, scn.GetCultureTokensOn(machinery));
		assertInZone(Zone.SUPPORT, machinery);
		// The skirmish goes on.
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.IsCharSkirmishing(worker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		// Frodo loses, 3 + 1 for the Ring against 5, and is offered the Ring for the wound.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(worker));
		// The Worker won: no token.
		assertEquals(0, scn.GetCultureTokensOn(machinery));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
