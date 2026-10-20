package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_258_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skulker1", "1_258");
					put("skulker2", "1_258");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, cost 2; text only on a weapon played on him

					// [wraith] cards of four types for the discard pile.  None is in play here, so their texts do nothing.
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc minion
					put("breath", "1_207");     // Black Breath: a [wraith] condition
					put("steed", "1_208");      // Black Steed: a [wraith] possession (a mount)
					put("frozen", "1_213");     // Frozen by Fear: a [wraith] Skirmish event

					// Cards that are nearly [wraith] cards in the discard pile, and [wraith] cards elsewhere.
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a Nazgul of [raider]
					put("fear", "1_212");       // Fear: a [wraith] Maneuver event; stays in hand
					put("servants", "1_219");   // The Nine Servants of Sauron: a [wraith] support-area condition; answers only a Nazgul played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSkulkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Morgul Skulker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: When you play this minion, you may shuffle up to 2 [wraith] cards from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("skulker1");

		assertEquals("Morgul Skulker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItsOfferMayBeDeclinedAndNothingIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skulker = scn.GetShadowCard("skulker1");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToHand(skulker);
		scn.MoveCardsToDiscard(breath);
		assertInDiscard(breath);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(skulker);

		assertInZone(Zone.SHADOW_CHARACTERS, skulker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skulker));

		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(breath);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedItMayShuffleTwoWraithCardsFromTheDiscardPileIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skulker = scn.GetShadowCard("skulker1");
		var discardedSkulker = scn.GetShadowCard("skulker2");
		var hound = scn.GetShadowCard("hound");
		var breath = scn.GetShadowCard("breath");
		var steed = scn.GetShadowCard("steed");
		var frozen = scn.GetShadowCard("frozen");
		var khamul = scn.GetShadowCard("khamul");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		// A [wraith] card in play and one in hand.
		scn.MoveCardsToSupportArea(servants);
		scn.MoveCardsToHand(skulker, fear);
		// A [wraith] minion, condition, possession and event; a [sauron] Orc and a Nazgul of [raider].
		scn.MoveCardsToDiscard(hound, breath, steed, frozen, discardedSkulker, khamul);
		assertInDiscard(hound, breath, steed, frozen, discardedSkulker, khamul);
		assertInZone(Zone.SUPPORT, servants);
		assertInZone(Zone.HAND, fear);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(skulker);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skulker));

		scn.ShadowChooseOptionalTrigger(skulker);

		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, breath, steed, frozen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(discardedSkulker, khamul, fear, servants));
		assertEquals(0, scn.ShadowGetChoiceMin());
		// Four could be taken; two is the most.
		assertEquals(2, scn.ShadowGetChoiceMax());
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentsDeck = scn.GetFreepsDeckCount();
		int opponentsShuffles = scn.GetFreepsDeckShuffleCount();

		// The minion and the possession
		scn.ShadowChooseCards(hound, steed);

		assertInZone(Zone.DECK, hound);
		assertInZone(Zone.DECK, steed);
		assertInDiscard(breath, frozen, discardedSkulker, khamul);
		assertEquals(deck + 2, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentsDeck, scn.GetFreepsDeckCount());
		assertEquals(opponentsShuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.SUPPORT, servants);
		assertInZone(Zone.HAND, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OneCardMayBeTakenOrNoneAndTakingNoneStillShufflesTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skulker1 = scn.GetShadowCard("skulker1");
		var skulker2 = scn.GetShadowCard("skulker2");
		var breath = scn.GetShadowCard("breath");
		var steed = scn.GetShadowCard("steed");
		var frozen = scn.GetShadowCard("frozen");
		scn.MoveCardsToHand(skulker1, skulker2);
		scn.MoveCardsToDiscard(breath, steed, frozen);
		assertInDiscard(breath, steed, frozen);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(skulker1);
		scn.ShadowChooseOptionalTrigger(skulker1);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		// One of the three
		scn.ShadowChooseCards(frozen);

		assertInZone(Zone.DECK, frozen);
		assertInDiscard(breath, steed);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The offer taken and then no card chosen: nothing goes into the draw deck, and the deck is shuffled
		// all the same.
		int opponentsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.ShadowPlayCard(skulker2);
		scn.ShadowChooseOptionalTrigger(skulker2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(breath, steed));
		scn.ShadowDeclineChoosing();

		assertInDiscard(breath, steed);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentsShuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.SHADOW_CHARACTERS, skulker2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWraithCardInTheDiscardPileTheOfferIsStillMadeAndTakingItShufflesTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skulker = scn.GetShadowCard("skulker1");
		var discardedSkulker = scn.GetShadowCard("skulker2");
		var khamul = scn.GetShadowCard("khamul");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		// A [wraith] card in play and one in hand; in the discard pile a [sauron] Orc and a Nazgul of [raider].
		scn.MoveCardsToSupportArea(servants);
		scn.MoveCardsToHand(skulker, fear);
		scn.MoveCardsToDiscard(discardedSkulker, khamul);
		assertInDiscard(discardedSkulker, khamul);
		assertInZone(Zone.SUPPORT, servants);
		assertInZone(Zone.HAND, fear);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(skulker);

		assertInZone(Zone.SHADOW_CHARACTERS, skulker);
		// There is nothing to shuffle in, and the offer does something all the same: it shuffles the draw deck.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skulker));

		scn.ShadowChooseOptionalTrigger(skulker);

		assertInDiscard(discardedSkulker, khamul);
		assertInZone(Zone.SUPPORT, servants);
		assertInZone(Zone.HAND, fear);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherSauronOrcPlayedMakesNoOfferAndASecondSkulkersOfferIsItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skulker = scn.GetShadowCard("skulker1");
		var earlierSkulker = scn.GetShadowCard("skulker2");
		var chieftain = scn.GetShadowCard("chieftain");
		var breath = scn.GetShadowCard("breath");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToHand(skulker, chieftain);
		scn.MoveCardsToDiscard(breath, steed);
		assertInDiscard(breath, steed);

		scn.StartGame();
		scn.MoveMinionsToTable(earlierSkulker);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int shuffles = scn.GetShadowDeckShuffleCount();

		// Another [sauron] Orc is played while a Skulker is in play.
		scn.ShadowPlayCard(chieftain);

		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(breath, steed);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Skulker is played: the copy played makes the offer and the copy in play makes none.
		scn.ShadowPlayCard(skulker);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skulker));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(earlierSkulker));

		scn.ShadowChooseOptionalTrigger(skulker);
		assertTrue(scn.ShadowHasCardChoiceAvailable(breath, steed));
		scn.ShadowChooseCards(breath);

		assertInZone(Zone.DECK, breath);
		assertInDiscard(steed);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
