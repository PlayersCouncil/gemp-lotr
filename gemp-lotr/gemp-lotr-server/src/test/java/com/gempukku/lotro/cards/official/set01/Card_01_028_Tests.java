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

public class Card_01_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wealth", "1_28");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion
					// Grimir, Dwarven Elder: a Dwarf ally; his Fellowship ability is used only on purpose
					put("grimir", "1_17");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf
					// Lord of Moria: a [dwarven] support-area condition, no Dwarf; "Each Dwarf is damage +1"
					put("lordofmoria", "1_21");

					// Cards for the draw deck.
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession
					put("axestrike", "1_3");    // Axe Strike: a [dwarven] Skirmish event
					put("marksman", "1_176");   // Goblin Marksman: a [moria] minion
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WealthofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Wealth of Moria
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot a Dwarf to reveal the top 3 cards of your draw deck. Take all Free People cards revealed into hand and discard the rest.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wealth");

		assertEquals("Wealth of Moria", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingADwarfCompanionRevealsTheTop3AndTakesTheFreePeoplesCardsIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wealth = scn.GetFreepsCard("wealth");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var cloak = scn.GetFreepsCard("cloak");
		var axestrike = scn.GetFreepsCard("axestrike");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(wealth);

		scn.StartGame();
		// Top first: a companion, a minion and a possession, and an event below them.
		scn.SetFreepsDrawDeck(arwen, marksman, cloak, axestrike);
		scn.FreepsRefreshDecision();

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInHand(wealth);
		assertTrue(scn.FreepsPlayAvailable(wealth));

		scn.FreepsPlayCard(wealth);
		// The top 3 are shown to each player, and the fourth card is not.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(arwen, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(marksman, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(cloak, false));
		scn.DismissRevealedCards();

		// Both Free Peoples cards are taken, with no choice asked; the minion is discarded.
		assertInHand(arwen, cloak);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(marksman, wealth);
		assertEquals(2, scn.GetFreepsDiscardCount());
		// The fourth card is still on top of the deck, and the other player's deck is untouched.
		assertEquals(1, scn.GetFreepsDeckCount());
		assertSame(axestrike, scn.GetFreepsTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingADwarfAllyRevealsWhatIsLeftAndTakesAFreePeoplesEventIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wealth = scn.GetFreepsCard("wealth");
		var grimir = scn.GetFreepsCard("grimir");
		var axestrike = scn.GetFreepsCard("axestrike");
		var savage = scn.GetFreepsCard("savage");
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(wealth);

		scn.StartGame();
		// Only two cards are left in the deck.
		scn.SetFreepsDrawDeck(axestrike, savage);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(wealth));

		scn.FreepsPlayCard(wealth);
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axestrike, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, false));
		scn.DismissRevealedCards();

		assertInHand(axestrike);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(savage, wealth);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wealth = scn.GetFreepsCard("wealth");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var arwen = scn.GetFreepsCard("arwen");
		var cloak = scn.GetFreepsCard("cloak");
		var axestrike = scn.GetFreepsCard("axestrike");
		// A companion who is no Dwarf, and a [dwarven] card that is none.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.MoveCardsToHand(wealth);

		scn.StartGame();
		scn.SetFreepsDrawDeck(arwen, cloak, axestrike);
		scn.FreepsRefreshDecision();

		assertInPlay(aragorn, lordofmoria);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertInHand(wealth);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wealth));
	}
}
