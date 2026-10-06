package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_042_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two copies: one to play, and one to be read where it lies.
					put("valiant1", "102_42");
					put("valiant2", "102_42");

					// Goblin Patrol Troop: a [moria] minion costing 6, site number 4, no text.
					put("troop", "1_177");
					// Uruk Savage: an [isengard] minion; Damage +1 is its whole text.
					put("savage", "1_151");

					// Shadow conditions of five cultures.  Apart from the two named below they are only ever
					// in the draw deck, the discard pile or the hand, so none of their text comes into play.
					// No Business of Ours: [isengard].  On the table in one test, as a Shadow card that is no
					// minion; it keeps the Free Peoples player from looking at the Shadow hand.
					put("business", "2_44");
					// Constantly Threatening: [dunland], costing 1, played in one test to take one token out of
					// the pool.  Its ability needs the fellowship to have moved twice.
					put("threatening", "4_6");
					put("armory", "1_173");      // Goblin Armory: [moria]
					put("evil", "1_283");        // You Bring Great Evil: [sauron], plays on a companion
					put("drawn", "1_211");       // Drawn to Its Power: [ringwraith]
					put("pinned", "1_192");      // Pinned Down: [moria]; the one kept in the discard pile

					// Cards in the draw deck that are not Shadow conditions.
					put("heart", "1_10");        // Dwarven Heart: a Free Peoples condition
					put("scimitar", "1_180");    // Goblin Scimitar: a Shadow possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValiantbutFoolishStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Valiant but Foolish
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		 * Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (6) to take up to 4 Shadow
		 * conditions into hand from your draw deck. If in region 1, this action can be performed from your
		 * draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("valiant1");

		assertEquals("Valiant but Foolish", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrawActionNeedsAMinionToSpotAndRemovesThreeToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var troop = scn.GetShadowCard("troop");
		var business = scn.GetShadowCard("business");
		scn.MoveCardsToHand(valiant1, troop);
		// A Shadow card on the table that is no minion.
		scn.MoveCardsToSupportArea(business);

		scn.StartGame();
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 2 for the site: one short of the 18 the other action spots, and no minion.
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.HAND, valiant1);
		assertFalse(scn.ShadowPlayAvailable(valiant1));
		// Nor is the copy in the draw deck on offer, in region 1 though this is.
		assertInZone(Zone.DECK, valiant2);
		assertFalse(scn.ShadowActionAvailable(valiant2));

		// 6 to play + 2 roaming: the Troop's site number is 4 and the fellowship is at site 2.
		scn.ShadowPlayCard(troop);
		assertEquals(17 - 8, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(valiant1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(valiant1);
		assertEquals(9 - 3, scn.GetTwilight());
		// The event leaves the hand and two cards come into it.
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, valiant1);
		// Drawing looks through nothing.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawActionIsOfferedWithExactlyThreeTwilightTokensAndNotWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var threatening = scn.GetShadowCard("threatening");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var drawn = scn.GetShadowCard("drawn");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(valiant1, valiant2, threatening);
		scn.MoveCardsToTopOfDeck(armory, drawn);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 5 set + 1 companion + 0 for the site.
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(valiant1);
		assertEquals(6 - 3, scn.GetTwilight());
		assertInZone(Zone.HAND, armory, drawn);
		// Exactly three left: the second copy is still on offer.
		assertInZone(Zone.HAND, valiant2);
		assertTrue(scn.ShadowPlayAvailable(valiant2));

		// A condition costing 1 leaves two, with the Savage still there: one short.
		scn.ShadowPlayCard(threatening);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowPlayAvailable(valiant2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSeventeenTwilightTokensInRegion1OnlyTheDrawIsPerformedAndNotFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var drawn = scn.GetShadowCard("drawn");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(valiant1);
		scn.MoveCardsToTopOfDeck(valiant2, armory, drawn);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(16);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 16 set + 1 companion + 0 for the site.
		assertEquals(17, scn.GetTwilight());
		// A minion and 3 tokens are enough for the draw, and the draw is not performed from the draw deck.
		assertInZone(Zone.DECK, valiant2);
		assertFalse(scn.ShadowActionAvailable(valiant2));
		assertTrue(scn.ShadowPlayAvailable(valiant1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// One token short of 18, there is no choosing between the two actions: this is the draw.
		scn.ShadowPlayCard(valiant1);
		assertEquals(17 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, valiant1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEighteenTwilightTokensAtSite6RemovesSixToTakeUpToFourShadowConditionsFromDrawDeckIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");
		var armory = scn.GetShadowCard("armory");
		var evil = scn.GetShadowCard("evil");
		var drawn = scn.GetShadowCard("drawn");
		var pinned = scn.GetShadowCard("pinned");
		var heart = scn.GetShadowCard("heart");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(5);
		// The turns played on the way drew cards: put every one where this test wants it.
		scn.MoveCardsToTopOfDeck(valiant2, troop, savage, business, threatening, armory, evil, drawn, heart, scimitar);
		// A Shadow condition that is not in the draw deck.
		scn.MoveCardsToDiscard(pinned);
		scn.MoveCardsToHand(valiant1);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 6 is the last site of region 2.
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 3 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(10, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowPlayAvailable(valiant1));
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(valiant1);
		assertEquals(18 - 6, scn.GetTwilight());

		// The deck is shown, and then the choice: Shadow conditions of any culture.  Not the Free Peoples
		// condition, nor a Shadow possession, minion or event, nor the Shadow condition in the discard pile.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(business, threatening, armory, evil, drawn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(heart, scimitar, troop, savage, valiant2, pinned));
		assertEquals(0, scn.ShadowGetChoiceMin());
		// Five could be taken, and four may be.
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(business, threatening, evil, drawn);
		assertInZone(Zone.HAND, business, threatening, evil, drawn);
		assertInZone(Zone.DECK, armory, heart, scimitar, troop, savage, valiant2);
		assertInZone(Zone.DISCARD, pinned);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(10 - 4, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, valiant1);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEighteenTwilightTokensAtSite2AndNoMinionItIsPlayedFromHandAndTheCopyInTheDrawDeckIsOnOfferToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");
		var armory = scn.GetShadowCard("armory");
		var evil = scn.GetShadowCard("evil");
		var drawn = scn.GetShadowCard("drawn");
		var heart = scn.GetShadowCard("heart");
		scn.MoveCardsToHand(valiant1);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 15 set + 1 companion + 2 for the site.  No minion is in play.
		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());
		// In region 1 with 18 tokens the copy in hand can be played and the copy in the draw deck performed.
		assertTrue(scn.ShadowPlayAvailable(valiant1));
		assertInZone(Zone.DECK, valiant2);
		assertTrue(scn.ShadowActionAvailable(valiant2));
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(valiant1);
		assertEquals(18 - 6, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(business, threatening, armory, evil, drawn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(heart, valiant2));
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(business, armory, evil, drawn);
		assertInZone(Zone.HAND, business, armory, evil, drawn);
		assertInZone(Zone.DECK, threatening, heart);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(11 - 4, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, valiant1);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());

		// Twelve tokens are left: the copy in the draw deck needs its 18 as well.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DECK, valiant2);
		assertFalse(scn.ShadowActionAvailable(valiant2));
	}

	@Test
	public void WithBothActionsOnOfferAtSite2ThePlayerMayChooseTheConditionsAndThenTakeNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant = scn.GetShadowCard("valiant1");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var drawn = scn.GetShadowCard("drawn");
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 15 set + 1 companion + 2 for the site.
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(valiant));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// A minion and 18 tokens: both actions are on offer, and the player says which.
		scn.ShadowPlayCard(valiant);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("draw 2 cards"));
		assertTrue(scn.ShadowChoiceAvailable("take up to 4 conditions"));

		scn.ShadowChooseOption("take up to 4 conditions");
		assertEquals(18 - 6, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armory, drawn));
		assertEquals(0, scn.ShadowGetChoiceMin());

		// Up to four is none, if the player likes.  The deck was looked through all the same.
		scn.ShadowDeclineChoosing();
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, armory, drawn);
		assertInZone(Zone.DISCARD, valiant);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3ItMayBePerformedFromTheDrawDeckAndThenOnlyTakesConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var business = scn.GetShadowCard("business");
		var threatening = scn.GetShadowCard("threatening");
		var armory = scn.GetShadowCard("armory");
		var evil = scn.GetShadowCard("evil");
		var drawn = scn.GetShadowCard("drawn");
		var pinned = scn.GetShadowCard("pinned");
		var heart = scn.GetShadowCard("heart");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(2);
		// Both copies in the draw deck and nothing in hand.
		scn.MoveCardsToTopOfDeck(valiant1, valiant2, troop, business, threatening, armory, evil, drawn, heart, scimitar);
		scn.MoveCardsToDiscard(pinned);
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
		assertInZone(Zone.DECK, valiant1, valiant2);
		assertTrue(scn.ShadowActionAvailable(valiant1));
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No question of which action: from the draw deck it is the taking of conditions.
		scn.ShadowUseCardAction(valiant1);
		assertEquals(18 - 6, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(business, threatening, armory, evil, drawn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(heart, scimitar, troop, valiant2, pinned));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(threatening, armory, evil, drawn);
		assertInZone(Zone.HAND, threatening, armory, evil, drawn);
		assertInZone(Zone.DECK, business, valiant2);
		assertEquals(4, scn.GetShadowHandCount());
		// The event is played and gone, like one played from hand.
		assertInZone(Zone.DISCARD, valiant1);
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4ItIsNotPerformedFromTheDrawDeckAndFromHandThePlayerMayChooseTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var drawn = scn.GetShadowCard("drawn");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(valiant1);
		scn.MoveCardsToTopOfDeck(valiant2, armory, drawn);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 4 is the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 14 set + 1 companion + 3 for the site.
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, valiant2);
		assertFalse(scn.ShadowActionAvailable(valiant2));
		assertTrue(scn.ShadowPlayAvailable(valiant1));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// A minion and 18 tokens: both actions are on offer, and this time the draw is chosen.
		scn.ShadowPlayCard(valiant1);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("draw 2 cards");
		assertEquals(18 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, valiant1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndNoMinionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant = scn.GetShadowCard("valiant1");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(valiant);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 17 set + 1 companion + 6 for the site: more than 18, and one short of 25.  No minion is in play.
		assertEquals(24, scn.GetTwilight());
		assertInZone(Zone.HAND, valiant);
		assertFalse(scn.ShadowPlayAvailable(valiant));
	}

	@Test
	public void AtSite7WithTwentyFourTwilightTokensAndAMinionOnlyTheDrawIsPerformed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant = scn.GetShadowCard("valiant1");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var evil = scn.GetShadowCard("evil");
		var drawn = scn.GetShadowCard("drawn");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(valiant);
		scn.MoveCardsToTopOfDeck(armory, evil, drawn);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Site 7 is the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 17 set + 1 companion + 6 for the site.
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(valiant));
		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		var shuffles = scn.GetShadowDeckShuffleCount();

		// 24 is more than 18 and one short of 25: with a minion to spot the event is played, and it draws.
		scn.ShadowPlayCard(valiant);
		assertEquals(24 - 3, scn.GetTwilight());
		assertEquals(hand - 1 + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, valiant);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7TwentyFiveTwilightTokensAreEnoughForTheConditionsAndItIsNotPerformedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant1 = scn.GetShadowCard("valiant1");
		var valiant2 = scn.GetShadowCard("valiant2");
		var armory = scn.GetShadowCard("armory");
		var evil = scn.GetShadowCard("evil");
		var heart = scn.GetShadowCard("heart");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(valiant1);
		scn.MoveCardsToTopOfDeck(valiant2, armory, evil, heart);
		scn.SetTwilight(18);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 18 set + 1 companion + 6 for the site.  No minion is in play.
		assertEquals(25, scn.GetTwilight());
		assertInZone(Zone.DECK, valiant2);
		assertFalse(scn.ShadowActionAvailable(valiant2));
		assertTrue(scn.ShadowPlayAvailable(valiant1));
		var shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(valiant1);
		assertEquals(25 - 6, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armory, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(heart, valiant2));
		scn.ShadowChooseCardBPFromSelection(armory, evil);
		assertInZone(Zone.HAND, armory, evil);
		assertInZone(Zone.DISCARD, valiant1);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
