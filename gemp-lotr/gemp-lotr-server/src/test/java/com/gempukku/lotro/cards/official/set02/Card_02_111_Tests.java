package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("invited", "2_111");
					put("retired", "1_284");    // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3; a fellowship ability used only on purpose
					put("melancholy", "12_119"); // Bilbo, Melancholy Hobbit: [shire] Hobbit companion; a start-of-skirmish trigger only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, not unique; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] companion Pippin needs; no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; to play, add a burden
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a Hobbit minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PracticallyEveryoneWasInvitedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Practically Everyone Was Invited
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert Bilbo to play a Hobbit from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("invited");

		assertEquals("Practically Everyone Was Invited", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheBilboWhoIsAnAllyToPlayAHobbitOfAnyCultureFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invited = scn.GetFreepsCard("invited");
		var retired = scn.GetFreepsCard("retired");
		var melancholy = scn.GetFreepsCard("melancholy");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var ted = scn.GetFreepsCard("ted");
		scn.MoveAlliesToTable(retired);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(invited);

		scn.StartGame();

		assertInZone(Zone.DECK, pippin, bounder, smeagol, ted, melancholy);
		assertInZone(Zone.DECK, scn.GetShadowCard("pippin"), scn.GetShadowCard("bounder"));
		assertEquals(0, scn.GetWoundsOn(retired));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsPlayAvailable(invited));

		scn.FreepsPlayCard(invited);
		// The one Bilbo exerts, with nobody asked.
		assertEquals(1, scn.GetWoundsOn(retired));

		// His own draw deck is shown to him.
		assertEquals(scn.GetFreepsDeckCount(), scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// A Hobbit companion of [gondor] and a Hobbit ally of [shire] may be played.  The [shire] companion who
		// is no Hobbit may not, nor the Hobbit minion, nor the second Bilbo while one is in play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(melancholy));
		// A play from a draw deck may always find nothing (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(pippin);

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertInZone(Zone.DECK, bounder, smeagol, ted, melancholy);
		assertInZone(Zone.DISCARD, invited);
		// Shuffled as Pippin leaves the deck that was looked through, and again once the look is over.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		// The Shadow player's deck was not looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBilboWhoIsACompanionPaysTooAndAHobbitAllyGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invited = scn.GetFreepsCard("invited");
		var melancholy = scn.GetFreepsCard("melancholy");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(melancholy);
		scn.MoveCardsToHand(invited);

		scn.StartGame();

		assertInZone(Zone.DECK, bounder);
		assertEquals(0, scn.GetWoundsOn(melancholy));

		scn.FreepsPlayCard(invited);
		assertEquals(1, scn.GetWoundsOn(melancholy));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(bounder);

		assertInZone(Zone.SUPPORT, bounder);
		assertInZone(Zone.DISCARD, invited);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBilboExhaustedItCannotBePlayedThoughAnotherShireAllyCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var invited = scn.GetFreepsCard("invited");
		var retired = scn.GetFreepsCard("retired");
		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveAlliesToTable(retired, bounder);
		// Aragorn lets Pippin, in the deck, be played.
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(invited);

		scn.StartGame();
		// Bilbo at one vitality; Bounder, a [shire] Hobbit ally, and Frodo could still exert.
		scn.AddWoundsToChar(retired, 2);
		assertEquals(1, scn.GetVitality(retired));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DECK, pippin);
		// The wounds went on under the pending choice of fellowship actions.
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsPlayAvailable(invited));
		assertInZone(Zone.HAND, invited);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHobbitInTheDrawDeckItIsStillPlayedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invited = scn.GetFreepsCard("invited");
		var melancholy = scn.GetFreepsCard("melancholy");
		var retired = scn.GetFreepsCard("retired");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var ted = scn.GetFreepsCard("ted");
		scn.MoveCompanionsToTable(melancholy);
		scn.MoveCardsToHand(invited);

		scn.StartGame();
		// Every Hobbit of his but Bilbo is in his discard pile; the deck holds no Hobbit.
		scn.MoveCardsToDiscard(retired, pippin, bounder);

		assertInZone(Zone.DECK, smeagol, ted);
		assertInZone(Zone.DISCARD, retired, pippin, bounder);
		assertEquals(0, scn.GetWoundsOn(melancholy));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the event may be played for a look.
		assertTrue(scn.FreepsPlayAvailable(invited));
		scn.FreepsPlayCard(invited);
		assertEquals(1, scn.GetWoundsOn(melancholy));

		// The deck is shown, with nothing in it that may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, smeagol, ted);
		assertInZone(Zone.DISCARD, retired, pippin, bounder, invited);
		// Nothing left the deck: one shuffle, once the look is over.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
