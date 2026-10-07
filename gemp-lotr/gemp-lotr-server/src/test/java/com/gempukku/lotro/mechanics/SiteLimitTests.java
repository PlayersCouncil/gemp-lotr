package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A limit on uses per site: "(limit once per site)".
 *
 * Neither rules document has the phrase; it is the Players' Council's.  Ketura, 2026-10-07, on Q-0084: "the intent
 * is not that you have to track it across turn boundaries", so it is once for each site in a turn; and on F-0146:
 * "it makes sense to add an engine-level handling of this instead of a hack."  The game counts the uses of a card
 * at the site the fellowship is at; the count is cleared when the fellowship moves to another site and at the end
 * of the turn, and by nothing else: not by a phase ending.
 *
 * Shown with Cirith Ungol Watcher, Spirit of Vigilance (103_63): "Each time a companion exerts, draw a card (limit
 * once per site)."  What that card's own tests hold (an exertion on arriving belongs to the site arrived at; two
 * companions exerting at once draw one card) is not repeated here.
 */
public class SiteLimitTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher", "103_63");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; Maneuver: exert Aragorn to make him defender +1
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; Fellowship: exert Sam to remove a burden
					put("runner1", "1_178");    // Goblin Runner: strength 5, a minion so that there are a maneuver phase and a skirmish
					put("runner2", "1_178");
					// Test Waystone (the tests' own card), the limit written with the requirement and the count:
					// "Fellowship or Regroup: Add (1) to heal a companion (limit once per site)."
					put("waystone", "0_8003");
					// Cards for the Shadow player's draw deck.
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ALimitPerSiteOutlastsThePhasesSpentAtASiteAndIsRenewedByTheMoveToAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2, the maneuver phase: the one use for this site.
		assertEquals(2, scn.GetCurrentSiteNumber());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsUseCardAction(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// The maneuver, archery and assignment phases end, and in the skirmish the fellowship is at site 2
		// still: Ettenmoors, "Skirmish: Exert your companion or minion to make that character strength +2".
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner1);
		var ettenmoors = scn.GetCurrentSite();

		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// Aragorn's 10 overwhelms the Runner's 5.  The fellowship moves on in the regroup phase (the Shadow
		// player reconciles on the way, so the counts are read afresh), and at site 3 there is a use again.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner2);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCurrentSiteNumber());
		hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();

		scn.FreepsUseCardAction(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ALimitPerSiteIsRenewedByTheEndOfTheTurnThoughTheFellowshipHasNotMoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var runner1 = scn.GetShadowCard("runner1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Turn 1, site 2: the one use for this site.
		assertEquals(2, scn.GetCurrentSiteNumber());
		int hand = scn.GetShadowHandCount();

		scn.FreepsUseCardAction(aragorn);

		assertEquals(hand + 1, scn.GetShadowHandCount());

		// The turn ends at site 2 and the other player's turn passes.  The next turn's fellowship phase is
		// spent at site 2 still.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, watcher);
		// The reconciles on the way may have emptied the draw deck.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("troop3"));
		hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// And once in that turn, at that site.
		scn.FreepsUseCardAction(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
	@Test
	public void ALimitPerSiteWrittenAsARequirementAndACountWithholdsTheAbilityUntilTheFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waystone = scn.GetFreepsCard("waystone");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(waystone);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();

		// Site 1, the fellowship phase.  Frodo is unwounded, so Aragorn is the one healed.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsActionAvailable(waystone));

		scn.FreepsUseCardAction(waystone);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		// Once at this site: (1) could be added again and Aragorn is still wounded, and it is not offered.
		assertFalse(scn.FreepsActionAvailable(waystone));

		// With no minion in play the game goes from the move straight to the regroup phase, at site 2.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsActionAvailable(waystone));

		scn.FreepsUseCardAction(waystone);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowPass();
		assertFalse(scn.FreepsActionAvailable(waystone));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
