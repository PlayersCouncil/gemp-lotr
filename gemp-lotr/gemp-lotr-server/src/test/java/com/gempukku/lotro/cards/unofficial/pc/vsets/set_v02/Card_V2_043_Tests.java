package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_043_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two copies: one to play, and one to be read where it lies.
					put("hope1", "102_43");
					put("hope2", "102_43");

					// Goblin Patrol Troop: a [moria] minion costing 6, site number 4, no text.
					put("troop", "1_177");
					// Uruk Savage: an [isengard] minion; Damage +1 is its whole text.
					put("savage", "1_151");

					// No Business of Ours: an [isengard] condition.  On the table in one test, as a Shadow card
					// that is no minion; it keeps the Free Peoples player from looking at the Shadow hand.
					put("business", "2_44");
					// Constantly Threatening: a [dunland] condition costing 1, played in one test to take one
					// token out of the pool.  Its ability needs the fellowship to have moved twice.
					put("threatening", "4_6");

					// Shadow events of five cultures and five phases.  They are only ever in the draw deck, the
					// discard pile or the hand, and none is played.
					put("bred", "1_121");        // Bred for Battle: [isengard], skirmish
					put("host", "1_187");        // Host of Thousands: [moria], Shadow
					put("hate", "1_250");        // Hate: [sauron], maneuver
					put("blades", "1_203");      // All Blades Perish: [ringwraith], response
					put("fear", "4_240");        // New Fear: [raider], regroup
					put("wake", "4_35");         // Wake of Destruction: [dunland]; the one kept in the discard pile

					// Cards in the draw deck that are not Shadow events.
					put("strike", "1_3");        // Axe Strike: a Free Peoples event
					put("scimitar", "1_180");    // Goblin Scimitar: a Shadow possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AllHopeofSecrecyLostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: All Hope of Secrecy Lost
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		 * Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (4) to take up to 4 Shadow
		 * events into hand from your draw deck. If in region 1, this action can be performed from your
		 * draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope1");

		assertEquals("All Hope of Secrecy Lost", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrawActionNeedsAMinionToSpotAndRemovesThreeToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var troop = scn.GetShadowCard("troop");
		var business = scn.GetShadowCard("business");
		scn.MoveCardsToHand(hope1, troop);
		// A Shadow card on the table that is no minion.
		scn.MoveCardsToSupportArea(business);

		scn.StartGame();
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 2 for the site: one short of the 18 the other action spots, and no minion.
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.HAND, hope1);
		assertFalse(scn.ShadowPlayAvailable(hope1));
		// Nor is the copy in the draw deck on offer, in region 1 though this is.
		assertInZone(Zone.DECK, hope2);
		assertFalse(scn.ShadowActionAvailable(hope2));

		// 6 to play + 2 roaming: the Troop's site number is 4 and the fellowship is at site 2.
		scn.ShadowPlayCard(troop);
		assertEquals(17 - 8, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hope1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(hope1);
		assertEquals(9 - 3, scn.GetTwilight());
		// The event leaves the hand and two cards come into it.
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, hope1);
		// Drawing looks through nothing.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawActionIsOfferedWithExactlyThreeTwilightTokensAndNotWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var threatening = scn.GetShadowCard("threatening");
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		var hate = scn.GetShadowCard("hate");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(hope1, hope2, threatening);
		scn.MoveCardsToTopOfDeck(bred, hate);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 5 set + 1 companion + 0 for the site.
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(hope1);
		assertEquals(6 - 3, scn.GetTwilight());
		assertInZone(Zone.HAND, bred, hate);
		// Exactly three left: the second copy is still on offer.
		assertInZone(Zone.HAND, hope2);
		assertTrue(scn.ShadowPlayAvailable(hope2));

		// A condition costing 1 leaves two, with the Savage still there: one short.
		scn.ShadowPlayCard(threatening);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowPlayAvailable(hope2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSeventeenTwilightTokensInRegion1OnlyTheDrawIsPerformedAndNotFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		var hate = scn.GetShadowCard("hate");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(hope1);
		scn.MoveCardsToTopOfDeck(hope2, bred, hate);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(16);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 16 set + 1 companion + 0 for the site.
		assertEquals(17, scn.GetTwilight());
		// A minion and 3 tokens are enough for the draw, and the draw is not performed from the draw deck.
		assertInZone(Zone.DECK, hope2);
		assertFalse(scn.ShadowActionAvailable(hope2));
		assertTrue(scn.ShadowPlayAvailable(hope1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// One token short of 18, there is no choosing between the two actions: this is the draw.
		scn.ShadowPlayCard(hope1);
		assertEquals(17 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, hope1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEighteenTwilightTokensAtSite6RemovesFourToTakeUpToFourShadowEventsFromDrawDeckIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");
		var bred = scn.GetShadowCard("bred");
		var host = scn.GetShadowCard("host");
		var hate = scn.GetShadowCard("hate");
		var blades = scn.GetShadowCard("blades");
		var fear = scn.GetShadowCard("fear");
		var wake = scn.GetShadowCard("wake");
		var strike = scn.GetShadowCard("strike");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(5);
		// The turns played on the way drew cards: put every one where this test wants it.
		scn.MoveCardsToTopOfDeck(hope2, troop, savage, business, threatening, bred, host, hate, blades, fear, strike, scimitar);
		// A Shadow event that is not in the draw deck.
		scn.MoveCardsToDiscard(wake);
		scn.MoveCardsToHand(hope1);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 6 is the last site of region 2.
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 3 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(12, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowPlayAvailable(hope1));
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(hope1);
		assertEquals(18 - 4, scn.GetTwilight());

		// The deck is shown, and then the choice: Shadow events of any culture and any phase, the other copy
		// of this one among them.  Not the Free Peoples event, nor a Shadow condition, possession or minion,
		// nor the Shadow event in the discard pile.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(bred, host, hate, blades, fear, hope2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(strike, scimitar, business, threatening, troop, savage, wake));
		assertEquals(0, scn.ShadowGetChoiceMin());
		// Six could be taken, and four may be.
		assertEquals(4, scn.ShadowGetChoiceMax());

		// The other player is shown nothing: the game log is what tells him which cards were taken.  The log
		// is read from here, after the line that named this event as it was played, so that the copy taken
		// is named by its taking alone.
		scn.MarkGameLog();
		scn.ShadowChooseCardBPFromSelection(host, blades, fear, hope2);
		assertInZone(Zone.HAND, host, blades, fear, hope2);
		assertInZone(Zone.DECK, bred, hate, strike, scimitar, business, threatening, troop, savage);
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(host, blades, fear, hope2));
		// The Shadow events left in the deck are not named.
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(bred));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(hate));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInZone(Zone.DISCARD, wake);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(12 - 4, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, hope1);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEighteenTwilightTokensAtSite2AndNoMinionItIsPlayedFromHandAndTheCopyInTheDrawDeckIsOnOfferToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var bred = scn.GetShadowCard("bred");
		var host = scn.GetShadowCard("host");
		var hate = scn.GetShadowCard("hate");
		var blades = scn.GetShadowCard("blades");
		var fear = scn.GetShadowCard("fear");
		var wake = scn.GetShadowCard("wake");
		var strike = scn.GetShadowCard("strike");
		var business = scn.GetShadowCard("business");
		scn.MoveCardsToHand(hope1);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 15 set + 1 companion + 2 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(13, scn.GetShadowDeckCount());
		// In region 1 with 18 tokens the copy in hand can be played and the copy in the draw deck performed.
		assertTrue(scn.ShadowPlayAvailable(hope1));
		assertInZone(Zone.DECK, hope2);
		assertTrue(scn.ShadowActionAvailable(hope2));
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(hope1);
		assertEquals(18 - 4, scn.GetTwilight());

		// Every Shadow event is in the draw deck here, the other copy of this one included: seven of them.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(bred, host, hate, blades, fear, wake, hope2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(strike, business));
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(bred, hate, blades, wake);
		assertInZone(Zone.HAND, bred, hate, blades, wake);
		assertInZone(Zone.DECK, host, fear, hope2, strike, business);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(13 - 4, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, hope1);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());

		// Fourteen tokens are left: the copy in the draw deck needs its 18 as well.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DECK, hope2);
		assertFalse(scn.ShadowActionAvailable(hope2));
	}

	@Test
	public void WithBothActionsOnOfferAtSite2ThePlayerMayChooseTheEventsAndThenTakeNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope1");
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		var hate = scn.GetShadowCard("hate");
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 15 set + 1 companion + 2 for the site.
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hope));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// A minion and 18 tokens: both actions are on offer, and the player says which.
		scn.ShadowPlayCard(hope);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("draw 2 cards"));
		assertTrue(scn.ShadowChoiceAvailable("take up to 4 events"));

		scn.ShadowChooseOption("take up to 4 events");
		assertEquals(18 - 4, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(bred, hate));
		assertEquals(0, scn.ShadowGetChoiceMin());

		// Up to four is none, if the player likes.  The deck was looked through all the same.
		scn.ShadowDeclineChoosing();
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, bred, hate);
		assertInZone(Zone.DISCARD, hope);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3ItMayBePerformedFromTheDrawDeckAndThenOnlyTakesEvents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");
		var bred = scn.GetShadowCard("bred");
		var host = scn.GetShadowCard("host");
		var hate = scn.GetShadowCard("hate");
		var blades = scn.GetShadowCard("blades");
		var fear = scn.GetShadowCard("fear");
		var wake = scn.GetShadowCard("wake");
		var strike = scn.GetShadowCard("strike");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(2);
		// One copy in the draw deck and nothing in hand.  The other copy is in the discard pile, so that
		// no card of this name is left in the deck once the first is being performed.
		scn.MoveCardsToTopOfDeck(hope1, troop, business, threatening, bred, host, hate, blades, fear, strike, scimitar);
		scn.MoveCardsToDiscard(hope2, wake);
		// A minion in play, so that the draw would be on offer too if the event were played from hand.
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 3 is the last site of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 17 set + 1 companion + 0 for the site.
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, hope1);
		assertTrue(scn.ShadowActionAvailable(hope1));
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No question of which action: from the draw deck it is the taking of events.
		scn.ShadowUseCardAction(hope1);
		assertEquals(18 - 4, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());

		// The event itself has left the deck and is not one of the events it may take.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(bred, host, hate, blades, fear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hope1, strike, scimitar, business, threatening, troop, wake));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(bred, host, hate, fear);
		assertInZone(Zone.HAND, bred, host, hate, fear);
		assertInZone(Zone.DECK, blades, strike);
		assertInZone(Zone.DISCARD, hope2, wake);
		assertEquals(4, scn.GetShadowHandCount());
		// The event is played and gone, like one played from hand.
		assertInZone(Zone.DISCARD, hope1);
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4ItIsNotPerformedFromTheDrawDeckAndFromHandThePlayerMayChooseTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		var hate = scn.GetShadowCard("hate");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(hope1);
		scn.MoveCardsToTopOfDeck(hope2, bred, hate);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 4 is the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 3 for the site.
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, hope2);
		assertFalse(scn.ShadowActionAvailable(hope2));
		assertTrue(scn.ShadowPlayAvailable(hope1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// A minion and 18 tokens: both actions are on offer, and this time the draw is chosen.
		scn.ShadowPlayCard(hope1);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("draw 2 cards");
		assertEquals(18 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, hope1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndNoMinionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope1");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(hope);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 17 set + 1 companion + 6 for the site: more than 18, and one short of 25.  No minion is in play.
		assertEquals(24, scn.GetTwilight());
		assertInZone(Zone.HAND, hope);
		assertFalse(scn.ShadowPlayAvailable(hope));
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndAMinionOnlyTheDrawIsPerformed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope1");
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		var host = scn.GetShadowCard("host");
		var hate = scn.GetShadowCard("hate");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(hope);
		scn.MoveCardsToTopOfDeck(bred, host, hate);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 17 set + 1 companion + 6 for the site.
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hope));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// 24 is more than 18 and one short of 25: with a minion to spot the event is played, and it draws.
		scn.ShadowPlayCard(hope);
		assertEquals(24 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, hope);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7TwentyFiveTwilightTokensAreEnoughForTheEventsAndItIsNotPerformedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetShadowCard("hope1");
		var hope2 = scn.GetShadowCard("hope2");
		var bred = scn.GetShadowCard("bred");
		var hate = scn.GetShadowCard("hate");
		var strike = scn.GetShadowCard("strike");
		var business = scn.GetShadowCard("business");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(hope1);
		scn.MoveCardsToTopOfDeck(hope2, bred, hate, strike, business);
		scn.SetTwilight(18);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 18 set + 1 companion + 6 for the site.  No minion is in play.
		assertEquals(25, scn.GetTwilight());
		assertInZone(Zone.DECK, hope2);
		assertFalse(scn.ShadowActionAvailable(hope2));
		assertTrue(scn.ShadowPlayAvailable(hope1));
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(hope1);
		assertEquals(25 - 4, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(bred, hate, hope2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(strike, business));
		scn.ShadowChooseCardBPFromSelection(bred, hope2);
		assertInZone(Zone.HAND, bred, hope2);
		assertInZone(Zone.DECK, hate, strike, business);
		assertInZone(Zone.DISCARD, hope1);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
