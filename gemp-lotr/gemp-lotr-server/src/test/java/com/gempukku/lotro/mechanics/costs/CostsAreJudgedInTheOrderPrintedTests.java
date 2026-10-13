package com.gempukku.lotro.mechanics.costs;

import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The costs of an action are judged in the order printed: a cost is payable only on the board the costs before
 * it will leave, and a card the action then plays must be playable on the board all of its costs will leave.
 *
 * Ketura, 2026-10-07, on Q-0080: "Costs (and effects for that matter) should always be evaluated in the order in
 * which they appear."  And on F-0046: playing a card from the discard pile as an effect "should block until
 * both can be paid for; rules state that effects playing from discard pile have to essentially be treated like
 * costs."  Comprehensive Rules 4.2, "exert": a character with 1 vitality cannot exert.
 *
 * What the game can tell beforehand is the exertions and wounds a cost is certain to place: on the card itself,
 * on its bearer, on a card the action has already picked out, and on a character who is the only one able to pay
 * ("exert the Ring-bearer"; "exert a Dwarf" with one Dwarf able to).  Where the player really chooses who exerts,
 * the action is judged on the board as it stands, since nobody can say whom he will choose (F-0184; the card
 * that met it, Grimir, Dwarven Elder 51_17, exerts himself twice to play an event that needs a Dwarf to exert).
 *
 * Shown here: two costs of one event.  A cost counted against the card an ability then PLAYS is Grimir's, in his
 * own tests (Card_01_017_ErrataTests).  No printed card has the player choose who exerts and then plays an event
 * that may need an exertion of its own (the printed cards of that shape play possessions, minions and
 * conditions), so the judging "on the board as it stands" is built and is not tested (Ketura, 2026-10-11: a
 * test shows what a card that exists does).
 */
public class CostsAreJudgedInTheOrderPrintedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fallen", "11_6");      // Fallen Lord: [dwarven] skirmish event, cost 1: exert the Ring-bearer and a Dwarf to make that Dwarf strength +X, X his resistance
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; fights the skirmish
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ASecondCostIsNotPayableByTheExertionTheFirstCostUsesUp() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fallen = scn.GetFreepsCard("fallen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fallen);
		// The Ring-bearer is made the only Dwarf there is, so that both of Fallen Lord's exertions are his.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, frodo, null, Race.DWARF));
		// Vitality 4 with 2 wounds: one exertion is left to him, not two.
		scn.AddWoundsToChar(frodo, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fallen));

		// With two exertions left to him the event is offered, and he pays both.
		scn.RemoveWoundsFromChar(frodo, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(fallen));

		var strength = scn.GetStrength(frodo);
		var resistance = scn.GetResistance(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(fallen);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(strength + resistance, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, fallen);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
