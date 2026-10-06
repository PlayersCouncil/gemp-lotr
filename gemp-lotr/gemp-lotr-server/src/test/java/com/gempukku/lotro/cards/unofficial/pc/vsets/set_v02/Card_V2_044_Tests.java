package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_044_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two copies: one to play, and one to be read where it lies.
					put("threats1", "102_44");
					put("threats2", "102_44");

					// Ulaire Lemenya: a [ringwraith] Nazgul costing 4, site number 3.  His text is about
					// companions who bear a [ringwraith] condition, and nobody here does.
					put("lemenya", "1_232");
					// Uruk Savage: an [isengard] Uruk-hai; Damage +1 is its whole text.
					put("savage", "1_151");

					// No Business of Ours: an [isengard] condition.  On the table in one test, as a Shadow card
					// that is no minion; it keeps the Free Peoples player from looking at the Shadow hand.
					put("business", "2_44");
					// Constantly Threatening: a [dunland] condition costing 1, played in one test to take one
					// token out of the pool.  Its ability needs the fellowship to have moved twice.
					put("threatening", "4_6");

					// Dwarf Guard: a companion whose whole text is what it takes to play him.  No more
					// threats may be added than there are companions, so the tests that add threats need
					// companions for them: with six Guards and Frodo, seven could be added.
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThreatsonAllSidesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Threats on All Sides
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		 * Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (9) to add up to 6 threats.
		 * If in region 1, this action can be performed from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("threats1");

		assertEquals("Threats on All Sides", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrawActionNeedsAMinionToSpotAndRemovesThreeToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats1 = scn.GetShadowCard("threats1");
		var threats2 = scn.GetShadowCard("threats2");
		var lemenya = scn.GetShadowCard("lemenya");
		var business = scn.GetShadowCard("business");
		scn.MoveCardsToHand(threats1, lemenya);
		// A Shadow card on the table that is no minion.
		scn.MoveCardsToSupportArea(business);

		scn.StartGame();
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 2 for the site: one short of the 18 the other action spots, and no minion.
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.HAND, threats1);
		assertFalse(scn.ShadowPlayAvailable(threats1));
		// Nor is the copy in the draw deck on offer, in region 1 though this is.
		assertInZone(Zone.DECK, threats2);
		assertFalse(scn.ShadowActionAvailable(threats2));

		// 4 to play + 2 roaming: Lemenya's site number is 3 and the fellowship is at site 2.
		scn.ShadowPlayCard(lemenya);
		assertEquals(17 - 6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(threats1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(threats1);
		assertEquals(11 - 3, scn.GetTwilight());
		// The event leaves the hand and two cards come into it.
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, threats1);
		// Drawing looks through nothing.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawActionIsOfferedWithExactlyThreeTwilightTokensAndNotWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats1 = scn.GetShadowCard("threats1");
		var threats2 = scn.GetShadowCard("threats2");
		var threatening = scn.GetShadowCard("threatening");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(threats1, threats2, threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 3 set + 1 companion + 2 for the site.
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(threats1);
		assertEquals(6 - 3, scn.GetTwilight());
		// Exactly three left: the second copy is still on offer.
		assertInZone(Zone.HAND, threats2);
		assertTrue(scn.ShadowPlayAvailable(threats2));

		// A condition costing 1 leaves two, with the Savage still there: one short.
		scn.ShadowPlayCard(threatening);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowPlayAvailable(threats2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSeventeenTwilightTokensInRegion1OnlyTheDrawIsPerformedAndNotFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats1 = scn.GetShadowCard("threats1");
		var threats2 = scn.GetShadowCard("threats2");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		scn.SkipToSite(2);
		// The turns played on the way drew cards: put every one where this test wants it.
		scn.MoveCardsToHand(threats1);
		scn.MoveCardsToBottomOfDeck(threats2);
		scn.MoveCardsToTopOfDeck(business, threatening);
		scn.MoveMinionsToTable(savage);
		// Three companions, so that threats could be added if the event came to that.
		scn.MoveCompanionsToTable("guard1", "guard2");
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 14 set + 3 companions + 0 for the site.
		assertEquals(17, scn.GetTwilight());
		// A minion and 3 tokens are enough for the draw, and the draw is not performed from the draw deck.
		assertInZone(Zone.DECK, threats2);
		assertFalse(scn.ShadowActionAvailable(threats2));
		assertTrue(scn.ShadowPlayAvailable(threats1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// One token short of 18, there is no choosing between the two actions: this is the draw.
		scn.ShadowPlayCard(threats1);
		assertEquals(17 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetThreats());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, threats1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEighteenTwilightTokensAtSite2AndNoMinionRemovesNineToAddUpToSixThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats1 = scn.GetShadowCard("threats1");
		var threats2 = scn.GetShadowCard("threats2");
		scn.MoveCardsToHand(threats1);
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4", "guard5", "guard6");

		scn.StartGame();
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 9 set + 7 companions + 2 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		// In region 1 with 18 tokens the copy in hand can be played and the copy in the draw deck performed.
		assertTrue(scn.ShadowPlayAvailable(threats1));
		assertInZone(Zone.DECK, threats2);
		assertTrue(scn.ShadowActionAvailable(threats2));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(threats1);
		assertEquals(18 - 9, scn.GetTwilight());
		// With seven companions the rules would let seven threats on: the six is the card's.
		assertTrue(scn.ShadowDecisionAvailable("how many threats"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.ShadowGetChoiceMax());

		scn.ShadowChoose("6");
		assertEquals(6, scn.GetThreats());
		assertEquals(9, scn.GetTwilight());
		// Nothing is drawn, and nothing is looked through.
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.DISCARD, threats1);

		// Nine tokens are left: the copy in the draw deck needs its 18 as well.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DECK, threats2);
		assertFalse(scn.ShadowActionAvailable(threats2));
	}

	@Test
	public void WithBothActionsOnOfferAtSite2ThePlayerMayChooseTheThreatsAndThenAddNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats = scn.GetShadowCard("threats1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(threats);
		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(13);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 13 set + 3 companions + 2 for the site.
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(threats));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();

		// A minion and 18 tokens: both actions are on offer, and the player says which.
		scn.ShadowPlayCard(threats);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("draw 2 cards"));
		assertTrue(scn.ShadowChoiceAvailable("add up to 6 threats"));

		scn.ShadowChooseOption("add up to 6 threats");
		assertEquals(18 - 9, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetChoiceMin());

		// Up to six is none, if the player likes.  The nine are spent all the same, and nothing is drawn.
		scn.ShadowChoose("0");
		assertEquals(0, scn.GetThreats());
		assertEquals(9, scn.GetTwilight());
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, threats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3ItMayBePerformedFromTheDrawDeckAndThenOnlyAddsThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats1 = scn.GetShadowCard("threats1");
		var threats2 = scn.GetShadowCard("threats2");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		scn.SkipToSite(2);
		// One copy in the draw deck, with cards above it that a draw would take.  The other copy is in the
		// discard pile.
		scn.MoveCardsToTopOfDeck(threats1, business, threatening);
		scn.MoveCardsToDiscard(threats2);
		// A minion in play, so that the draw would be on offer too if the event were played from hand.
		scn.MoveMinionsToTable(savage);
		scn.MoveCompanionsToTable("guard1", "guard2");
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 3 is the last site of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 15 set + 3 companions + 0 for the site.
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DECK, threats1);
		assertTrue(scn.ShadowActionAvailable(threats1));
		// The draw deck is the only other place it is performed from.
		assertInZone(Zone.DISCARD, threats2);
		assertFalse(scn.ShadowActionAvailable(threats2));
		assertFalse(scn.ShadowPlayAvailable(threats2));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No question of which action: from the draw deck it is the threats.
		scn.ShadowUseCardAction(threats1);
		assertEquals(18 - 9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("how many threats"));
		assertEquals(0, scn.ShadowGetChoiceMin());

		// Three could be added here, there being three companions, and two are.
		scn.ShadowChoose("2");
		assertEquals(2, scn.GetThreats());
		assertEquals(9, scn.GetTwilight());
		// The event is played and gone, like one played from hand.  Nothing was drawn.
		assertInZone(Zone.DISCARD, threats1);
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, business, threatening);
		// A draw deck that a card was played from has been looked through, and is shuffled.
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4ItIsNotPerformedFromTheDrawDeckAndFromHandThePlayerMayChooseTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats1 = scn.GetShadowCard("threats1");
		var threats2 = scn.GetShadowCard("threats2");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(threats1);
		scn.MoveCardsToBottomOfDeck(threats2);
		scn.MoveCardsToTopOfDeck(business, threatening);
		scn.MoveMinionsToTable(savage);
		scn.MoveCompanionsToTable("guard1", "guard2");
		scn.SetTwilight(12);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 4 is the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 12 set + 3 companions + 3 for the site.
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, threats2);
		assertFalse(scn.ShadowActionAvailable(threats2));
		assertTrue(scn.ShadowPlayAvailable(threats1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// A minion and 18 tokens: both actions are on offer, and this time the draw is chosen.
		scn.ShadowPlayCard(threats1);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("draw 2 cards");
		assertEquals(18 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetThreats());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, threats1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEighteenTwilightTokensAtSite6AndNoMinionFewerThreatsThanCouldBeAddedMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats = scn.GetShadowCard("threats1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(threats);
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3");
		scn.SetTwilight(11);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 6 is the last site of region 2.
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 11 set + 4 companions + 3 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(threats));

		scn.ShadowPlayCard(threats);
		assertEquals(18 - 9, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetChoiceMin());

		// Four could be added, there being four companions, and three are.
		scn.ShadowChoose("3");
		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.DISCARD, threats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndNoMinionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats = scn.GetShadowCard("threats1");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(threats);
		scn.MoveCompanionsToTable("guard1", "guard2");
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 15 set + 3 companions + 6 for the site: more than 18, and one short of 25.  No minion is in play.
		assertEquals(24, scn.GetTwilight());
		assertInZone(Zone.HAND, threats);
		assertFalse(scn.ShadowPlayAvailable(threats));
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndAMinionOnlyTheDrawIsPerformed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats = scn.GetShadowCard("threats1");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(threats);
		scn.MoveCardsToTopOfDeck(business, threatening);
		scn.MoveMinionsToTable(savage);
		scn.MoveCompanionsToTable("guard1", "guard2");
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 15 set + 3 companions + 6 for the site.
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(threats));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();

		// 24 is more than 18 and one short of 25: with a minion to spot the event is played, and it draws.
		scn.ShadowPlayCard(threats);
		assertEquals(24 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DISCARD, threats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7TwentyFiveTwilightTokensAreEnoughForTheThreatsAndItIsNotPerformedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats1 = scn.GetShadowCard("threats1");
		var threats2 = scn.GetShadowCard("threats2");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(threats1);
		scn.MoveCardsToTopOfDeck(threats2);
		scn.MoveCompanionsToTable("guard1", "guard2");
		scn.SetTwilight(16);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 16 set + 3 companions + 6 for the site.  No minion is in play.
		assertEquals(25, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DECK, threats2);
		assertFalse(scn.ShadowActionAvailable(threats2));
		assertTrue(scn.ShadowPlayAvailable(threats1));

		scn.ShadowPlayCard(threats1);
		assertEquals(25 - 9, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetChoiceMin());

		scn.ShadowChoose("1");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, threats1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThreatsActionIsOfferedAndPaidForWhenNoMoreThreatsCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats = scn.GetShadowCard("threats1");
		scn.MoveCardsToHand(threats);

		scn.StartGame();
		// One companion and one threat: no more may be added.
		scn.AddThreats(1);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 15 set + 1 companion + 2 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(threats));

		scn.ShadowPlayCard(threats);
		assertEquals(18 - 9, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, threats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawActionIsOfferedAndPaidForWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threats = scn.GetShadowCard("threats1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(threats);
		// Every other card of the Shadow player's leaves his draw deck.
		scn.MoveCardsToShadowDiscard("threats2", "lemenya", "business", "threatening",
				"guard1", "guard2", "guard3", "guard4", "guard5", "guard6");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 3 set + 1 companion + 2 for the site.
		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(threats));

		scn.ShadowPlayCard(threats);
		assertEquals(6 - 3, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, threats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
