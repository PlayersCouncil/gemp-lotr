package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_304_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("getaway", "4_304");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GetOnandGetAwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Get On and Get Away
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Add 2 burdens to play the fellowship's next 2 sites (replacing opponent's sites if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("getaway");

		assertEquals("Get On and Get Away", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsTwoBurdensToPlayTheFellowshipsNextTwoSites() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");
		scn.MoveCardsToHand(getaway);

		scn.StartGame();

		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		var site4 = scn.GetFreepsSite(4);
		// Only site 1 is on the adventure path so far.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(2));
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site2, site3, site4);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(getaway));

		scn.FreepsPlayCard(getaway);

		assertEquals(2, scn.GetBurdens());
		// The fellowship's next 2 sites are the player's own sites 2 and 3, and no further.
		assertEquals(site2, scn.GetSite(2));
		assertEquals(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site2, site3);
		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		// The sites are played, and the fellowship has not moved.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite2TheNextTwoSitesAreSites3And4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(getaway);
		scn.FreepsRefreshDecision();

		var site2 = scn.GetSite(2);
		var site3 = scn.GetFreepsSite(3);
		var site4 = scn.GetFreepsSite(4);
		var site5 = scn.GetFreepsSite(5);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, site4, site5);
		assertInHand(getaway);
		int burdens = scn.GetBurdens();

		scn.FreepsPlayCard(getaway);

		assertEquals(burdens + 2, scn.GetBurdens());
		// Site 2, where the fellowship is, stays as it was.
		assertSame(site2, scn.GetSite(2));
		assertSame(site3, scn.GetSite(3));
		assertSame(site4, scn.GetSite(4));
		assertNull(scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_DECK, site5);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ReplacesTheOpponentsNextSiteWithHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");
		scn.MoveCardsToHand(getaway);

		scn.StartGame();

		// The other player's site 2 lies next, as it does where he has played the site himself. Both players
		// hold the same nine sites here, so the site that replaces it is its twin.
		var opponentsSite2 = scn.GetShadowSite(2);
		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		scn.MoveCardToAdventurePath(opponentsSite2);
		scn.FreepsRefreshDecision();
		assertSame(opponentsSite2, scn.GetSite(2));
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site2, site3);
		assertInHand(getaway);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(getaway);

		assertEquals(2, scn.GetBurdens());
		// His own site 2 lies on the path, and the other player's is back in its owner's adventure deck.
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_DECK, opponentsSite2);
		assertSame(site3, scn.GetSite(3));
		assertNull(scn.GetSite(4));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithHisOwnSiteAlreadyNextOnlyTheSiteAfterItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");
		scn.MoveCardsToHand(getaway);

		scn.StartGame();

		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		var site4 = scn.GetFreepsSite(4);
		scn.MoveCardToAdventurePath(site2);
		scn.FreepsRefreshDecision();
		assertSame(site2, scn.GetSite(2));
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, site4);
		assertInHand(getaway);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(getaway);

		// The next 2 sites are sites 2 and 3, whatever lies there: site 2 stays his own, site 3 is laid, and
		// site 4 is not.
		assertEquals(2, scn.GetBurdens());
		assertSame(site2, scn.GetSite(2));
		assertSame(site3, scn.GetSite(3));
		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(getaway);

		scn.StartGame();
		assertInHand(getaway);
		assertTrue(scn.FreepsPlayAvailable(getaway));

		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(getaway));
		assertInHand(getaway);
		assertEquals(0, scn.GetBurdens());
	}
}
