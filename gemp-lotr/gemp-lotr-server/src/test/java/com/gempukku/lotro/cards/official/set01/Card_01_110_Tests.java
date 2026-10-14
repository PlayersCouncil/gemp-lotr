package com.gempukku.lotro.cards.official.set01;

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

public class Card_01_110_Tests
{

	protected VirtualTableScenario GetNextSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pathfinder", "1_110");
					put("pathfinder2", "1_110");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the ranger spotted
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no ranger
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
		 * Subtype: Fellowship or Regroup
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Regroup:</b> Spot a ranger to play the fellowship's next site (replacing opponent's site if necessary).
		*/

		var scn = GetNextSiteScenario();

		var card = scn.GetFreepsCard("pathfinder");

		assertEquals("Pathfinder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

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
		assertInZone(Zone.DISCARD, pathfinder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARangerIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNextSiteScenario();

		var pathfinder = scn.GetFreepsCard("pathfinder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(pathfinder, aragorn);

		scn.StartGame();

		// A [gondor] Man who is no ranger is in play.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertNull(scn.GetSite(2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pathfinder));

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(pathfinder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElvenRangerIsSpottedToPlayTheNextSiteInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNextSiteScenario();

		var pathfinder = scn.GetFreepsCard("pathfinder");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(pathfinder);

		var freepsSite3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3, shadowSite3);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(pathfinder));

		scn.FreepsPlayCard(pathfinder);

		// His own site 3 is laid; the fellowship stays at site 2 and site 4 is unlaid.
		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertNull(scn.GetSite(4));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInZone(Zone.DISCARD, pathfinder);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReplacesTheOpponentsSiteLyingNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNextSiteScenario();

		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable("aragorn");

		var freepsSite4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(pathfinder);

		// The other player's site 4 lies next, as where he has played his next site himself.  Both players hold
		// the same nine sites here, so the site that replaces it is its twin (Q-0226).
		scn.MoveCardToAdventurePath(shadowSite4);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite4);
		assertTrue(scn.FreepsPlayAvailable(pathfinder));

		scn.FreepsPlayCard(pathfinder);

		// His own site 4 lies on the path, and the other player's is back in its owner's adventure deck.
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, pathfinder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithHisOwnSiteAlreadyNextNoSiteIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNextSiteScenario();

		var pathfinder = scn.GetFreepsCard("pathfinder");
		var pathfinder2 = scn.GetFreepsCard("pathfinder2");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(pathfinder, pathfinder2);

		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		scn.FreepsPlayCard(pathfinder);
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);

		// CR 4.2 "effect": the second copy may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(pathfinder2));
		scn.FreepsPlayCard(pathfinder2);

		// His own site 2 stays; he has no other site 2, and the other player's is not played either.
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, pathfinder, pathfinder2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
