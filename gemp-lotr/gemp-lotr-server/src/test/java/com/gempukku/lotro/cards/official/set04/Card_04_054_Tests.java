package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rest", "4_54");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetConditionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rest", "4_54");
					put("stout", "4_57");       // Stout and Strong: unique [dwarven] support-area condition; a trigger on a Dwarf winning, a skirmish ability
					put("stairs", "1_24");      // Stairs of Khazad-dûm: [dwarven] support-area condition; a strength bonus in a skirmish only
					put("arms", "4_82");        // Strength of Arms: the [elven] twin of Stout and Strong
					// Here Is Good Rock: unique [dwarven] support-area condition, "Skirmish: Stack a Free
					// Peoples card from hand here to make a Dwarf damage +1."
					put("rock", "4_50");
					put("guard", "1_7");        // Dwarf Guard: a Free Peoples card for Here Is Good Rock to hold
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RestbyBlindNightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Rest by Blind Night
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Shuffle any number of [dwarven] conditions (and all cards stacked on them) into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rest");

		assertEquals("Rest by Blind Night", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedWithNoDwarvenConditionInPlayAndShufflesTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rest = scn.GetFreepsCard("rest");
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		// No minions: the game goes straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		// Ketura on Q-0112: "Rest by Blind Night should clearly be playable with no effect"; the shuffle of no
		// cards still shuffles the deck, as from a discard pile (Q-0102).
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(rest));
		scn.FreepsPlayCard(rest);

		assertInZone(Zone.DISCARD, rest);
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ADwarvenConditionChosenIsShuffledIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetConditionsScenario();

		var rest = scn.GetFreepsCard("rest");
		var stout = scn.GetFreepsCard("stout");
		var stairs = scn.GetFreepsCard("stairs");
		var arms = scn.GetFreepsCard("arms");
		scn.MoveCardsToSupportArea(stout, stairs, arms);
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		// No minions: the game goes straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		assertTrue(scn.FreepsPlayAvailable(rest));
		scn.FreepsPlayCard(rest);
		// "Any number": none need be chosen.  The [elven] condition is no candidate.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(stout, stairs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arms));

		scn.FreepsChooseCards(stout);
		assertInZone(Zone.DECK, stout);
		assertEquals(freepsDeck + 1, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SUPPORT, stairs, arms);
		assertInZone(Zone.DISCARD, rest);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ADwarvenConditionChosenTakesTheCardStackedOnItIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetConditionsScenario();

		var rest = scn.GetFreepsCard("rest");
		var stout = scn.GetFreepsCard("stout");
		var rock = scn.GetFreepsCard("rock");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(stout, rock);
		scn.StackCardsOn(rock, guard);
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertInZone(Zone.STACKED, guard);

		scn.FreepsPlayCard(rest);
		assertTrue(scn.FreepsHasCardChoiceAvailable(stout, rock));
		scn.FreepsChooseCards(rock);

		// The condition and the card stacked on it: two more cards in the deck, in one shuffle.
		assertInZone(Zone.DECK, rock, guard);
		assertEquals(freepsDeck + 2, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.SUPPORT, stout);
		assertInZone(Zone.DISCARD, rest);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
