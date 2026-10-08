package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A limit on what a card adds: "(limit +3)".
 *
 * Comprehensive Rules 4.2, "limit": "When a card has a limit, such as '(limit +3),' the limit applies to that card
 * for an entire phase."  The rule's example is Trust Me As You Once Did (3_35): with two signets to spot, "you may
 * add +2 to one companion and +1 to another in two different actions.  Once the limit is reached, no more may be
 * added, and the last +1 is ignored."  "A limit does not span multiple phases", and "a limit does not apply to a
 * different copy of the same card."
 *
 * Ketura, 2026-10-07 (F-0125): the limit is the source card's, not one for each card it is used on.
 */
public class LimitTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Trust Me as You Once Did. Skirmish: Exert Gandalf to make a companion strength +1 for each
					// companion with the Gandalf signet you can spot (limit +3).
					put("trust1", "3_35");
					put("trust2", "3_35");
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: vitality 4, Gandalf signet
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, Gandalf signet
					put("guard", "1_7");            // Dwarf Guard: strength 4, no signet

					put("troop1", "1_177");         // Goblin Patrol Troop: strength 13, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo signet: two Gandalf signets in all
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ALimitedBonusIsCountedForTheCardThatGivesItWhoeverItIsGivenTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(gandalf, aragorn, guard);
		scn.MoveCardsToSupportArea(trust);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals("false", scn.FreepsActionNoEffect(trust));

		// Two companions with the Gandalf signet: +2 to one companion in the first action.
		scn.FreepsUseCardAction(trust);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard));
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// +2 again, to another companion: only 1 of the card's +3 is left, and the last +1 is ignored.
		scn.ShadowPass();
		assertEquals("false", scn.FreepsActionNoEffect(trust));
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(guard);
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// The limit is reached: Gandalf may still be exerted, and no more is added to anybody.
		scn.ShadowPass();
		assertEquals("limit", scn.FreepsActionNoEffect(trust));
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(guard);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ALimitDoesNotApplyToAnotherCopyOfTheCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust1 = scn.GetFreepsCard("trust1");
		var trust2 = scn.GetFreepsCard("trust2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(trust1, trust2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));

		// The first copy gives +2 and then the 1 that is left of its limit.
		scn.FreepsUseCardAction(trust1);
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		scn.ShadowPass();
		scn.FreepsUseCardAction(trust1);
		scn.FreepsChooseCard(aragorn);
		assertEquals(11, scn.GetStrength(aragorn));

		// The second copy has a limit of its own: +2 more to the same companion in the same skirmish.
		scn.ShadowPass();
		assertEquals("limit", scn.FreepsActionNoEffect(trust1));
		assertEquals("false", scn.FreepsActionNoEffect(trust2));
		scn.FreepsUseCardAction(trust2);
		scn.FreepsChooseCard(aragorn);
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ALimitIsNewInTheNextSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trust = scn.GetFreepsCard("trust1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(gandalf, aragorn, guard);
		scn.MoveCardsToSupportArea(trust);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{gandalf, troop2});
		scn.FreepsResolveSkirmish(aragorn);

		// The whole limit is given in the first skirmish phase: +2, then the 1 that is left.
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(guard);
		scn.ShadowPass();
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(guard);
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		scn.ShadowPass();
		assertEquals("limit", scn.FreepsActionNoEffect(trust));

		// Aragorn (8) loses to the Troop (13).  The next skirmish is another skirmish phase: what was given
		// is gone with the first, and the card may give its +3 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(4, scn.GetStrength(guard));
		assertEquals("false", scn.FreepsActionNoEffect(trust));
		scn.FreepsUseCardAction(trust);
		scn.FreepsChooseCard(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	protected VirtualTableScenario GetValueLimitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// A definition of the tests' own (testCards.hjson): "Fellowship: Add (1) to make a companion
					// strength +1 (limit +2, or +4 if you can spot a Dwarf)."  Its limit is a value, not a number.
					put("banner", "0_8001");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	/**
	 * "Once the limit is reached, no more may be added": a limit that has fallen below what the card has already
	 * given in the phase leaves nothing more to give.  What was given stays, and a further use takes none of it
	 * back.
	 */
	@Test
	public void ALimitThatFallsBelowWhatHasBeenGivenLeavesNothingToGiveAndTakesNothingBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetValueLimitScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(banner);

		scn.StartGame();

		// A Dwarf to spot: the limit is +4.  Three uses, each +1.
		assertEquals(8, scn.GetStrength(aragorn));
		for (int used = 0; used < 3; used++) {
			scn.FreepsUseCardAction(banner);
			scn.FreepsChooseCard(aragorn);
		}
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetTwilight());

		// The Dwarf goes: the limit is +2, and +3 has been given.
		scn.MoveCardsToDiscard(gimli);
		scn.FreepsRefreshDecision();
		assertEquals("limit", scn.FreepsActionNoEffect(banner));

		scn.FreepsUseCardAction(banner);
		scn.FreepsChooseCard(aragorn);
		assertEquals(4, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(aragorn));

		// And again: the first of these two uses gave nothing back to the limit either.
		assertEquals("limit", scn.FreepsActionNoEffect(banner));
		scn.FreepsUseCardAction(banner);
		scn.FreepsChooseCard(aragorn);
		assertEquals(5, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
