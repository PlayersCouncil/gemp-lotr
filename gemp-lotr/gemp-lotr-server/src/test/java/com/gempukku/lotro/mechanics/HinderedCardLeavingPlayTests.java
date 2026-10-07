package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A hindered card that leaves play is hindered no longer.
 *
 * Comprehensive Rules 5.0, "hinder": "Cards in out-of-play zones cannot be hindered. If a hindered card is
 * discarded or otherwise moved to your hand, draw deck, dead pile, adventure deck, or discard pile, or removed
 * from the game, it is flipped the correct way up and exits play as any other card."
 *
 * A card shuffled into a draw deck from play used to keep its hindered mark there (found with F-0172).
 */
public class HinderedCardLeavingPlayTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Worth Fighting For: "Make the Shadow player exhaust 1 of your characters.  For each exertion
					// made, hinder a Shadow support card.  If you cannot spot more than 4 companions, you may make
					// the Shadow player shuffle one of their hindered cards (and all cards stacked on it) into
					// their owner's draw deck."
					put("wff", "103_120");
					put("merry", "1_302");      // Merry, Friend to Sam: a Hobbit companion, vitality 4
					put("armory", "1_173");     // Goblin Armory: a [moria] condition in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedCardShuffledIntoADrawDeckIsNoLongerHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var merry = scn.GetFreepsCard("merry");
		var armory = scn.GetShadowCard("armory");

		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(armory);
		// Hindered before the event, so that the event's own hindering finds nothing and asks nothing
		scn.HinderCard(armory);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.IsHindered(armory));

		scn.FreepsPlayCard(wff);
		scn.ShadowChooseCard(merry);

		// Goblin Armory is the Shadow player's only hindered card: it is shuffled in without a question
		assertInZone(Zone.DECK, armory);
		assertFalse(scn.IsHindered(armory));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
