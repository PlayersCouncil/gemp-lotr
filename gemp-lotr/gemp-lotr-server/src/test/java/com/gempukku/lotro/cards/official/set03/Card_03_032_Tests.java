package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fireworks", "3_32");
					put("fireworks2", "3_32");  // a second copy, a [gandalf] event for the discard pile
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("walkers", "1_79");     // The Nine Walkers: a [gandalf] condition
					put("bounder", "1_286");    // Bounder: a [shire] ally
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("stealth", "1_298");    // Hobbit Stealth: a [shire] event
					put("enmity", "1_29");      // Ancient Enmity: an [elven] event, of neither culture
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FireworksStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Fireworks
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> Spot Gandalf to shuffle up to 2 [gandalf] or up to 2 [shire] cards from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var fireworks = scn.GetFreepsCard("fireworks");

		assertEquals("Fireworks", fireworks.getBlueprint().getTitle());
		assertNull(fireworks.getBlueprint().getSubtitle());
		assertFalse(fireworks.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, fireworks.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, fireworks.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fireworks.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(fireworks, Keyword.SPELL));
		assertTrue(scn.HasTimeword(fireworks, Timeword.FELLOWSHIP));
		assertEquals(1, fireworks.getBlueprint().getTwilightCost());
	}

	@Test
	public void ChoosingGandalfShufflesUpToTwoGandalfCardsOfAnyTypeIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fireworks = scn.GetFreepsCard("fireworks");
		var fireworks2 = scn.GetFreepsCard("fireworks2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var walkers = scn.GetFreepsCard("walkers");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var stealth = scn.GetFreepsCard("stealth");
		var enmity = scn.GetFreepsCard("enmity");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fireworks);
		scn.MoveCardsToDiscard(fireworks2, avenger, walkers, bounder, sword, stealth, enmity);
		assertInHand(fireworks);
		assertInDiscard(fireworks2, avenger, walkers, bounder, sword, stealth, enmity);

		scn.StartGame();
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(fireworks));

		scn.FreepsPlayCard(fireworks);
		// Cards of both cultures lie in the pile: the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("GANDALF");

		// A companion, a condition and an event of [gandalf]; the copy being played is not in the pile yet.
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, walkers, fireworks2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, sword, stealth, enmity));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(avenger, walkers);

		assertInZone(Zone.DECK, avenger, walkers);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInDiscard(fireworks, fireworks2, bounder, sword, stealth, enmity);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChoosingShireShufflesUpToTwoShireCardsOfAnyTypeIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fireworks = scn.GetFreepsCard("fireworks");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var walkers = scn.GetFreepsCard("walkers");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var stealth = scn.GetFreepsCard("stealth");
		var enmity = scn.GetFreepsCard("enmity");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fireworks);
		scn.MoveCardsToDiscard(avenger, walkers, bounder, sword, stealth, enmity);
		assertInHand(fireworks);
		assertInDiscard(avenger, walkers, bounder, sword, stealth, enmity);

		scn.StartGame();
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(fireworks);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("SHIRE");

		// An ally, a possession and an event of [shire]; nothing of [gandalf] or [elven].
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder, sword, stealth));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, walkers, enmity));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(bounder, stealth);

		assertInZone(Zone.DECK, bounder, stealth);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(fireworks, avenger, walkers, sword, enmity);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNeitherCultureInTheDiscardPileItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fireworks = scn.GetFreepsCard("fireworks");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enmity = scn.GetFreepsCard("enmity");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fireworks);
		scn.MoveCardsToDiscard(enmity);
		assertInHand(fireworks);
		assertInDiscard(enmity);

		scn.StartGame();
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// An event's target is no requirement: it is played all the same.
		assertTrue(scn.FreepsPlayAvailable(fireworks));

		scn.FreepsPlayCard(fireworks);
		// Each branch still shuffles the deck (Q-0102), so the choice between them is asked; no card is then asked for.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("GANDALF");
		assertInDiscard(fireworks, enmity);
		assertEquals(deck, scn.GetFreepsDeckCount());
		// "Shuffle up to 2 ... into your draw deck" shuffles the deck with nothing shuffled in (Q-0102).
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfToSpotItIsNotPlayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fireworks = scn.GetFreepsCard("fireworks");
		var avenger = scn.GetFreepsCard("avenger");
		var walkers = scn.GetFreepsCard("walkers");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(fireworks);
		scn.MoveCardsToDiscard(walkers, bounder);
		assertInHand(fireworks);
		assertInDiscard(walkers, bounder);

		scn.StartGame();
		// The Ent Avenger is a [gandalf] companion, and no Gandalf.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fireworks));
		assertInHand(fireworks);
	}
}
