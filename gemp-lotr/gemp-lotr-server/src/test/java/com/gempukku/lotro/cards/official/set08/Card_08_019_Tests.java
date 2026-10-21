package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("doorstep", "8_19");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] ally; an archery ability used only on purpose
					// Ent Avenger: a [gandalf] companion who is not Gandalf; strength +2 while 3 Ents can be spotted
					put("avenger", "6_27");

					// For the discard pile: [gondor] cards of three types, and two [gandalf] cards
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession
					put("swordarm", "1_116");   // Swordarm of the White Tower: a [gondor] Skirmish event
					put("glamdring", "1_75");   // Glamdring: a [gandalf] possession
					put("staff", "2_22");       // Gandalf's Staff: a [gandalf] artifact
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OnYourDoorstepStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: On Your Doorstep
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Gandalf and exert a companion to place a card of that companion's culture from your discard pile on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("doorstep");

		assertEquals("On Your Doorstep", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingAGondorCompanionPlacesAGondorCardFromTheDiscardPileOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorstep = scn.GetFreepsCard("doorstep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var swordarm = scn.GetFreepsCard("swordarm");
		var glamdring = scn.GetFreepsCard("glamdring");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToDiscard(boromir, sword, swordarm, glamdring);
		scn.MoveCardsToHand(doorstep);

		scn.StartGame();

		assertInDiscard(boromir, sword, swordarm, glamdring);
		assertNotSame(sword, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(doorstep));

		scn.FreepsPlayCard(doorstep);
		// Any companion may pay; an ally may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// A [gondor] card of any type; the [gandalf] card is not of Aragorn's culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, sword, swordarm));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		assertSame(sword, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, sword);
		assertInDiscard(boromir, swordarm, glamdring, doorstep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingGandalfPlacesAGandalfCardAndNoGondorCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorstep = scn.GetFreepsCard("doorstep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToDiscard(sword, glamdring, staff);
		scn.MoveCardsToHand(doorstep);

		scn.StartGame();

		assertInDiscard(sword, glamdring, staff);
		assertNotSame(staff, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(doorstep);
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, staff));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));
		scn.FreepsChooseCard(staff);

		assertSame(staff, scn.GetFreepsTopOfDeck());
		assertInDiscard(sword, glamdring);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingACompanionWithNoCardOfHisCultureInTheDiscardPilePlacesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorstep = scn.GetFreepsCard("doorstep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		var glamdring = scn.GetFreepsCard("glamdring");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(sword, glamdring);
		scn.MoveCardsToHand(doorstep);

		scn.StartGame();

		assertInDiscard(sword, glamdring);
		var top = scn.GetFreepsTopOfDeck();
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Frodo is [shire], and the discard pile holds no [shire] card: the event is played for nothing.
		scn.FreepsPlayCard(doorstep);
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertSame(top, scn.GetFreepsTopOfDeck());
		assertInDiscard(sword, glamdring, doorstep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorstep = scn.GetFreepsCard("doorstep");
		var avenger = scn.GetFreepsCard("avenger");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToDiscard(glamdring);
		scn.MoveCardsToHand(doorstep);

		scn.StartGame();

		// The Ent Avenger is a [gandalf] companion who could exert, and no Gandalf.
		assertInDiscard(glamdring);
		assertInHand(doorstep);
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertFalse(scn.FreepsPlayAvailable(doorstep));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithEveryCompanionExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorstep = scn.GetFreepsCard("doorstep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var orophin = scn.GetFreepsCard("orophin");
		var glamdring = scn.GetFreepsCard("glamdring");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToDiscard(glamdring);
		scn.MoveCardsToHand(doorstep);

		scn.StartGame();
		// Vitality 4 each, with 3 wounds; Orophin, an ally, could exert.
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertInDiscard(glamdring);
		assertInHand(doorstep);
		assertFalse(scn.FreepsPlayAvailable(doorstep));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
