package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("resistance", "2_79");
					// Ulaire Attea, The Easterling: a twilight Nazgul, strength 12, vitality 3.  "When you play
					// Ulaire Attea, you may wound an ally" acts only when he is played.
					put("twigul", "2_82");
					// Ulaire Enquea, Ringwraith in Twilight: a twilight Nazgul, vitality 4; his text answers a
					// skirmish he wins.
					put("enquea", "2_83");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul who is not twilight, vitality 2; her text is
					// about bearers of [ringwraith] conditions.
					put("lemenya", "1_232");
					// Corpse Lights: twilight, and a [sauron] Wraith, not a Nazgul; vitality 2; a skirmish ability only.
					put("lights", "6_99");
					put("runner", "1_178");     // Goblin Runner: strength 5; its text acts only when it is played

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Ruling Ring: "While wearing The One Ring, each time the Ring-bearer is about to take a wound
				// during a skirmish, add a burden instead."  Its own response is the only other way to put it on.
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ResistanceBecomesUnbearableStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Resistance Becomes Unbearable
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a twilight Nazgûl to exert the Ring-bearer. If the Ring-bearer is then exhausted, he puts on The One Ring until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("resistance");

		assertEquals("Resistance Becomes Unbearable", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsATwilightNazgulToExertTheRingBearerWhoKeepsTheRingOffWhileHeIsNotExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var resistance = scn.GetShadowCard("resistance");
		var twigul = scn.GetShadowCard("twigul");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToHand(resistance);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		// Aragorn is exhausted (4 vitality, 3 wounds) and is not the Ring-bearer: it is the Ring-bearer's state the
		// event reads, not anyone's.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(twigul, enquea, lemenya, lights);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		int twilight = scn.GetTwilight();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetVitality(aragorn));
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.ShadowPlayAvailable(resistance));

		scn.ShadowPlayCard(resistance);
		// The two twilight Nazgul; not the Nazgul who is not twilight, and not the twilight Wraith.
		assertTrue(scn.ShadowHasCardChoiceAvailable(twigul, enquea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, lights));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(enquea);

		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(twigul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		// Frodo has 2 of his 4 vitality left: he is not exhausted, and the Ring stays off though Aragorn is.
		assertEquals(2, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(aragorn));
		assertFalse(scn.RBWearingOneRing());
		assertInDiscard(resistance);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheRingBearerExhaustedByTheExertionPutsOnTheRingUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var resistance = scn.GetShadowCard("resistance");
		var twigul = scn.GetShadowCard("twigul");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(resistance);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.MoveMinionsToTable(twigul, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertFalse(scn.RBWearingOneRing());

		scn.ShadowPlayCard(resistance);
		assertEquals(1, scn.GetWoundsOn(twigul));
		// The third wound of Frodo's 4 vitality: he is exhausted, and puts on the Ring.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetVitality(frodo));
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, twigul},
				new PhysicalCardImpl[]{frodo, runner}
		);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 8 against 12
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The second skirmish of the turn: the Ring is still on.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		// Frodo, 3 + 1 for the Ring, loses to the Runner's 5.  Wearing The Ruling Ring he takes a burden in place
		// of the wound, and is asked nothing: with the Ring off, its Response would be put to him here.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		// The regroup phase: it is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyTwilightNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var resistance = scn.GetShadowCard("resistance");
		var twigul = scn.GetShadowCard("twigul");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToHand(resistance);

		scn.StartGame();
		scn.MoveMinionsToTable(twigul, lemenya, lights);
		scn.AddWoundsToChar(twigul, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The twilight Nazgul has 1 vitality left and cannot exert.  The Nazgul who is not twilight and the
		// twilight minion who is no Nazgul could, and do not pay for it.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetVitality(twigul));
		assertEquals(2, scn.GetVitality(lemenya));
		assertEquals(2, scn.GetVitality(lights));
		// 1 companion + 2 for Ettenmoors: the event's cost of 1 is there.
		assertEquals(3, scn.GetTwilight());
		assertInHand(resistance);
		assertFalse(scn.ShadowPlayAvailable(resistance));

		// With one wound fewer he can.
		scn.RemoveWoundsFromChar(twigul, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(resistance));
	}

	@Test
	public void PutsOnTheRingWhenTheRingBearerWasAlreadyExhaustedAndCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var resistance = scn.GetShadowCard("resistance");
		var twigul = scn.GetShadowCard("twigul");
		scn.MoveCardsToHand(resistance);

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(twigul);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(twigul));
		assertFalse(scn.RBWearingOneRing());
		// The cost can be paid, so the event may be played, though Frodo cannot exert.
		assertTrue(scn.ShadowPlayAvailable(resistance));

		scn.ShadowPlayCard(resistance);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(twigul));
		// He is exhausted when the event looks, and that is all it asks.
		assertTrue(scn.RBWearingOneRing());
		assertInDiscard(resistance);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
