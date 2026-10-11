package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
 * Ring-bearer who bore no Ring (F-0175).
 *
 * This is a guard: no card should be able to discard the Ring, so none is there to try.  Each test here makes
 * the attempt itself (Ketura, 2026-10-11, Q-0241): a maneuver ability stapled onto a minion with no text of its
 * own, Goblin Patrol Troop 1_177, whose effect is the engine's own discard from play
 * (DiscardCardsFromPlayEffect, what every card's "discard" is carried out by) aimed at the Ring by the test,
 * with the Troop as the card that discards.
 */
public class TheOneRingIsNeverDiscardedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability that is never used here
					put("armor", "1_8");        // Dwarven Armor: bearer must be a Dwarf; changes no number
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability that is never used here
					put("sword", "1_112");      // Ranger's Sword: bearer must be Aragorn; he is damage +1
					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase and a card
					// for each test's discard to be stapled onto
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ADiscardAimedAtTheOneRingAndAnotherCardTakesTheOtherCardAndLeavesTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Stapled onto the Troop: "Maneuver: Discard The One Ring and Dwarven Armor."
		scn.ApplyAdHocAction(new AbstractActionProxy() { // lint-ok: a guard: no card should be able to discard The One Ring, so the attempt is stapled on (Q-0241)
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				if (!VirtualTableScenario.P2.equals(playerId) || game.getGameState().getCurrentPhase() != Phase.MANEUVER)
					return null;
				var action = new ActivateCardAction(troop, playerId);
				action.appendEffect(new DiscardCardsFromPlayEffect(playerId, troop, Filters.or(ring, armor)));
				return Collections.singletonList(action);
			}
		});
		scn.FreepsPass();

		assertAttachedTo(ring, frodo);
		assertFalse(scn.IsHindered(ring));
		assertAttachedTo(armor, gimli);
		assertTrue(scn.ShadowActionAvailable(troop));

		scn.ShadowUseCardAction(troop);
		// What a companion bears is a card like the Ring, and is discarded.  The Ring is not.
		assertInDiscard(armor);
		assertAttachedTo(ring, frodo);
		assertInZone(Zone.ATTACHED, ring);
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ADiscardThatReachesHinderedCardsTakesAHinderedCardAndLeavesTheHinderedRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Hindered, the Ring has nothing that cards give it; the sword hindered beside it shows that this
		// discard reaches hindered cards.
		scn.HinderCard(ring, sword);

		// Stapled onto the Troop: "Maneuver: Discard The One Ring and Ranger's Sword", hindered or not.
		scn.ApplyAdHocAction(new AbstractActionProxy() { // lint-ok: a guard: no card should be able to discard The One Ring, so the attempt is stapled on (Q-0241)
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				if (!VirtualTableScenario.P2.equals(playerId) || game.getGameState().getCurrentPhase() != Phase.MANEUVER)
					return null;
				var action = new ActivateCardAction(troop, playerId);
				action.appendEffect(new DiscardCardsFromPlayEffect(playerId, troop, SpotOverride.INCLUDE_HINDERED, Filters.or(ring, sword)));
				return Collections.singletonList(action);
			}
		});
		scn.FreepsPass();

		assertAttachedTo(ring, frodo);
		assertTrue(scn.IsHindered(ring));
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.IsHindered(sword));
		assertTrue(scn.ShadowActionAvailable(troop));

		scn.ShadowUseCardAction(troop);
		assertInDiscard(sword);
		assertAttachedTo(ring, frodo);
		assertInZone(Zone.ATTACHED, ring);
		assertTrue(scn.IsHindered(ring));
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ADiscardAimedAtTheOneRingAloneIsCarriedOutAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Stapled onto the Troop: "Maneuver: Discard The One Ring."
		scn.ApplyAdHocAction(new AbstractActionProxy() { // lint-ok: a guard: no card should be able to discard The One Ring, so the attempt is stapled on (Q-0241)
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				if (!VirtualTableScenario.P2.equals(playerId) || game.getGameState().getCurrentPhase() != Phase.MANEUVER)
					return null;
				var action = new ActivateCardAction(troop, playerId);
				action.appendEffect(new DiscardCardsFromPlayEffect(playerId, troop, ring));
				return Collections.singletonList(action);
			}
		});
		scn.FreepsPass();

		assertAttachedTo(ring, frodo);
		int discard = scn.GetFreepsDiscardCount();
		assertTrue(scn.ShadowActionAvailable(troop));

		// The ability is used, and it is the Free Peoples player's turn to act: nothing was discarded
		scn.ShadowUseCardAction(troop);
		assertAttachedTo(ring, frodo);
		assertInZone(Zone.ATTACHED, ring);
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
