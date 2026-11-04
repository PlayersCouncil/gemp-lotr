package com.gempukku.lotro.mechanics.costs;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Exerting one character more than once: "exert a Nazgul twice".
 *
 * Comprehensive Rules 4.2, "exert": "If the cost of an action requires a character to exert X times, then that
 * character must have X+1 or more vitality or that action cannot be performed."
 *
 * Ketura, 2026-10-07 (F-0131): the choice of who exerts is of those able to exert that many times, without each
 * card's definition having to say so.  As a cost that is all there is to it.  As an effect a player given a choice
 * must take one that can be fully performed (Ketura on F-0053); where nobody can exert that many times, the one
 * chosen exerts as often as he can (do what you can and ignore the rest).
 */
public class ExertSeveralTimesTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Spied From Above. Regroup: Exert a Nazgul twice to play a [wraith] or [sauron] minion. That
					// minion's twilight cost is -8 and it comes into play exhausted.
					put("spied", "6_84");
					put("witchking", "1_237");      // The Witch-king, Lord of Angmar: vitality 4
					put("enquea", "1_231");         // Ulaire Enquea, Lieutenant of Morgul: vitality 4
					put("lemenya", "1_232");        // Ulaire Lemenya, Lieutenant of Morgul: vitality 2, can exert once
					put("hound", "7_192");          // Morgul Hound: [wraith] minion, cost 2

					// Mumak Commander. Maneuver: Exert Mumak Commander twice to exert a companion twice (except
					// the Ring-bearer).  Vitality 3.
					put("commander", "15_86");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: vitality 4
					put("gimli", "1_13");           // Gimli, Son of Gloin: vitality 3
					put("guard1", "1_7");           // Dwarf Guard: vitality 2, can exert once
					put("guard2", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACostOfExertingTwiceIsChosenFromThoseAbleToExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetShadowCard("spied");
		var witchking = scn.GetShadowCard("witchking");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(hound);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, enquea, lemenya);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(spied));
		scn.ShadowUseCardAction(spied);
		// Lemenya has 2 vitality and could exert once: he is not among those who can pay.
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking));
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya));

		scn.ShadowChooseCard(enquea);
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACostOfExertingTwiceCannotBePaidByOneWhoCanExertOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetShadowCard("spied");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(hound);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetVitality(lemenya));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spied));
	}

	@Test
	public void AnEffectOfExertingTwiceIsChosenFromThoseAbleToExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(commander);
		assertEquals(2, scn.GetWoundsOn(commander));
		// The Guard has 2 vitality and could exert once; Frodo is the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));

		scn.ShadowChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnEffectOfExertingTwiceWithNobodyAbleToExertTwiceExertsTheOneChosenAsOftenAsHeCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var commander = scn.GetShadowCard("commander");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(commander);
		assertEquals(2, scn.GetWoundsOn(commander));
		// Neither Guard can exert twice, so the effect cannot be fully performed whoever is chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));

		scn.ShadowChooseCard(guard1);
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertInZone(Zone.FREE_CHARACTERS, guard1);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
