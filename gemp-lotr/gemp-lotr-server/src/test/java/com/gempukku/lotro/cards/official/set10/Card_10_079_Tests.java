package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_079_Tests
{

	// The card's own block: King-block sites, whose site 8 is Watchers of Cirith Ungol 10_120 and site 9 Dagorlad
	// 7_360 (the move there adds 3 threats).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barren", "10_79");
					put("chieftain", "1_266");   // Orc Chieftain: a [sauron] Orc, cost 2, site 6; text only on a weapon played on him
					put("hunter", "1_256");      // Morgul Hunter: a [sauron] Orc, cost 5, site 6; strength +1 for each companion, and nothing else
					put("sentry1", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, cost 2, site 6; damage +1 and nothing else
					put("sentry2", "10_84");
					// Goblin Runner: a [moria] Orc, cost 1, site 4; his "may add (2)" answers only his own play.
					put("runner", "1_178");
					// Strength Born of Fear: a [sauron] condition, no minion; its skirmish ability costs (3) and is not used.
					put("fear", "1_278");
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound by rule, not the Ring-bearer; abilities used only on purpose
					put("faramir", "4_117");     // Faramir, Son of Denethor: a Ring-bound [gondor] Man; his text is on skirmishes only
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: an unbound companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// "Any site 8 or 9": a site 8 of another block, Anduin Banks 1_356 of the Fellowship path.
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barren", "10_79");
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound by rule, not the Ring-bearer; abilities used only on purpose
					put("faramir", "4_117");     // Faramir, Son of Denethor: a Ring-bound [gondor] Man; his text is on skirmishes only
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: an unbound companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarrenLandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Barren Land
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [sauron] minion.<br>Each time the fellowship moves to any site 8 or 9, exert each Ring-bound companion.<br><b>Shadow:</b> Discard this condition to play a [sauron] minion. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var barren = scn.GetFreepsCard("barren");

		assertEquals("Barren Land", barren.getBlueprint().getTitle());
		assertNull(barren.getBlueprint().getSubtitle());
		assertTrue(barren.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, barren.getBlueprint().getSide());
		assertEquals(Culture.SAURON, barren.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, barren.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(barren, Keyword.SUPPORT_AREA));
		assertEquals(2, barren.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASauronMinionIsSpottedAndThenGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barren = scn.GetShadowCard("barren");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(barren, chieftain);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		assertInHand(barren);
		scn.FreepsPass();

		// A [moria] minion and a [sauron] condition are in play, and no [sauron] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(barren));

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowPlayAvailable(barren));
		scn.ShadowPlayCard(barren);
		assertInZone(Zone.SUPPORT, barren);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsItToPlayASauronMinionFromHandAtTwilightCostMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barren = scn.GetShadowCard("barren");
		var hunter = scn.GetShadowCard("hunter");
		var sentry1 = scn.GetShadowCard("sentry1");
		var sentry2 = scn.GetShadowCard("sentry2");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(barren, hunter, sentry2, runner, fear);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry1);
		scn.SetTwilight(5);
		assertInHand(hunter);
		assertInHand(sentry2);
		scn.FreepsPass();

		// The Sentry, an Uruk-hai, is the only [sauron] minion in play, and is spotted for the play.
		scn.ShadowPlayCard(barren);
		assertInZone(Zone.SUPPORT, barren);
		// The pool is set for the ability's own cost, whatever the play left in it.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(barren));

		scn.ShadowUseCardAction(barren);
		assertInDiscard(barren);
		// The two [sauron] minions in hand, an Orc and an Uruk-hai; not the [moria] Orc or the [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, sentry2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, fear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hunter);

		// 5 - 2 + 2 roaming at site 2
		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		assertEquals(0, scn.GetTwilight());
		assertInHand(sentry2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoSauronMinionInHandNorUntilThePoolPaysTheDiscountedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barren = scn.GetShadowCard("barren");
		var hunter = scn.GetShadowCard("hunter");
		var sentry1 = scn.GetShadowCard("sentry1");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(runner, fear);
		scn.MoveCardsToSupportArea(barren);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry1);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, barren);
		scn.FreepsPass();

		// Only a [moria] minion and a [sauron] condition in hand: the play the ability buys cannot be made.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertFalse(scn.ShadowActionAvailable(barren));

		// Morgul Hunter costs 5 - 2 + 2 roaming at site 2: at 4 it is not offered, at 5 it is.
		scn.MoveCardsToHand(hunter);
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(barren));
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(barren));
		assertInZone(Zone.SUPPORT, barren);
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barren = scn.GetShadowCard("barren");
		var hunter = scn.GetShadowCard("hunter");
		var sentry1 = scn.GetShadowCard("sentry1");
		scn.MoveCardsToHand(hunter);
		scn.MoveCardsToSupportArea(barren);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry1);
		scn.SetTwilight(10);
		assertInHand(hunter);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(barren));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// 10 set + 1 companion + 1 for King's Tent: enough for the Hunter's 5
		assertEquals(12, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(barren));
	}

	@Test
	public void EachMoveToSite8Or9ExertsEachRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, faramir, aragorn);

		var barren = scn.GetShadowCard("barren");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(barren);
		// Sam, vitality 4, exhausted: an exertion cannot wound him.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// No minion, no skirmish: the regroup move to site 9 exerts them again.  Dagorlad's own required trigger (3
		// threats) answers the same move, and the Free Peoples player orders the two.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsActionAvailable("Barren Land"));
		assertTrue(scn.FreepsActionAvailable("Dagorlad"));
		scn.FreepsResolveActionOrder("Barren Land");
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void TheMoveToSite7ExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(sam, faramir);

		var barren = scn.GetShadowCard("barren");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(barren);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(faramir));
	}

	@Test
	public void TheMoveToSite8OfAnotherBlockExertsEachRingBoundCompanionToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var barren = scn.GetShadowCard("barren");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(barren);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
