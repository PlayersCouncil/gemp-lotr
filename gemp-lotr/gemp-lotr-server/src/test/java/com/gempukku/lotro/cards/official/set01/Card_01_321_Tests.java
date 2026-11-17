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

public class Card_01_321_Tests
{

	// Farmer Maggot's Fields is site 1; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion; a standing bonus with a weapon only
					put("holbytla", "103_85");  // Merry, Master Holbytla: [rohan] Hobbit companion; his trigger answers only a [rohan] Man exerting
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit companion; a bar on discarding tales only
					put("trollslayer", "103_31"); // Pippin, Trollslayer: [gondor] Hobbit companion; to play, spot a [gondor] companion
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion of another title; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion who is no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_321");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarmerMaggotsFieldsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Farmer Maggot's Fields
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert a Hobbit to play Merry or Pippin from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Farmer Maggot's Fields", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionExertsAHobbitToPlayAMerryFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var holbytla = scn.GetFreepsCard("holbytla");
		var sam = scn.GetFreepsCard("sam");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		// Pippin, Trollslayer is the Hobbit of another culture who exerts; a Merry is the one played here.
		scn.MoveCompanionsToTable(trollslayer, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		var fields = scn.GetCurrentSite();
		assertEquals("Farmer Maggot's Fields", fields.getBlueprint().getTitle());
		// Each player's two Merrys and his Sam lie in his own draw deck.
		assertInZone(Zone.DECK, merry, holbytla, sam);
		assertInZone(Zone.DECK, scn.GetShadowCard("merry"), scn.GetShadowCard("holbytla"));
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(fields));

		scn.FreepsUseCardAction(fields);

		// A Hobbit of either culture, companion or ally, may exert; the companions who are no Hobbits may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, trollslayer, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(trollslayer);
		assertEquals(1, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// His own draw deck is shown to him, and no other.
		assertEquals(scn.GetFreepsDeckCount(), scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The [shire] Merry and the [rohan] Merry; the Hobbit of another title is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		assertTrue(scn.FreepsHasCardChoiceAvailable(holbytla));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		// Looking through a draw deck never forces a card to be taken (F-0049).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(holbytla);

		assertInZone(Zone.FREE_CHARACTERS, holbytla);
		assertInZone(Zone.DECK, merry, sam);
		// One shuffle as the card leaves the deck that was looked through, one when the look is over.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		// The Shadow player's deck was not looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EitherMerryAndEitherPippinIsOfferedAndThePippinOfGondorIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var holbytla = scn.GetFreepsCard("holbytla");
		var pippin = scn.GetFreepsCard("pippin");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Aragorn is the [gondor] companion Pippin, Trollslayer needs to be played.
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		var fields = scn.GetCurrentSite();
		assertInZone(Zone.DECK, merry, holbytla, pippin, trollslayer, sam);
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(fields);
		// Frodo is the only Hobbit, and exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDismissRevealedCards();

		// Both Merrys and both Pippins, of three cultures; not Sam.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		assertTrue(scn.FreepsHasCardChoiceAvailable(holbytla));
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(trollslayer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCardBPFromSelection(trollslayer);

		assertInZone(Zone.FREE_CHARACTERS, trollslayer);
		assertInZone(Zone.DECK, merry, holbytla, pippin, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryHobbitExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(trollslayer, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Every Hobbit at one vitality; Aragorn and Smeagol, who are no Hobbits, could still exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(trollslayer, 3);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(trollslayer));
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.DECK, merry);
		// The wounds went on under the pending choice of fellowship actions.
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoMerryOrPippinInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var holbytla = scn.GetFreepsCard("holbytla");
		var pippin = scn.GetFreepsCard("pippin");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var sam = scn.GetFreepsCard("sam");

		scn.StartGame();
		scn.MoveCardsToDiscard(merry, holbytla, pippin, trollslayer);
		scn.FreepsRefreshDecision();

		var fields = scn.GetCurrentSite();
		assertInZone(Zone.DISCARD, merry, holbytla, pippin, trollslayer);
		assertEquals(0, scn.GetWoundsOn(frodo));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(fields));
		scn.FreepsUseCardAction(fields);
		// Frodo is the only Hobbit, and exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The deck is shown, with nothing in it that may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, sam);
		assertInZone(Zone.DISCARD, merry, holbytla, pippin, trollslayer);
		// The one shuffle when the look is over.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
