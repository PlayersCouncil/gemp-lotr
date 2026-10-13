package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_104_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dear", "103_104");
					put("mouth", "103_102");      // The Mouth of Sauron, Emissary of Mordor: unique [sauron] Man, strength 9, vitality 3
					put("lemenya", "1_232");      // Ulaire Lemenya, Lieutenant of Morgul: unique [ringwraith] Nazgul, strength 9, vitality 2
					put("taskmaster", "2_90");    // Orc Taskmaster: [sauron] and not unique, strength 8; a Shadow-phase ability only
					put("creeper", "8_75");       // Morgul Creeper: [ringwraith] and not unique, strength 8; text needs 3 burdens
					put("attea", "103_61");       // Ulaire Attea, Khamul of the East: unique [raider] Nazgul, strength 12; text needs 3 burdens
					put("troop", "1_177");        // Goblin Patrol Troop: [moria], strength 13, no text at all
					put("troop2", "1_177");
					put("runner", "1_178");       // Goblin Runner: [moria], strength 5, vitality 1; its only text is for when it is played
					put("runner2", "1_178");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer, strength 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WasHeDeartoTheeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Was He Dear to Thee?
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a unique [sauron] or [ringwraith] minion and hinder the Ring-bearer to make each minion strength +2 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dear");

		assertEquals("Was He Dear to Thee?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAUniqueSauronOrRingwraithMinionAndHindersTheRingBearerToMakeEachMinionInPlayStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dear = scn.GetShadowCard("dear");
		var mouth = scn.GetShadowCard("mouth");
		var lemenya = scn.GetShadowCard("lemenya");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var creeper = scn.GetShadowCard("creeper");
		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(dear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(mouth, lemenya, taskmaster, creeper, attea, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 5 set + 3 companions + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, dear);
		assertFalse(scn.IsHindered(frodo));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(9, scn.GetStrength(mouth));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(8, scn.GetStrength(taskmaster));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.ShadowPlayAvailable(dear));

		scn.ShadowPlayCard(dear);

		// Unique and [sauron], unique and [ringwraith]; not the two that are not unique, nor the [raider] Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mouth, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(taskmaster, creeper, attea, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(lemenya);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertTrue(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(sam));
		assertInZone(Zone.DISCARD, dear);
		assertEquals(11, scn.GetStrength(mouth));
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(taskmaster));
		assertEquals(10, scn.GetStrength(creeper));
		assertEquals(14, scn.GetStrength(attea));
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));

		// A minion that arrives after the event was played is not one of those it made stronger.
		scn.MoveMinionsToTable(troop2);
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(15, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void MinionsStayStrongerThroughTheSkirmishesAndNoLongerOnceTheRegroupPhaseBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dear = scn.GetShadowCard("dear");
		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(dear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(lemenya, troop, runner, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner2));

		// Lemenya is the only minion that can pay for it, and exerts.
		scn.ShadowPlayCard(dear);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(runner2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{gimli, runner2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// 8 against 5 + 2: Aragorn wins and the first Runner dies.
		scn.FreepsPass();
		scn.ShadowPass();
		assertInZone(Zone.DISCARD, runner);

		// The second skirmish of the turn: still +2.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(runner2));

		// 6 against 5 + 2: Gimli loses to a Runner he would otherwise have beaten.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner2));
	}

	@Test
	public void IsNotPlayableWhenNoUniqueSauronOrRingwraithMinionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dear = scn.GetShadowCard("dear");
		var mouth = scn.GetShadowCard("mouth");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var creeper = scn.GetShadowCard("creeper");
		var attea = scn.GetShadowCard("attea");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(dear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(mouth, taskmaster, creeper, attea);
		// Vitality 3 with 2 wounds: the one unique [sauron] minion is exhausted. The others could all exert.
		scn.AddWoundsToChar(mouth, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 5 set + 1 for Frodo + 2 for the shadow number of site 2: enough for the event's (3)
		assertEquals(8, scn.GetTwilight());
		assertFalse(scn.IsHindered(frodo));
		assertInZone(Zone.HAND, dear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dear));
	}

	@Test
	public void IsNotPlayableWhileTheRingBearerIsAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dear = scn.GetShadowCard("dear");
		var mouth = scn.GetShadowCard("mouth");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(dear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(mouth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(frodo);
		scn.FreepsPass();

		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.IsHindered(frodo));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertInZone(Zone.HAND, dear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dear));
	}
}
