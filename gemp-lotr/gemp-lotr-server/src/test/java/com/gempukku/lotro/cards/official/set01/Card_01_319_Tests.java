package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_319_Tests
{

	// Bag End is site 1 of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gaffer", "1_291");     // The Gaffer, Sam's Father: [shire] Hobbit ally, home 1F; a fellowship ability used only on purpose
					put("gardener", "13_151");  // The Gaffer, Master Gardener: [shire] follower; Aid and a transfer trigger, neither reached
					put("pipe", "1_292");       // The Gaffer's Pipe: [shire] pipe a Hobbit may bear; a title that only begins with the name
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally of another title; a fellowship ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BagEndStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bag End
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert a Hobbit to play The Gaffer from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Bag End", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionExertsAHobbitToPlayTheGafferFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var gardener = scn.GetFreepsCard("gardener");
		var pipe = scn.GetFreepsCard("pipe");
		var bilbo = scn.GetFreepsCard("bilbo");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		var bagEnd = scn.GetCurrentSite();
		assertEquals("Bag End", bagEnd.getBlueprint().getTitle());
		// Each player's two Gaffers lie in his own draw deck, with the Pipe and Bilbo.
		assertInZone(Zone.DECK, gaffer, gardener, pipe, bilbo);
		assertInZone(Zone.DECK, scn.GetShadowCard("gaffer"), scn.GetShadowCard("gardener"));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(bagEnd));

		scn.FreepsUseCardAction(bagEnd);

		// A Hobbit of either culture, companion or ally, may exert; the companions who are no Hobbits may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// His own draw deck is shown to him, and no other.
		assertEquals(scn.GetFreepsDeckCount(), scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The Gaffer who is an ally and The Gaffer who is a follower; neither the Pipe, whose title only begins
		// with the name, nor a Hobbit ally of another title, though Frodo could bear the one and Bilbo be played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gaffer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gardener));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, bilbo));
		// Looking through a draw deck never forces a card to be taken (F-0049).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(gaffer);

		// An ally goes to the support area.
		assertInZone(Zone.SUPPORT, gaffer);
		assertInZone(Zone.DECK, gardener, pipe, bilbo);
		// One shuffle as the card leaves the deck that was looked through, one when the look is over.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		// The Shadow player's deck was not looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheGafferWhoIsAFollowerIsPlayedToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var gardener = scn.GetFreepsCard("gardener");

		scn.StartGame();

		var bagEnd = scn.GetCurrentSite();
		assertInZone(Zone.DECK, gaffer, gardener);
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(bagEnd);
		// Frodo is the only Hobbit, and exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gardener);

		assertInZone(Zone.SUPPORT, gardener);
		assertInZone(Zone.DECK, gaffer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryHobbitExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Every Hobbit at one vitality; Aragorn and Smeagol, who are no Hobbits, could still exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(pippin, 3);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(pippin));
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.DECK, gaffer);
		// The wounds went on under the pending choice of fellowship actions.
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGafferInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var gardener = scn.GetFreepsCard("gardener");
		var pipe = scn.GetFreepsCard("pipe");

		scn.StartGame();
		scn.MoveCardsToDiscard(gaffer, gardener);
		scn.FreepsRefreshDecision();

		var bagEnd = scn.GetCurrentSite();
		assertInZone(Zone.DISCARD, gaffer, gardener);
		assertEquals(0, scn.GetWoundsOn(frodo));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(bagEnd));
		scn.FreepsUseCardAction(bagEnd);
		// Frodo is the only Hobbit, and exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The deck is shown, with nothing in it that may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, pipe);
		assertInZone(Zone.DISCARD, gaffer, gardener);
		// The one shuffle when the look is over.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
