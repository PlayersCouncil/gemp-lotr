package com.gempukku.lotro.mechanics;

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
 * The One Ring cannot be discarded from play.
 *
 * Ketura, 2026-10-07, on Q-0109: "The Ring-bearer and One Ring should never be discarded for any reason."
 * Comprehensive Rules 4.2, "Ring-bearer", says it of the bearer ("The Ring-bearer cannot be discarded or returned
 * to your hand"); the Ring has the same from the game itself and not from any card, so, like the bearer's
 * (mechanics/HinderedRingBearerTests), it holds while the Ring is hindered, when nothing a card gives it would.
 *
 * The Ring has no side, culture or keyword, so of the real cards only one that discards "a card" or "it" can
 * reach it: Doom Causeway 103_127 ("Each time a card is hindered, discard it") did, and the game went on with a
 * Ring-bearer who bore no Ring (F-0175).  The two test cards here are that shape made plain.
 */
public class TheOneRingIsNeverDiscardedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test cards (testCards.hjson), Shadow conditions in the support area:
					// "Maneuver: Remove (1) to discard a card."  Its discard reaches hindered cards.
					put("riptide", "0_8008");
					// "Maneuver: Remove (1) to discard The One Ring."
					put("maelstrom", "0_8009");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability that is never used here
					put("armor", "1_8");        // Dwarven Armor: bearer must be a Dwarf; changes no number
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability that is never used here
					put("sword", "1_112");      // Ranger's Sword: bearer must be Aragorn; he is damage +1
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ADiscardOfACardNeitherOffersNorTakesTheOneRingHinderedOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var riptide = scn.GetShadowCard("riptide");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(riptide);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertAttachedTo(ring, frodo);
		assertFalse(scn.IsHindered(ring));
		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(riptide);
		assertEquals(twilight - 1, scn.GetTwilight());
		// "A card": what the companions bear is offered, and the companions.  The Ring, a card like them, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, sword, gimli, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring));
		scn.ShadowChooseCard(armor);
		assertInDiscard(armor);
		assertAttachedTo(ring, frodo);

		// Hindered, the Ring has nothing that cards give it; the sword hindered beside it shows that this
		// discard reaches hindered cards.
		scn.HinderCard(ring, sword);
		scn.FreepsPass();
		assertTrue(scn.IsHindered(ring));
		assertTrue(scn.IsHindered(sword));

		scn.ShadowUseCardAction(riptide);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, gimli, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring));
		scn.ShadowChooseCard(sword);
		assertInDiscard(sword);
		assertAttachedTo(ring, frodo);
		assertTrue(scn.IsHindered(ring));
		assertEquals(frodo, scn.GetRingBearer());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ADiscardThatNamesTheOneRingIsPaidForAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var maelstrom = scn.GetShadowCard("maelstrom");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(maelstrom);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertAttachedTo(ring, frodo);
		int twilight = scn.GetTwilight();
		int discard = scn.GetFreepsDiscardCount();
		// An action with a cost is offered though its effect will do nothing (Q-0034)
		assertTrue(scn.ShadowActionAvailable(maelstrom));

		scn.ShadowUseCardAction(maelstrom);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertAttachedTo(ring, frodo);
		assertInZone(Zone.ATTACHED, ring);
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
