package com.gempukku.lotro.mechanics.costs;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card being played is not discarded from hand to pay its own cost.
 *
 * Comprehensive Rules 4.2, "playing a card": "You cannot discard The Card to pay a cost", and the costs of a card
 * must all be payable for it to be played.  A chosen discard from hand (DiscardFromHand) leaves the card out of its
 * choice; a discard AT RANDOM from hand (DiscardCardAtRandomFromHand) leaves it out of the hand it counts while the
 * card is still there (F-0498, engine pass 39).
 *
 * We Must Have It 5_30 ("Skirmish: Discard a card at random from hand to make Gollum strength +3") is the one
 * printed card with a random discard from its own player's hand as a cost, so it is the card that shows it.
 */
public class TheCardIsNotDiscardedToPayItsOwnCostTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("musthave", "5_30");    // We Must Have It: the random discard from hand is the cost of its skirmish half
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5; a regroup ability only
					put("filler", "1_178");     // Goblin Runner: a card in hand for the discard, kept out of play

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARandomDiscardFromHandDoesNotCountTheCardBeingPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var musthave = scn.GetShadowCard("musthave");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(musthave);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.MoveCardsToHand(musthave);
		scn.FreepsPass();

		// The event is the whole hand: its cost would have to discard the event itself.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowPlayAvailable(musthave));
		assertEquals(5, scn.GetStrength(gollum));
	}

	@Test
	public void ARandomDiscardFromHandIsPaidWithTheOneOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var musthave = scn.GetShadowCard("musthave");
		var gollum = scn.GetShadowCard("gollum");
		var filler = scn.GetShadowCard("filler");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(musthave, filler);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.MoveCardsToHand(musthave, filler);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(musthave));

		scn.ShadowPlayCard(musthave);

		// The one card beside the event is the one the random discard can take.
		assertInZone(Zone.DISCARD, musthave, filler);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
