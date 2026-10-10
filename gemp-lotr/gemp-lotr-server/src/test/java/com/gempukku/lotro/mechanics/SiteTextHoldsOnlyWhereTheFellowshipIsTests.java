package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A site's game text is in force only while the fellowship is at that site, and "the fellowship" is the one whose
 * player's turn it is.
 *
 * Comprehensive Rules 4.2, "active", as Legacy Ruling 3 has it: "Sites are always active.  A site's keywords are
 * thus always available to be spotted while on the adventure path.  A site's non-keyword game text does not apply
 * unless the fellowship is at that site, although some cards may copy and use that game text."
 *
 * Each player's fellowship has its own place on the one adventure path.  So a site's text comes into force as a
 * fellowship arrives and goes out as it moves on, and it changes with the turn as well: a site that the first
 * player's marker stands on says nothing during the second player's turn, whose fellowship is somewhere else.
 *
 * The two sites here each change what a Nazgûl costs, by different amounts, so one number read from a Nazgûl in
 * the Shadow player's hand says which of the two texts is in force: neither, the one, the other or both.
 *
 * At the instant of a move the text in force goes by where the marker is: StepsOfAMoveTests.  The two site
 * texts that hold wherever the site lies (Mount Doom 15_193 by Legacy Ruling 3's own card ruling, Doorway to
 * Doom 68_134) are those cards' own.
 */
public class SiteTextHoldsOnlyWhereTheFellowshipIsTests
{
	// Weatherhills at 2: "Each Nazgûl's twilight cost is -1."  Ford of Bruinen at 3: "The twilight cost of the
	// first Nazgûl played to Ford of Bruinen each turn is -5."  No Nazgûl is played in the test, so the Ford's
	// text stands at -5 for as long as it is in force.  The other sites are the usual Fellowship ones.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Witch-king, Lord of Angmar: a Nazgûl of cost 8, so that 8 less 1, less 5 and less 6 are
					// four different numbers.  His own text is about his strength.
					put("witchking", "1_237");
				}},
				new HashMap<>() {{
					put("site1", "1_319");  // Bag End
					put("site2", "1_335");  // Weatherhills
					put("site3", "1_338");  // Ford of Bruinen; a sanctuary
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ASitesTextHoldsOnlyWhileTheFellowshipWhoseTurnItIsStandsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Each player's own Witch-king, in his own hand: read on the turn on which that player is the Shadow player
		var firstPlayersKing = scn.GetFreepsCard("witchking");
		var secondPlayersKing = scn.GetShadowCard("witchking");
		scn.MoveCardsToHand(firstPlayersKing, secondPlayersKing);

		scn.StartGame();

		// The first player's turn, at site 1: the printed 8
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, secondPlayersKing);
		assertEquals(8, scn.GetTwilightCost(secondPlayersKing));

		scn.FreepsPass();

		// At Weatherhills its text is in force: 8 - 1
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals("Weatherhills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(7, scn.GetTwilightCost(secondPlayersKing));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// At the Ford of Bruinen the Ford's text is in force and Weatherhills', left behind, is not: 8 - 5.
		// With both it would be 2.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.HAND, secondPlayersKing);
		assertEquals(3, scn.GetTwilightCost(secondPlayersKing));

		scn.SkipToOtherPlayersTurn();

		// The second player's turn.  His fellowship is at site 1; Weatherhills and the Ford both lie on the
		// path, and the first player's marker stands on the Ford.  Neither text is in force: the first player,
		// who is the Shadow player now, would pay the printed 8.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, firstPlayersKing);
		assertEquals(8, scn.GetTwilightCost(firstPlayersKing));

		scn.ShadowPass();

		// His fellowship has come to Weatherhills, and its text is in force for this turn too.  The Ford's is
		// not, though a marker stands there: 8 - 1, and not 8 - 6.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals("Weatherhills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(7, scn.GetTwilightCost(firstPlayersKing));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
