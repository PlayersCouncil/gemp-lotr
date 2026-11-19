package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gift", "6_19");
					// Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("legolas", "1_50");
					// Celeborn, Lord of Lorien: [elven] Elf ally; a fellowship ability used only on purpose
					put("celeborn", "1_34");
					// Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("aragorn", "1_89");

					// Cards laid on top of the draw deck.
					put("armor", "1_8");        // Dwarven Armor
					put("count", "4_69");       // Final Count
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GiftofForesightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gift of Foresight
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot an Elf to reveal the top card of your draw deck. You may discard it,
		 * return it to the top of your draw deck, or take it into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gift");

		assertEquals("Gift of Foresight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElfCompanionRevealsTheTopCardToBothPlayersAndItMayBeTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var count = scn.GetFreepsCard("count");
		var shadowCount = scn.GetShadowCard("count");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(gift);

		scn.StartGame();
		// The Armor on top, Final Count beneath it; Final Count on top of the Shadow player's deck.
		scn.MoveCardsToTopOfDeck(count, armor, shadowCount);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertSame(shadowCount, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, armor, count);
		assertInHand(gift);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(gift));
		scn.FreepsPlayCard(gift);

		// The top card is shown to both players, listed and not selectable.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		scn.DismissRevealedCards();

		// Three things to do with it.
		assertEquals(3, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Take");

		assertInHand(armor);
		assertInDiscard(gift);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(shadowCount, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingAnElfAllyRevealsTheTopCardAndItMayBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var celeborn = scn.GetFreepsCard("celeborn");
		var armor = scn.GetFreepsCard("armor");
		var count = scn.GetFreepsCard("count");
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(gift);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(count, armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor, count);
		int deck = scn.GetFreepsDeckCount();
		int hand = scn.GetFreepsHandCount();

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(gift));
		scn.FreepsPlayCard(gift);
		scn.DismissRevealedCards();
		scn.FreepsChooseOption("Discard");

		assertInDiscard(armor, gift);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The event left the hand, and nothing came into it.
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ReturningTheRevealedCardLeavesItOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(gift);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor);
		int deck = scn.GetFreepsDeckCount();
		int hand = scn.GetFreepsHandCount();

		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(gift);
		scn.DismissRevealedCards();
		scn.FreepsChooseOption("Return");

		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertInDiscard(gift);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gift);

		scn.StartGame();

		// A companion who is no Elf.
		assertInHand(gift);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gift));
	}

	@Test
	public void WithAnEmptyDrawDeckItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(gift);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInHand(gift);
		int hand = scn.GetFreepsHandCount();

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(gift));
		scn.FreepsPlayCard(gift);

		// Nothing is shown, and nothing is asked.
		assertFalse(scn.BeingShownCards(scn.P1()));
		assertFalse(scn.BeingShownCards(scn.P2()));
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInDiscard(gift);
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
