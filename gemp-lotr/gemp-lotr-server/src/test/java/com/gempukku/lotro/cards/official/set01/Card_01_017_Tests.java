package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimir", "1_17");

					// Cards for the discard piles. None of them is played.
					put("strike", "1_3");         // Axe Strike: [dwarven] skirmish event
					put("delving", "1_6");        // Delving: [dwarven] fellowship event
					put("tosses", "1_23");        // Nobody Tosses a Dwarf: [dwarven] response event
					put("halls", "1_26");         // Their Halls of Stone: [dwarven] skirmish event, for the hand
					put("fury", "1_4");           // Battle Fury: [dwarven] skirmish event, for the other player's discard pile
					put("guard", "1_7");          // Dwarf Guard: a [dwarven] card that is no event
					put("swordarm", "1_116");     // Swordarm of the White Tower: a [gondor] skirmish event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Grimir, Dwarven Elder
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Dwarf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fellowship:</b> Exert Grimir to shuffle a [dwarven] event from your discard pile into your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grimir");

		assertEquals("Grimir", card.getBlueprint().getTitle());
		assertEquals("Dwarven Elder", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void IsPlayedForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(grimir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(grimir));

		scn.FreepsPlayCard(grimir);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, grimir);
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(3, scn.GetVitality(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsGrimirToShuffleAChosenDwarvenEventFromHisDiscardPileIntoHisDrawDeckUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var strike = scn.GetFreepsCard("strike");
		var delving = scn.GetFreepsCard("delving");
		var tosses = scn.GetFreepsCard("tosses");
		var halls = scn.GetFreepsCard("halls");
		var guard = scn.GetFreepsCard("guard");
		var swordarm = scn.GetFreepsCard("swordarm");
		var shadowFury = scn.GetShadowCard("fury");
		scn.MoveAlliesToTable(grimir);
		// Three [dwarven] events of three kinds; a [dwarven] companion and a [gondor] event beside them.
		scn.MoveCardsToDiscard(strike, delving, tosses, guard, swordarm);
		// [dwarven] events that are not in this player's discard pile: one in his hand, one in the other
		// player's discard pile (the player's own Battle Fury is in his draw deck).
		scn.MoveCardsToHand(halls);
		scn.MoveCardsToDiscard(shadowFury);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsActionAvailable(grimir));

		scn.FreepsUseCardAction(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, delving, tosses));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, swordarm, halls, shadowFury));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(tosses);
		assertInZone(Zone.DECK, tosses);
		assertInZone(Zone.DISCARD, strike, delving, guard, swordarm, shadowFury);
		assertInZone(Zone.HAND, halls);
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.FreepsActionAvailable(grimir));

		scn.FreepsUseCardAction(grimir);
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, delving));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, swordarm));

		scn.FreepsChooseCardBPFromSelection(delving);
		assertInZone(Zone.DECK, tosses, delving);
		assertInZone(Zone.DISCARD, strike, guard, swordarm);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());

		// Vitality 3 with 2 wounds: exhausted, with a [dwarven] event still in the discard pile.
		assertFalse(scn.FreepsActionAvailable(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenEventInHisDiscardPileGrimirExertsAndNoCardLeavesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var halls = scn.GetFreepsCard("halls");
		var guard = scn.GetFreepsCard("guard");
		var swordarm = scn.GetFreepsCard("swordarm");
		var shadowFury = scn.GetShadowCard("fury");
		scn.MoveAlliesToTable(grimir);
		// Everything that is nearly a [dwarven] event in his discard pile.
		scn.MoveCardsToDiscard(guard, swordarm);
		scn.MoveCardsToHand(halls);
		scn.MoveCardsToDiscard(shadowFury);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(grimir));
		// The exertion can be paid, so the ability is on offer though there is no event to shuffle in.
		assertTrue(scn.FreepsActionAvailable(grimir));

		scn.FreepsUseCardAction(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertInZone(Zone.DISCARD, guard, swordarm, shadowFury);
		assertInZone(Zone.HAND, halls);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		// A shuffle into a draw deck is made though nothing is shuffled in.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
