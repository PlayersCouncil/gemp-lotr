package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_304_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("getaway", "54_304");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit companion, cost 2
					put("merry", "1_302");      // Merry, Friend to Sam: Hobbit companion, not Ring-bound
					// Faramir, Captain of Gondor: a Ring-bound companion who is a [gondor] Man; his texts are about a starting
					// fellowship and a skirmish
					put("faramir", "4_116");
					put("gollum", "10_21");     // Gollum, Mad Thing: [gollum] minion, not Sméagol
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("getaway", "54_304");
					put("smeagol", "5_29");     // Sméagol, Slinker: [gollum] Ring-bound companion; to play, add a burden
					put("shireSmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Ring-bound companion, not a Hobbit; to play, add a burden
					put("gollum", "10_21");     // Gollum, Mad Thing: [gollum] minion, not Sméagol
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom as the Ring-bearer, so that no Ring-bound Hobbit is in play.
				"9_14",
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
		 * Game Text: Fellowship: Spot 2 Ring-bound Hobbits (or Smeagol) and add 2 burdens to play the fellowship's next 2 sites (replacing opponent's sites if necessary).
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
	public void WithTwoRingBoundHobbitsAddsTwoBurdensToPlayTheFellowshipsNextTwoSites() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(getaway);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		var site4 = scn.GetFreepsSite(4);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(2));
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site2, site3, site4);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(getaway));

		scn.FreepsPlayCard(getaway);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		// The fellowship's next 2 sites are the player's own sites 2 and 3, and no further.
		assertEquals(site2, scn.GetSite(2));
		assertEquals(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site2, site3);
		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite2TheNextTwoSitesAreSites3And4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(getaway);

		var site2 = scn.GetSite(2);
		var site3 = scn.GetFreepsSite(3);
		var site4 = scn.GetFreepsSite(4);
		var site5 = scn.GetFreepsSite(5);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, site4, site5);
		int burdens = scn.GetBurdens();

		scn.FreepsPlayCard(getaway);

		assertEquals(burdens + 2, scn.GetBurdens());
		// Site 2, where the fellowship is, stays as it was.
		assertEquals(site2, scn.GetSite(2));
		assertEquals(site3, scn.GetSite(3));
		assertEquals(site4, scn.GetSite(4));
		assertNull(scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_DECK, site5);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneRingBoundHobbitUntilASecondIsPlayedAndThenReplacesTheOpponentsNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("getaway");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var faramir = scn.GetFreepsCard("faramir");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(getaway, sam);
		// A Hobbit who is not Ring-bound, and a Ring-bound companion who is not a Hobbit.
		scn.MoveCompanionsToTable(merry, faramir);

		scn.StartGame();
		// A [gollum] character who is not Sméagol.
		scn.MoveMinionsToTable(gollum);

		var opponentsSite2 = scn.GetShadowSite(2);
		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		scn.MoveCardToAdventurePath(opponentsSite2);
		scn.FreepsRefreshDecision();
		assertEquals(opponentsSite2, scn.GetSite(2));
		assertNull(scn.GetSite(3));

		assertTrue(scn.HasKeyword(frodo, Keyword.RING_BOUND));
		assertFalse(scn.HasKeyword(merry, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(faramir, Keyword.RING_BOUND));
		assertTrue(scn.FreepsPlayAvailable(sam));
		// One Ring-bound Hobbit: Frodo.
		assertFalse(scn.FreepsPlayAvailable(getaway));

		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());

		// Two Ring-bound Hobbits.
		assertTrue(scn.FreepsPlayAvailable(getaway));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(getaway);

		assertEquals(2, scn.GetBurdens());
		// The opponent's site 2 is replaced by the player's own and goes back to its owner's adventure deck.
		assertEquals(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, opponentsSite2);
		assertEquals(site3, scn.GetSite(3));
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SmeagolAloneIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var getaway = scn.GetFreepsCard("getaway");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(getaway, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		assertEquals(0, scn.GetBurdens());
		// No Ring-bound Hobbit, and no Sméagol: Gollum is not him.
		assertFalse(scn.FreepsPlayAvailable(getaway));

		scn.FreepsPlayCard(smeagol);
		// Sméagol's own cost.
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(getaway));
		scn.FreepsPlayCard(getaway);

		assertEquals(3, scn.GetBurdens()); // 1 for Sméagol + 2 for the event
		assertEquals(site2, scn.GetSite(2));
		assertEquals(site3, scn.GetSite(3));
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileBurdensCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var getaway = scn.GetFreepsCard("getaway");
		var smeagol = scn.GetFreepsCard("smeagol");
		var galadriel = scn.GetRingBearer();
		scn.MoveCardsToHand(getaway);
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(getaway));

		scn.HinderCard(galadriel);
		scn.FreepsRefreshDecision();

		// The 2 burdens to be added are a cost, and while the Ring-bearer is hindered no burden can be added.
		// Smeagol, who is not hindered, is still there to be spotted.
		assertTrue(scn.IsHindered(galadriel));
		assertFalse(scn.IsHindered(smeagol));
		assertFalse(scn.FreepsPlayAvailable(getaway));
		assertEquals(0, scn.GetBurdens());
		assertNull(scn.GetSite(2));
		assertInZone(Zone.HAND, getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASmeagolOfAnotherCultureIsEnoughToPlayItToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var getaway = scn.GetFreepsCard("getaway");
		var smeagol = scn.GetFreepsCard("shireSmeagol");
		scn.MoveCardsToHand(getaway, smeagol);

		scn.StartGame();

		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		assertEquals(0, scn.GetBurdens());
		// No Ring-bound Hobbit, and no Smeagol.
		assertFalse(scn.FreepsPlayAvailable(getaway));

		scn.FreepsPlayCard(smeagol);
		// Smeagol's own cost.
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(getaway));
		scn.FreepsPlayCard(getaway);

		assertEquals(3, scn.GetBurdens()); // 1 for Smeagol + 2 for the event
		assertEquals(site2, scn.GetSite(2));
		assertEquals(site3, scn.GetSite(3));
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
