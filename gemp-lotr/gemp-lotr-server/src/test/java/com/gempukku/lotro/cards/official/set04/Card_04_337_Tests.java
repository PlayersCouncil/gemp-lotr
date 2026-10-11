package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_337_Tests
{

	// The Towers sites: Barrows of Edoras is site 3 of them.  Both players hold every card below, and whatever
	// a test does not place lies in its owner's draw deck.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cards to hold in hand: a companion, a possession and a Shadow minion.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("armor", "1_8");        // Dwarven Armor
					put("troop", "1_177");      // Goblin Patrol Troop

					// Cards to draw.
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("guard", "1_7");        // Dwarf Guard
					put("runner", "1_178");     // Goblin Runner
					put("savage", "1_151");     // Uruk Savage
					put("marksman", "1_176");   // Goblin Marksman
					put("brigand", "4_10");     // Dunlending Brigand
					put("soldier", "1_271");    // Orc Soldier
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarrowsofEdorasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Barrows of Edoras
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Place your hand beneath your draw deck and draw 4 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Barrows of Edoras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void FellowshipActionPlacesTheWholeHandBeneathTheDrawDeckAndDrawsFourCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetFreepsCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetFreepsCard("runner");
		var savage = scn.GetFreepsCard("savage");
		var marksman = scn.GetFreepsCard("marksman");

		scn.StartGame();
		scn.SkipToSite(3);

		// The site under the fellowship is the copy the other player laid.
		var barrows = scn.GetShadowSite(3);
		assertSame(barrows, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		// Three cards in hand, of three kinds; Marksman, Savage, Runner, Guard and Gimli on top of the deck,
		// Gimli highest.  The other player holds two cards.
		scn.SetFreepsHand(aragorn, armor, troop);
		scn.MoveCardsToTopOfDeck(marksman, savage, runner, guard, gimli);
		scn.SetShadowHand(scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"));
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(barrows));
		scn.FreepsUseCardAction(barrows);

		// He places the cards one at a time; the last goes unasked.
		scn.FreepsChooseCardBPFromSelection(aragorn);
		scn.FreepsChooseCardBPFromSelection(armor);

		// His whole hand lies beneath his draw deck, the first card placed highest.
		assertSame(troop, scn.GetFromBottomOfFreepsDeck(1));
		assertSame(armor, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(aragorn, scn.GetFromBottomOfFreepsDeck(3));
		// And his hand is the four cards that were on top, with the fifth still in the deck.
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(gimli, guard, runner, savage);
		assertInZone(Zone.DECK, marksman);
		// 7 + 3 placed - 4 drawn
		assertEquals(6, scn.GetFreepsDeckCount());

		// "Your" hand and "your" draw deck: the other player's are as they were.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondUseInOneFellowshipPhasePlacesTheHandBeneathAndDrawsNothingAndThenTheActionIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetFreepsCard("runner");
		var savage = scn.GetFreepsCard("savage");
		var marksman = scn.GetFreepsCard("marksman");

		scn.StartGame();
		scn.SkipToSite(3);
		var barrows = scn.GetCurrentSite();

		scn.SetFreepsHand(aragorn, armor);
		scn.MoveCardsToTopOfDeck(marksman, savage, runner, guard, gimli);
		scn.FreepsRefreshDecision();
		assertEquals(8, scn.GetFreepsDeckCount());

		scn.FreepsUseCardAction(barrows);
		scn.FreepsChooseCardBPFromSelection(aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(gimli, guard, runner, savage);
		// 8 + 2 placed - 4 drawn
		assertEquals(6, scn.GetFreepsDeckCount());

		// Again in the same fellowship phase: the four cards go beneath the deck, and the Rule of 4 lets no
		// fifth card be drawn.
		assertTrue(scn.FreepsActionAvailable(barrows));
		scn.FreepsUseCardAction(barrows);
		scn.FreepsChooseCardBPFromSelection(gimli);
		scn.FreepsChooseCardBPFromSelection(guard);
		scn.FreepsChooseCardBPFromSelection(runner);

		assertSame(savage, scn.GetFromBottomOfFreepsDeck(1));
		assertSame(runner, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(guard, scn.GetFromBottomOfFreepsDeck(3));
		assertSame(gimli, scn.GetFromBottomOfFreepsDeck(4));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(10, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, marksman);

		// With no card in hand and no card left to draw this phase the action would do nothing: not offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(barrows));
	}

	@Test
	public void WithNoCardInHandTheActionStillDrawsFourCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetFreepsCard("runner");
		var savage = scn.GetFreepsCard("savage");
		var marksman = scn.GetFreepsCard("marksman");

		scn.StartGame();
		scn.SkipToSite(3);
		var barrows = scn.GetCurrentSite();

		// An empty hand, and the whole of his deck to draw from.
		scn.SetFreepsHand();
		scn.MoveCardsToTopOfDeck(marksman, savage, runner, guard, gimli);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(10, scn.GetFreepsDeckCount());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(barrows));

		// Nothing to place beneath the deck: he does what he can, and draws 4.
		scn.FreepsUseCardAction(barrows);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(gimli, guard, runner, savage);
		assertInZone(Zone.DECK, marksman);
		assertEquals(6, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheActionIsNotOfferedInTheManeuverPhaseAtTheBarrows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(aragorn, armor);

		// The move to the Barrows; a minion, so that there is a maneuver phase.
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var barrows = scn.GetCurrentSite();
		assertEquals("Barrows of Edoras", barrows.getBlueprint().getTitle());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(barrows));
	}
}
