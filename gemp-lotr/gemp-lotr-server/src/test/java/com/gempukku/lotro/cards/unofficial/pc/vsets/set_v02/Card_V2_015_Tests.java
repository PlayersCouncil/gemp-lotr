package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("event", "102_15");
					put("event2", "102_15");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, cost 2, site 3, no text
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai; Damage +1 is its whole text
					put("pinned", "1_192");     // Pinned Down: a Shadow condition that does nothing without a [moria] archer

					// Goblin Patrol Troop, which has no text: these are the cards that get drawn.
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
					put("filler7", "1_177");
					put("filler8", "1_177");
					put("filler9", "1_177");
					put("filler10", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WatchingAndWaitingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Watching and Waiting
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		 * 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (3) to draw 8 cards. If in region 1, this action can be performed from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("event");

		assertEquals("Watching and Waiting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NeedsAMinionOfYoursAndThenRemoves3ToDraw2Cards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var brigand = scn.GetShadowCard("brigand");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToHand(event, brigand);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		// 4 set + 1 companion + 2 for site 2
		scn.SetTwilight(4);
		scn.FreepsPass();

		// The Shadow player has a condition in play and no minion.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, pinned);
		assertFalse(scn.ShadowPlayAvailable(event));

		// 2 to play + 2 roaming
		scn.ShadowPlayCard(brigand);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		// With fewer than 18 in the pool there is nothing to choose between.
		scn.ShadowPlayCard(event);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAMinionAndOnly2InThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(event, brigand);

		scn.StartGame();
		// 3 set + 1 companion + 2 for site 2
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertEquals(6, scn.GetTwilight());

		// 2 to play + 2 roaming
		scn.ShadowPlayCard(brigand);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertFalse(scn.ShadowPlayAvailable(event));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void Spots18TwilightAndRemoves3ToDraw8CardsWithNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentShuffles = scn.GetFreepsDeckShuffleCount();

		// With no minion there is nothing to choose between.
		scn.ShadowPlayCard(event);
		assertEquals(15, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(deck - 8, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		// Played from hand, the deck was not looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWith17InThePoolAndNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		// 14 set + 1 companion + 2 for site 2
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(17, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(event));
		// Nor is the copy in the draw deck offered.
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void With17InThePoolAndAMinionOnlyTheSmallDrawIsMadeAndOnlyFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		// 14 set + 1 companion + 2 for site 2
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		// Region 1, and the copy in the draw deck is not offered: the action that can be performed from
		// there is the one that spots 18.
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));
		assertEquals(1, scn.GetShadowHandCount());

		// No choice is put: 2 cards are drawn.
		scn.ShadowPlayCard(event);
		assertEquals(14, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMinionAnd18InThePoolTheSmallDrawMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(event);
		assertEquals(2, scn.ShadowGetMultipleChoices().size());
		assertTrue(scn.ShadowChoiceAvailable("draw 2 cards"));
		assertTrue(scn.ShadowChoiceAvailable("draw 8 cards"));

		scn.ShadowChoose("draw 2 cards");
		assertEquals(15, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMinionAnd18InThePoolTheBigDrawMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(event);
		assertEquals(2, scn.ShadowGetMultipleChoices().size());

		scn.ShadowChoose("draw 8 cards");
		assertEquals(15, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(deck - 8, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite6StillInRegion2APoolOf18IsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");

		scn.StartGame();
		scn.SkipToSite(5);

		// The turns skipped have drawn the deck into hand: the fillers go back to be drawn.
		scn.MoveCardsToTopOfShadowDeck("filler1", "filler2", "filler3", "filler4", "filler5",
				"filler6", "filler7", "filler8", "filler9", "filler10");
		scn.MoveCardsToHand(event);
		// 14 set + 1 companion + 3 for site 6
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(event);
		assertEquals(15, scn.GetTwilight());
		// the event gone, 8 drawn
		assertEquals(hand + 7, scn.GetShadowHandCount());
		assertEquals(deck - 8, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7InRegion3APoolOf24IsNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToHand(event);
		// 17 set + 1 companion + 6 for site 7
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(24, scn.GetTwilight());
		assertInZone(Zone.HAND, event);
		assertFalse(scn.ShadowPlayAvailable(event));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7With24InThePoolAndAMinionOnlyTheSmallDrawIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToTopOfShadowDeck("filler1", "filler2", "filler3", "filler4", "filler5",
				"filler6", "filler7", "filler8", "filler9", "filler10");
		scn.MoveCardsToHand(event);
		scn.MoveMinionsToTable(warrior);
		// 17 set + 1 companion + 6 for site 7
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		int hand = scn.GetShadowHandCount();

		// No choice is put: 2 cards are drawn.
		scn.ShadowPlayCard(event);
		assertEquals(21, scn.GetTwilight());
		// the event gone, 2 drawn
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7InRegion3APoolOf25IsEnoughAndOnlyFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToTopOfShadowDeck("filler1", "filler2", "filler3", "filler4", "filler5",
				"filler6", "filler7", "filler8", "filler9", "filler10");
		scn.MoveCardsToHand(event);
		scn.MoveCardsToBottomOfDeck(event2);
		// 18 set + 1 companion + 6 for site 7
		scn.SetTwilight(18);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		// Not region 1: the copy in the draw deck is not offered.
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(event);
		assertEquals(22, scn.GetTwilight());
		// the event gone, 8 drawn
		assertEquals(hand + 7, scn.GetShadowHandCount());
		assertEquals(deck - 8, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3InRegion1TheBigDrawCanBePerformedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");

		scn.StartGame();
		scn.SkipToSite(2);

		scn.MoveCardsToTopOfShadowDeck("filler1", "filler2", "filler3", "filler4", "filler5",
				"filler6", "filler7", "filler8", "filler9", "filler10");
		scn.MoveCardsToBottomOfDeck(event);
		scn.MoveCardsToDiscard(event2);
		// 17 set + 1 companion + 0 for site 3
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, event);
		assertTrue(scn.ShadowActionAvailable(event));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowUseCardAction(event);
		assertEquals(15, scn.GetTwilight());
		assertEquals(hand + 8, scn.GetShadowHandCount());
		// the event and the 8 drawn
		assertEquals(deck - 9, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		// The deck was looked through for the event, so it is shuffled: once as the event leaves it and
		// once more by the card.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FromTheDrawDeckOnlyTheBigDrawIsMadeEvenWithAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToDiscard(event2);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, event);
		assertTrue(scn.ShadowActionAvailable(event));
		assertEquals(0, scn.GetShadowHandCount());

		// From hand this board puts a choice; from the draw deck it does not.
		scn.ShadowUseCardAction(event);
		assertEquals(15, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4InRegion2ItCannotBePerformedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveCardsToHand(event);
		scn.MoveCardsToBottomOfDeck(event2);
		// 14 set + 1 companion + 3 for site 4
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		// The copy in hand can be played; the copy in the draw deck is not offered.
		assertTrue(scn.ShadowPlayAvailable(event));
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
