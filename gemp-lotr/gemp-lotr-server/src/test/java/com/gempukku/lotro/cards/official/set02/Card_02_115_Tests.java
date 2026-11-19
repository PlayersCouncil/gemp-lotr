package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_115_Tests
{

	// Hobbiton Party Field is site 1; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("retired", "1_284");    // Bilbo, Retired Adventurer: [shire] Hobbit ally, home 3F; a fellowship ability used only on purpose
					put("melancholy", "12_119"); // Bilbo, Melancholy Hobbit: [shire] Hobbit companion; a start-of-skirmish trigger only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] Hobbit companion who is no Bilbo
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_115");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitonPartyFieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Hobbiton Party Field
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert a Hobbit to play Bilbo from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Hobbiton Party Field", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionExertsAHobbitToPlayBilboFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var retired = scn.GetFreepsCard("retired");
		var melancholy = scn.GetFreepsCard("melancholy");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		var field = scn.GetCurrentSite();
		assertEquals("Hobbiton Party Field", field.getBlueprint().getTitle());
		// Each player's two Bilbos and his Merry lie in his own draw deck.
		assertInZone(Zone.DECK, retired, melancholy, merry);
		assertInZone(Zone.DECK, scn.GetShadowCard("retired"), scn.GetShadowCard("melancholy"));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(field));

		scn.FreepsUseCardAction(field);

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
		scn.FreepsDismissRevealedCards();

		// The Bilbo who is an ally and the Bilbo who is a companion; the Hobbit who is no Bilbo is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(retired));
		assertTrue(scn.FreepsHasCardChoiceAvailable(melancholy));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		// Looking through a draw deck never forces a card to be taken (F-0049).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(retired);

		// An ally goes to the support area.
		assertInZone(Zone.SUPPORT, retired);
		assertInZone(Zone.DECK, melancholy, merry);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		// The Shadow player's deck was not looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBilboWhoIsACompanionIsPlayedIntoTheFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var retired = scn.GetFreepsCard("retired");
		var melancholy = scn.GetFreepsCard("melancholy");

		scn.StartGame();

		var field = scn.GetCurrentSite();
		assertInZone(Zone.DECK, retired, melancholy);
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(field);
		// Frodo is the only Hobbit, and exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(melancholy);

		assertInZone(Zone.FREE_CHARACTERS, melancholy);
		assertInZone(Zone.DECK, retired);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryHobbitExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var retired = scn.GetFreepsCard("retired");
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
		assertInZone(Zone.DECK, retired);
		// The wounds went on under the pending choice of fellowship actions.
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBilboInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var retired = scn.GetFreepsCard("retired");
		var melancholy = scn.GetFreepsCard("melancholy");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.MoveCardsToDiscard(retired, melancholy);
		scn.FreepsRefreshDecision();

		var field = scn.GetCurrentSite();
		assertEquals(0, scn.GetWoundsOn(frodo));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(field));
		scn.FreepsUseCardAction(field);
		// Frodo is the only Hobbit, and exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The deck is shown, with nothing in it that may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, merry);
		assertInZone(Zone.DISCARD, retired, melancholy);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
