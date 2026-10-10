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
 * Muster.
 *
 * Comprehensive Rules 4.2, "muster": "At the start of the regroup phase, for each of your characters who has the
 * muster keyword, you may discard a card from hand, then draw a card.  You must discard all at once."
 *
 * "Your characters" has no side in it: a Shadow player whose minion has muster is made the offer as a Free
 * Peoples player is for a companion.  A card with muster and a start-of-regroup text of its own is in
 * OptionalActionsAnsweringOneTriggerTests.
 */
public class MusterTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Witch-king, Captain of the Nine Riders: fierce, toil 2 and muster, and no other text.
					put("witchking", "11_226");
					put("runner", "1_178");     // Goblin Runner and Goblin Patrol Troop: the two cards in the Shadow hand
					put("troop", "1_177");
					put("marksman", "1_176");   // Goblin Marksman: the top card of the Shadow draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheShadowPlayerIsOfferedMusterForHisMinionAtTheStartOfTheRegroupPhaseAndDiscardsACardToDrawOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(runner, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.MoveCardsToTopOfDeck(marksman);
		// Nobody is assigned to the Witch-king in either round (he is fierce), so he is in play when the regroup
		// phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(marksman, scn.GetShadowTopOfDeck());

		// The offer is the Shadow player's; the Free Peoples player, who has no muster character, is asked
		// nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking, "Use Muster"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// One muster character: no card or one.
		scn.ShadowChooseOptionalTrigger(witchking, "Use Muster");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));

		// The Runner is discarded and the top card of the deck drawn: the hand is the size it was.
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.HAND, marksman);
		assertInZone(Zone.HAND, troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
