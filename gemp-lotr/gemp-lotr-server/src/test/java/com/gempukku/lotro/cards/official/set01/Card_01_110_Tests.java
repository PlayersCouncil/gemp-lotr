package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_110");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNextSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pathfinder", "1_110");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the ranger spotted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PathfinderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pathfinder
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Regroup:</b> Spot a ranger to play the fellowship's next site (replacing opponent's site if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Pathfinder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// The parenthesis, "replacing opponent's site if necessary", is left for the card's own batch: on this table
	// the site replaced would be its twin, and Q-0226 asks whether a copy may replace a copy.
	@Test
	public void SpotsARangerToPlayTheFellowshipsNextSiteFromYourOwnAdventureDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNextSiteScenario();

		var pathfinder = scn.GetFreepsCard("pathfinder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		scn.StartGame();

		// Only site 1 is on the adventure path; each player's site 2 is in his adventure deck.
		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);
		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2, shadowSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(pathfinder));

		scn.FreepsPlayCard(pathfinder);
		// Site 2 is played from the Free Peoples player's own adventure deck: in a move it would have been
		// the Shadow player's.  The fellowship has not moved.
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, pathfinder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
