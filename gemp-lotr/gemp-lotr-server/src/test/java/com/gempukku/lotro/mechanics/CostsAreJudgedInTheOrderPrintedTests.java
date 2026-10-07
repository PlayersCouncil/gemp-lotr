package com.gempukku.lotro.mechanics;

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
 */
public class CostsAreJudgedInTheOrderPrintedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test card (testCards.hjson), a Free Peoples condition in the support area:
					// "Skirmish: Exert a Dwarf to play a [dwarven] event from your discard pile."
					put("whetstone", "0_8013");

					put("fallen", "11_6");      // Fallen Lord: [dwarven] skirmish event, cost 1: exert the Ring-bearer and a Dwarf to make that Dwarf strength +X, X his resistance
					put("fury", "1_4");         // Battle Fury: [dwarven] skirmish event, cost 0: exert a Dwarf to make that Dwarf strength +3 and damage +1
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6, vitality 3
					put("guard", "1_7");        // Dwarf Guard: Dwarf, strength 4, vitality 2
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

	@Test
	public void AnExertionOnlyOneCharacterCanPayIsCountedAgainstTheCardThenPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whetstone = scn.GetFreepsCard("whetstone");
		var fury = scn.GetFreepsCard("fury");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(whetstone);
		scn.MoveCardsToDiscard(fury);
		// Vitality 3 with 1 wound: Gimli, the only Dwarf, can exert once.  The Whetstone takes that exertion,
		// and Battle Fury, the only event in the pile, then has no Dwarf to exert for it.
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(whetstone));

		// With two exertions left to him it is offered: one for the Whetstone and one for Battle Fury.
		scn.RemoveWoundsFromChar(gimli, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(whetstone));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(whetstone);
		assertEquals(2, scn.GetWoundsOn(gimli));
		// 6 + 3
		assertEquals(9, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WhereThePlayerChoosesWhoExertsTheActionIsJudgedOnTheBoardAsItStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whetstone = scn.GetFreepsCard("whetstone");
		var fury = scn.GetFreepsCard("fury");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveCardsToSupportArea(whetstone);
		scn.MoveCardsToDiscard(fury);
		// Two Dwarves with one exertion each: Gimli at vitality 3 with 1 wound, the Guard at vitality 2.
		// Whichever pays the Whetstone, the other is left to exert for Battle Fury.
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(whetstone));
		assertEquals(4, scn.GetStrength(guard));

		scn.FreepsUseCardAction(whetstone);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		scn.FreepsChooseCard(gimli);

		// Gimli has paid the Whetstone and cannot exert again: the Guard is the only Dwarf left for Battle Fury.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		// 4 + 3
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
