package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "2_104");
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: [shire] Hobbit ally, home 1F; a fellowship ability never used
					put("marish", "11_163");    // Farmer Maggot, Hobbit of the Marish: [shire] Hobbit companion; a standing bonus at a dwelling or forest
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally of another title; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Merry, Horticulturalist
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Fellowship:</b> Exert Merry to play Farmer Maggot from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Horticulturalist", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipActionExertsMerryToPlayTheFarmerMaggotAllyFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var maggot = scn.GetFreepsCard("maggot");
		var marish = scn.GetFreepsCard("marish");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		// Each player's two Farmer Maggots and his Bounder lie in his own draw deck.
		assertInZone(Zone.DECK, maggot, marish, bounder);
		assertInZone(Zone.DECK, scn.GetShadowCard("maggot"), scn.GetShadowCard("marish"));
		assertEquals(0, scn.GetWoundsOn(merry));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// His own draw deck is shown to him, and no other.
		assertEquals(scn.GetFreepsDeckCount(), scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The Farmer Maggot who is an ally and the one who is a companion; a Hobbit ally of another title is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(maggot));
		assertTrue(scn.FreepsHasCardChoiceAvailable(marish));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		// Looking through a draw deck never forces a card to be taken (F-0049; Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(maggot);

		assertInZone(Zone.SUPPORT, maggot);
		assertInZone(Zone.DECK, marish, bounder);
		// One shuffle as the card leaves the deck that was looked through, one when the look is over.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		// The Shadow player's deck was not looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFarmerMaggotWhoIsACompanionIsPlayedAmongTheCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var maggot = scn.GetFreepsCard("maggot");
		var marish = scn.GetFreepsCard("marish");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		assertInZone(Zone.DECK, maggot, marish);
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(marish);

		assertInZone(Zone.FREE_CHARACTERS, marish);
		assertInZone(Zone.DECK, maggot);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileMerryIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 3);
		// The wounds went on under the pending choice of fellowship actions.
		scn.FreepsRefreshDecision();

		// Vitality 4 with 3 wounds: he cannot exert.
		assertEquals(1, scn.GetVitality(merry));
		assertInZone(Zone.DECK, maggot);
		assertFalse(scn.FreepsActionAvailable(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFarmerMaggotInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var maggot = scn.GetFreepsCard("maggot");
		var marish = scn.GetFreepsCard("marish");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveCardsToDiscard(maggot, marish);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DISCARD, maggot, marish);
		assertInZone(Zone.DECK, bounder);
		assertEquals(0, scn.GetWoundsOn(merry));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// The deck is shown, with nothing in it that may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, bounder);
		assertInZone(Zone.DISCARD, maggot, marish);
		// The one shuffle when the look is over.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
