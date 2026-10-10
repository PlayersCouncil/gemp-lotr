package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "Instead".
 *
 * Comprehensive Rules 4.2, "instead": "When a card uses the phrase 'instead' or 'instead of', the stated effect is
 * replaced with a different effect.  This does not mean that the original effect is prevented."
 *
 * Ketura (2026-10-09): "The main thing that makes 'instead' interesting is that it causes costs to be paid via an
 * alternate currency--not preventing or canceling the effect, just providing a conversion."
 *
 * Two things follow, and each is a test here.  A cost that an "instead" turns into something else is paid, and
 * what it buys happens (a cost that is PREVENTED is not paid: ACostThatIsPreventedIsNotPaidTests).  And where
 * something cannot be prevented, an "instead" still takes its place, because replacing a thing is not preventing
 * it.
 */
public class InsteadTests
{
	protected VirtualTableScenario GetCostScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Merry, Unquenchable Hobbit: "Skirmish: If Merry is not assigned to a skirmish, discard him
					// to remove a [sauron] Orc from a skirmish involving an unbound companion."
					put("merry", "4_311");
					// Treebeard, Earthborn, an ally: "Response: If an unbound Hobbit is about to be discarded,
					// stack him here instead."
					put("treebeard", "4_103");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, strength 8
					// Orc Soldier: a [sauron] Orc, strength 7, vitality 2; a skirmish ability that is only ever
					// used on purpose.
					put("soldier", "1_271");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path with Steward's Tomb as its site 5: "Wounds cannot be prevented or healed.  Burdens
	// cannot be removed."
	protected VirtualTableScenario GetStewardsTombScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8, vitality 4
					// Gondor Will See It Done: "Response: If a companion is about to take a wound, exert a
					// [gondor] companion to prevent that wound."
					put("seeitdone", "2_34");
					// Uruk Savage: strength 5, vitality 3; damage +1 is its whole text.  It beats Frodo's 4 without
					// overwhelming him, and two wounds are then due.
					put("savage", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text; beats Aragorn without overwhelming him
					// Lost to the Goblins, a Shadow event: "Response: If the Ring-bearer exerts or takes a wound,
					// ..."  Never played: it is here to be offered when he takes a wound, and only then.
					put("lost", "1_189");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "10_119");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				// The One Ring, The Ruling Ring: strength +1.  "Response: If bearer is about to take a wound in a
				// skirmish, he wears The One Ring until the regroup phase.  While wearing The One Ring, each time
				// the Ring-bearer is about to take a wound during a skirmish, add a burden instead."
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACostPaidInsteadIsPaidAndWhatItBuysHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostScenario();

		var merry = scn.GetFreepsCard("merry");
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(treebeard);

		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, runner);
		scn.SkipToAssignments();
		// Both minions on Aragorn, and Merry assigned to nobody.
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(soldier));
		assertTrue(scn.IsCharSkirmishing(runner));
		assertTrue(scn.FreepsActionAvailable(merry));

		// Merry is to be discarded as the cost of his ability, and Treebeard answers that discard.
		scn.FreepsUseCardAction(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(treebeard));

		// He is stacked on Treebeard instead, and is not in the discard pile.
		scn.FreepsChooseOptionalTrigger(treebeard);
		assertInZone(Zone.STACKED, merry);
		assertTrue(scn.GetStackedCards(treebeard).contains(merry));
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The cost was paid in that other way, and what it buys happens: the [sauron] Orc is out of the
		// skirmish, which goes on with the Runner.
		assertFalse(scn.IsCharSkirmishing(soldier));
		assertTrue(scn.IsCharSkirmishing(runner));
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn 8 against the Runner's 5 alone: he wins.  With the Soldier's 7 still in it he would have
		// lost to 12.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtStewardsTombTheOneRingStillTurnsAWoundIntoABurdenBecauseInsteadIsNotPrevention() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStewardsTombScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var seeitdone = scn.GetFreepsCard("seeitdone");

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var lost = scn.GetShadowCard("lost");

		scn.StartGame();
		scn.SkipToSite(4);
		// The turns played on the way drew both decks into the hands: the board is set out now.
		scn.MoveCompanionsToTable(aragorn);
		scn.SetFreepsHand(seeitdone);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, troop);
		scn.SetShadowHand(lost);
		scn.SkipToAssignments();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals("Steward's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, savage}, new PhysicalCardImpl[]{aragorn, troop});

		// Frodo 3 + 1 for the Ring against the Savage's 5: he loses to a damage +1 minion, and is about to take
		// two wounds, one at a time.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));

		// The first wound is let through, to have a wound that is taken beside the one that is not: it is one
		// token, and the Shadow player is offered the event that answers a wound the Ring-bearer takes.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.ShadowPlayAvailable(lost));
		scn.ShadowDeclineOptionalTrigger();

		// The second wound, and the Ring's response to it, at the Tomb: he puts the Ring on, and a burden is
		// added in the wound's place.  The wound is not prevented; something else happens instead of it.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsChooseOptionalTrigger(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		// No wound was taken this time, and nothing answers one: the Shadow player is asked nothing.
		assertFalse(scn.ShadowPlayAvailable(lost));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.HAND, lost);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The control, on the same table: a prevention does nothing here.  Aragorn 8 against the Troop's 13:
		// he loses, and is about to take one wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));

		// Gondor Will See It Done exerts him, the one [gondor] companion, and the wound lands all the same.
		scn.FreepsPlayCard(seeitdone);
		assertInZone(Zone.DISCARD, seeitdone);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
