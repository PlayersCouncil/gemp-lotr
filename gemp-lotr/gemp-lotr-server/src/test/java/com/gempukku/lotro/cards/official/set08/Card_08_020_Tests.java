package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fire", "8_20");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, an ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion, in the deck
					put("armor", "1_92");       // Armor: [gondor] possession, in the deck
					put("swordarm", "1_116");   // Swordarm of the White Tower: [gondor] event, in the deck
					put("mail", "1_101");       // Coat of Mail: [gondor] possession, in the deck
					put("shield", "1_107");     // Great Shield: [gondor] possession, put in the discard pile
					put("caradhras", "1_84");   // Sleep, Caradhras: [gandalf] event, in the deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavedFromtheFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Saved From the Fire
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take up to 3 cards
		 * 	from that companion's culture into hand from your draw deck. Shuffle your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fire");

		assertEquals("Saved From the Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlacesAGondorCompanionInTheDeadPileToTakeUpToThreeGondorCardsFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetFreepsCard("fire");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var swordarm = scn.GetFreepsCard("swordarm");
		var mail = scn.GetFreepsCard("mail");
		var shield = scn.GetFreepsCard("shield");
		var caradhras = scn.GetFreepsCard("caradhras");
		scn.MoveCompanionsToTable(gandalf, aragorn, sam);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(fire);
		scn.MoveCardsToDiscard(shield);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(1, scn.GetFreepsHandCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(fire));

		scn.FreepsPlayCard(fire);
		// Any companion but the Ring-bearer: Sam, who is Ring-bound, and Gandalf himself among them. Not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetFreepsDeadCount());

		scn.FreepsDismissRevealedCards();
		// Aragorn's culture: the four [gondor] cards in the draw deck, of three card types. Not the [gandalf]
		// card of the event's own culture, nor the [gondor] card in the discard pile.
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, armor, swordarm, mail));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(caradhras, shield));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.MarkGameLog();
		scn.FreepsChooseCardBPFromSelection(boromir, armor, swordarm);

		assertInHand(boromir, armor, swordarm);
		assertInZone(Zone.DECK, mail);
		assertInDiscard(fire, shield);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(boromir, armor, swordarm));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(mail));
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GandalfMayBeTheCompanionPlacedAndThenGandalfCardsAreTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetFreepsCard("fire");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var caradhras = scn.GetFreepsCard("caradhras");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(fire);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeadCount());
		scn.FreepsPlayCard(fire);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.DEAD, gandalf);
		assertInPlay(aragorn);

		scn.FreepsDismissRevealedCards();
		// Gandalf's culture now: a companion, an ally and an event in the deck; no [gondor] card.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, barliman, caradhras));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, armor));
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(avenger, caradhras);

		assertInHand(avenger, caradhras);
		assertInZone(Zone.DECK, barliman, boromir, armor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughAnotherGandalfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetFreepsCard("fire");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveCardsToHand(fire, gandalf);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(fire));

		scn.FreepsPlayCard(gandalf);

		assertTrue(scn.FreepsPlayAvailable(fire));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayTakeNothingAndTheDeckIsStillShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetFreepsCard("fire");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(fire);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsPlayCard(fire);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, boromir);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
