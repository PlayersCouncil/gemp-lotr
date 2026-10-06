package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_022_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two copies: one to play, and one to be read where it lies.
					put("fell1", "102_22");
					put("fell2", "102_22");

					// Goblin Patrol Troop: a [moria] minion costing 6, site number 4, no text.
					put("troop", "1_177");
					// Uruk Savage: an [isengard] minion; Damage +1 is its whole text.
					put("savage", "1_151");
					// No Business of Ours: a Shadow condition; it keeps the Free Peoples player from looking
					// at the Shadow hand.
					put("business", "2_44");
					// Constantly Threatening: a Shadow condition costing 1, played to take one token out of
					// the pool.  Its ability needs the fellowship to have moved twice.
					put("threatening", "4_6");

					// Shadow items: possessions and artifacts, of four cultures.  They are only ever in the
					// draw deck or in hand, so none of their text comes into play.
					put("sword", "1_160");       // Uruk-hai Sword: [isengard] possession
					put("palantir", "3_67");     // The Palantir of Orthanc: [isengard] artifact
					put("scimitar", "1_180");    // Goblin Scimitar: [moria] possession
					put("steed", "1_208");       // Black Steed: [ringwraith] possession
					put("stone", "9_47");        // Ithil Stone: [sauron] artifact

					// Free Peoples items.
					put("asfaloth", "1_31");     // Asfaloth: [elven] possession
					put("nenya", "3_23");        // Nenya: [elven] artifact
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FellfromWisdomintoFollyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Fell from Wisdom into Folly
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		 * Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (5) to take up to 4 Shadow items
		 * into hand from your draw deck. If in region 1, this action can be performed from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fell1");

		assertEquals("Fell from Wisdom into Folly", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrawActionNeedsAMinionToSpotAndRemovesThreeToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell1");
		var troop = scn.GetShadowCard("troop");
		var business = scn.GetShadowCard("business");
		scn.MoveCardsToHand(fell, troop);
		// A Shadow card on the table that is no minion.
		scn.MoveCardsToSupportArea(business);

		scn.StartGame();
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 14 set + 1 companion + 2 for the site: one short of the 18 the other action spots, and no minion.
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.HAND, fell);
		assertFalse(scn.ShadowPlayAvailable(fell));

		// 6 to play + 2 roaming: the Troop's site number is 4 and the fellowship is at site 2.
		scn.ShadowPlayCard(troop);
		assertEquals(17 - 8, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fell));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(fell);
		assertEquals(9 - 3, scn.GetTwilight());
		// The event leaves the hand and two cards come into it.
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, fell);
		// Drawing looks through nothing.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawActionIsOfferedWithExactlyThreeTwilightTokensAndNotWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell1 = scn.GetShadowCard("fell1");
		var fell2 = scn.GetShadowCard("fell2");
		var threatening = scn.GetShadowCard("threatening");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(fell1, fell2, threatening);
		scn.MoveCardsToTopOfDeck(sword, palantir);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 5 set + 1 companion + 0 for the site.
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(fell1);
		assertEquals(6 - 3, scn.GetTwilight());
		assertInZone(Zone.HAND, sword, palantir);
		// Exactly three left: the second copy is still on offer.
		assertInZone(Zone.HAND, fell2);
		assertTrue(scn.ShadowPlayAvailable(fell2));

		// A condition costing 1 leaves two, with the Savage still there: one short.
		scn.ShadowPlayCard(threatening);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowPlayAvailable(fell2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSeventeenTwilightTokensInRegion1OnlyTheDrawIsPerformedAndNotFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell1 = scn.GetShadowCard("fell1");
		var fell2 = scn.GetShadowCard("fell2");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(fell1);
		scn.MoveCardsToTopOfDeck(fell2, sword, palantir);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(16);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 16 set + 1 companion + 0 for the site.
		assertEquals(17, scn.GetTwilight());
		// A minion and 3 tokens are enough for the draw, and the draw is not performed from the draw deck.
		assertInZone(Zone.DECK, fell2);
		assertFalse(scn.ShadowActionAvailable(fell2));
		assertTrue(scn.ShadowPlayAvailable(fell1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// One token short of 18, there is no choosing between the two actions: this is the draw.
		scn.ShadowPlayCard(fell1);
		assertEquals(17 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, fell1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEighteenTwilightTokensAtSite6RemovesFiveToTakeUpToFourShadowItemsFromDrawDeckIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell1 = scn.GetShadowCard("fell1");
		var fell2 = scn.GetShadowCard("fell2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");
		var scimitar = scn.GetShadowCard("scimitar");
		var steed = scn.GetShadowCard("steed");
		var stone = scn.GetShadowCard("stone");
		var asfaloth = scn.GetShadowCard("asfaloth");
		var nenya = scn.GetShadowCard("nenya");

		scn.StartGame();
		scn.SkipToSite(5);
		// The turns played on the way drew cards: put every one where this test wants it.
		scn.MoveCardsToTopOfDeck(fell2, troop, savage, business, threatening, sword, palantir, scimitar, steed, stone, asfaloth, nenya);
		scn.MoveCardsToHand(fell1);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 6 is the last site of region 2.
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 3 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(12, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowPlayAvailable(fell1));
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(fell1);
		assertEquals(18 - 5, scn.GetTwilight());

		// The deck is shown, and then the choice: Shadow possessions and artifacts of any culture.  Not the
		// Free Peoples possession or artifact, nor a Shadow condition, minion or event.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, palantir, scimitar, steed, stone));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(asfaloth, nenya, business, threatening, troop, savage, fell2));
		assertEquals(0, scn.ShadowGetChoiceMin());
		// Five could be taken, and four may be.
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(sword, palantir, scimitar, stone);
		assertInZone(Zone.HAND, sword, palantir, scimitar, stone);
		assertInZone(Zone.DECK, steed, asfaloth, nenya, business, threatening, troop, savage, fell2);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(12 - 4, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, fell1);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithBothActionsOnOfferAtSite2ThePlayerMayChooseTheItemsAndThenTakeNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell1");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 15 set + 1 companion + 2 for the site.
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fell));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// A minion and 18 tokens: both actions are on offer, and the player says which.
		scn.ShadowPlayCard(fell);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("draw 2 cards"));
		assertTrue(scn.ShadowChoiceAvailable("take up to 4 items"));

		scn.ShadowChooseOption("take up to 4 items");
		assertEquals(18 - 5, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, palantir));
		assertEquals(0, scn.ShadowGetChoiceMin());

		// Up to four is none, if the player likes.  The deck was looked through all the same.
		scn.ShadowDeclineChoosing();
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, sword, palantir);
		assertInZone(Zone.DISCARD, fell);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3ItMayBePlayedFromTheDrawDeckAndThenOnlyTakesItems() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell1 = scn.GetShadowCard("fell1");
		var fell2 = scn.GetShadowCard("fell2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");
		var scimitar = scn.GetShadowCard("scimitar");
		var steed = scn.GetShadowCard("steed");
		var stone = scn.GetShadowCard("stone");
		var asfaloth = scn.GetShadowCard("asfaloth");
		var nenya = scn.GetShadowCard("nenya");

		scn.StartGame();
		scn.SkipToSite(2);
		// Both copies in the draw deck and nothing in hand.
		scn.MoveCardsToTopOfDeck(fell1, fell2, troop, business, threatening, sword, palantir, scimitar, steed, stone, asfaloth, nenya);
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
		assertInZone(Zone.DECK, fell1, fell2);
		assertTrue(scn.ShadowActionAvailable(fell1));
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No question of which action: from the draw deck it is the taking of items.
		scn.ShadowUseCardAction(fell1);
		assertEquals(18 - 5, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, palantir, scimitar, steed, stone));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(asfaloth, nenya, business, threatening, troop, fell2));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(palantir, scimitar, steed, stone);
		assertInZone(Zone.HAND, palantir, scimitar, steed, stone);
		assertInZone(Zone.DECK, sword, fell2);
		assertEquals(4, scn.GetShadowHandCount());
		// The event is played and gone, like one played from hand.
		assertInZone(Zone.DISCARD, fell1);
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4ItIsNotPlayedFromTheDrawDeckAndFromHandThePlayerMayChooseTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell1 = scn.GetShadowCard("fell1");
		var fell2 = scn.GetShadowCard("fell2");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(fell1);
		scn.MoveCardsToTopOfDeck(fell2, sword, palantir);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 4 is the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 3 for the site.
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, fell2);
		assertFalse(scn.ShadowActionAvailable(fell2));
		assertTrue(scn.ShadowPlayAvailable(fell1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// A minion and 18 tokens: both actions are on offer, and this time the draw is chosen.
		scn.ShadowPlayCard(fell1);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("draw 2 cards");
		assertEquals(18 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, fell1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndNoMinionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell1");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(fell);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 17 set + 1 companion + 6 for the site: more than 18, and one short of 25.  No minion is in play.
		assertEquals(24, scn.GetTwilight());
		assertInZone(Zone.HAND, fell);
		assertFalse(scn.ShadowPlayAvailable(fell));
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndAMinionOnlyTheDrawIsPerformed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell1 = scn.GetShadowCard("fell1");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(fell1);
		scn.MoveCardsToTopOfDeck(sword, palantir, scimitar);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 17 set + 1 companion + 6 for the site.
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fell1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// 24 is more than 18 and one short of 25: with a minion to spot the event is played, and it draws.
		scn.ShadowPlayCard(fell1);
		assertEquals(24 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, fell1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7TwentyFiveTwilightTokensAreEnoughForTheItemsAndItIsNotPlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell1 = scn.GetShadowCard("fell1");
		var fell2 = scn.GetShadowCard("fell2");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");
		var asfaloth = scn.GetShadowCard("asfaloth");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(fell1);
		scn.MoveCardsToTopOfDeck(fell2, sword, palantir, asfaloth);
		scn.SetTwilight(18);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 18 set + 1 companion + 6 for the site.  No minion is in play.
		assertEquals(25, scn.GetTwilight());
		assertInZone(Zone.DECK, fell2);
		assertFalse(scn.ShadowActionAvailable(fell2));
		assertTrue(scn.ShadowPlayAvailable(fell1));
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(fell1);
		assertEquals(25 - 5, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, palantir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(asfaloth, fell2));
		scn.ShadowChooseCardBPFromSelection(sword, palantir);
		assertInZone(Zone.HAND, sword, palantir);
		assertInZone(Zone.DISCARD, fell1);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
