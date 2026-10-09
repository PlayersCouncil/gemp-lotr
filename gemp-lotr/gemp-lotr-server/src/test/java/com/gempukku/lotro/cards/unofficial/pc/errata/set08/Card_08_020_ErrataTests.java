package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_08_020_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fire", "58_20");
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
		 * Game Text: Spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take up to X cards
		 * from that companion's culture into hand from your draw deck, where X is the current region number +1.
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
	public void PlacesAGondorCompanionInTheDeadPileToTakeUpToTwoGondorCardsFromTheDrawDeckAtSiteOne() throws DecisionResultInvalidException, CardNotFoundException {
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

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
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
		assertEquals(3, scn.GetTwilight());

		scn.FreepsDismissRevealedCards();
		// Aragorn's culture: the four [gondor] cards in the draw deck, of three card types. Not the [gandalf]
		// cards of the event's own culture, nor the [gondor] card in the discard pile.
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, armor, swordarm, mail));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(caradhras, shield));
		// Region 1 (site 1) + 1
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.MarkGameLog();
		scn.FreepsChooseCardBPFromSelection(boromir, swordarm);

		assertInHand(boromir, swordarm);
		assertInZone(Zone.DECK, armor, mail);
		assertInDiscard(fire, shield);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(boromir, swordarm));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(armor));
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
		assertEquals(2, scn.FreepsGetChoiceMax());

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
	public void AtSiteFourInRegionTwoUpToThreeCardsAreTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetFreepsCard("fire");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var swordarm = scn.GetFreepsCard("swordarm");
		var mail = scn.GetFreepsCard("mail");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(fire);
		// The fifth [gondor] card waits in the discard pile, as in the test at site 1: left in the draw deck,
		// the reconciles on the way would draw it or not by chance (F-0265).
		scn.MoveCardsToDiscard(shield);

		scn.StartGame();
		scn.SkipToSite(4);
		// The reconciles on the way drew most of the small deck into the hand: the four [gondor] cards go back,
		// from wherever they are.
		scn.MoveCardsToTopOfDeck(boromir, armor, swordarm, mail);
		assertInZone(Zone.DECK, boromir, armor, swordarm, mail);
		assertInDiscard(shield);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(fire));
		scn.FreepsPlayCard(fire);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsDismissRevealedCards();

		assertEquals(4, scn.FreepsGetCardChoiceCount());
		// Region 2 (sites 4 to 6) + 1
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(boromir, armor, swordarm);

		assertInHand(boromir, armor, swordarm);
		assertInZone(Zone.DECK, mail);
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
