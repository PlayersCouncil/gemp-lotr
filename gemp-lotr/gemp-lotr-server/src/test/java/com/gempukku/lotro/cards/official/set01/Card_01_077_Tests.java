package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("folly", "1_77");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3, a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4, a maneuver ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3, an archery ability only
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, vitality 2; able to exert, and no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo and Aragorn, with the event in hand.
	protected VirtualTableScenario GetTwoCompanionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("folly", "1_77");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4, a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetFollyBeOurCloakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Let Folly Be Our Cloak
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert X companions to remove (X).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("folly");

		assertEquals("Let Folly Be Our Cloak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "Exert X companions to remove (X)": an X of exertions is at least 1 (Q-0076).  The definition let the
	// choice of companions be answered with none, so the event could be played with nobody able to exert.
	// F-0445.
	@Test
	public void AtLeastOneCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoCompanionsScenario();

		var folly = scn.GetFreepsCard("folly");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(folly);

		scn.StartGame();
		scn.SetTwilight(3);

		// Both companions exhausted: nobody can exert, and the event cannot be played.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(folly));

		scn.RemoveWoundsFromChar(frodo, 3);
		scn.RemoveWoundsFromChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(folly));
		scn.FreepsPlayCard(folly);

		// One companion at the least, and as many as can exert.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(frodo, aragorn);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyCompanionsAbleToExertAreOfferedAndOneExertingRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var folly = scn.GetFreepsCard("folly");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gimli, aragorn, legolas);
		// An ally can exert as well as anyone, and is no companion
		scn.MoveAlliesToTable(bounder);
		// Legolas has vitality 3: with 2 wounds he is exhausted and cannot exert
		scn.AddWoundsToChar(legolas, 2);
		scn.MoveCardsToHand(folly);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(folly));

		scn.FreepsPlayCard(folly);
		// The Ring-bearer and two companions of other cultures and races can exert; X is at least 1
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// The event costs nothing: 5 - 1 removed
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(folly);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XIsNotHeldToTheTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		// X is what is paid, whatever it buys: two companions may exert to empty a pool of 1.
		var scn = GetScenario();

		var folly = scn.GetFreepsCard("folly");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(folly);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(folly));
		scn.FreepsPlayCard(folly);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli, aragorn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
