package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forewarned", "6_17");
					put("forewarned2", "6_17");
					// Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("legolas", "1_50");
					// Celeborn, Lord of Lorien: [elven] Elf ally, twilight 2, no requirement to play; a fellowship
					// ability used only on purpose
					put("celeborn", "1_34");
					// Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Uruk Savage: [isengard] Uruk-hai; Damage +1 is its whole text
					put("savage", "1_151");

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
	public void ForewarnedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Forewarned
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 Elves. Plays to your support area.
		 * <b>Fellowship:</b> Add (1) to look at the top card of your draw deck. You may discard this condition to
		 * discard that card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forewarned");

		assertEquals("Forewarned", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondElfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forewarned = scn.GetFreepsCard("forewarned");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(forewarned, celeborn);

		scn.StartGame();

		// One Elf, and a companion who is no Elf beside him: not enough.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(forewarned, celeborn);
		assertFalse(scn.FreepsPlayAvailable(forewarned));

		// An Elf ally played: two Elves, a companion and an ally.
		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(forewarned));

		scn.FreepsPlayCard(forewarned);
		assertInZone(Zone.SUPPORT, forewarned);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityAddsOneToLookAtTheTopCardShownToItsOwnerAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forewarned = scn.GetFreepsCard("forewarned");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(forewarned);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor);
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(forewarned));
		scn.FreepsUseCardAction(forewarned);

		assertEquals(1, scn.GetTwilight());
		// The top card is shown to its owner, listed and not selectable, and the other player is shown nothing.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		assertFalse(scn.BeingShownCards(scn.P2()));
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
	}

	@Test
	public void DiscardingThisConditionDiscardsTheCardLookedAtAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forewarned = scn.GetFreepsCard("forewarned");
		var forewarned2 = scn.GetFreepsCard("forewarned2");
		var armor = scn.GetFreepsCard("armor");
		var count = scn.GetFreepsCard("count");
		scn.MoveCardsToSupportArea(forewarned, forewarned2);

		scn.StartGame();
		// The Armor on top, Final Count beneath it.
		scn.MoveCardsToTopOfDeck(count, armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor, count);
		assertInZone(Zone.SUPPORT, forewarned, forewarned2);
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(forewarned);
		assertEquals(1, scn.GetTwilight());
		scn.DismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseYes();

		// The copy used is discarded, and with it the card looked at; the other copy and the next card stay.
		assertInDiscard(forewarned, armor);
		assertInZone(Zone.SUPPORT, forewarned2);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningKeepsTheConditionAndTheCardOnTop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forewarned = scn.GetFreepsCard("forewarned");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(forewarned);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor);
		assertInZone(Zone.SUPPORT, forewarned);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(forewarned);
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseNo();

		assertInZone(Zone.SUPPORT, forewarned);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forewarned = scn.GetFreepsCard("forewarned");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(forewarned);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(forewarned));
	}

	@Test
	public void WithAnEmptyDrawDeckTheLookShowsNothingAndTheConditionMayStillBeDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forewarned = scn.GetFreepsCard("forewarned");
		scn.MoveCardsToSupportArea(forewarned);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, forewarned);

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(forewarned));
		scn.FreepsUseCardAction(forewarned);
		assertEquals(1, scn.GetTwilight());

		// A look at an empty deck shows its owner nothing.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.BeingShownCards(scn.P1()));
		scn.FreepsDismissRevealedCards();

		// "You may discard this condition" has a cost of its own, and is asked though there is no card to discard.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseYes();
		assertInDiscard(forewarned);
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
