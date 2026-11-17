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

import static com.gempukku.lotro.framework.Assertions.*;
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

	protected VirtualTableScenario GetSpreadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rest", "4_54");
					// Here Is Good Rock: [dwarven] support-area condition that play stacks Free Peoples cards on; a skirmish ability only
					put("rock", "4_50");
					// From the Armory: [dwarven] support-area condition; a skirmish ability only
					put("armory", "4_47");
					// Endurance of Dwarves: [dwarven] condition borne by a Dwarf, vitality +1; a skirmish ability only
					put("endurance", "2_4");
					// Dwarven Axe: a [dwarven] possession, no condition; its trigger is on a skirmish lost to its bearer
					put("dwaxe", "1_9");
					// Ithilien Trap: a [gondor] support-area condition; its trigger is on a [gondor] Man winning a skirmish
					put("trap", "4_126");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: a Free Peoples card stacked on Here Is Good Rock
					put("sword", "1_299");      // Hobbit Sword: a second
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase; its play trigger does nothing when cheated in
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
		assertInZone(Zone.SUPPORT, stout, stairs, arms);
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
		assertInZone(Zone.SUPPORT, stout, rock);
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

	@Test
	public void AnyNumberOfDwarvenConditionsBorneOrNotAreOfferedAndThoseChosenGoInWithTheirStackedCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpreadScenario();

		var rest = scn.GetFreepsCard("rest");
		var rock = scn.GetFreepsCard("rock");
		var armory = scn.GetFreepsCard("armory");
		var endurance = scn.GetFreepsCard("endurance");
		var dwaxe = scn.GetFreepsCard("dwaxe");
		var trap = scn.GetFreepsCard("trap");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock, armory, trap);
		scn.AttachCardsTo(gimli, endurance, dwaxe);
		scn.StackCardsOn(rock, guard, sword);
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, rock, armory, trap);
		assertInZone(Zone.ATTACHED, endurance, dwaxe);
		assertInZone(Zone.STACKED, guard, sword);
		// No minions: the game goes straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(rest);
		// Every [dwarven] condition, in the support area or borne; not a [dwarven] possession, nor a condition of
		// another culture.  "Any number": from none to all three.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rock, armory, endurance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwaxe, trap));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(rock, endurance);
		// The two chosen and the two cards stacked on the Rock: four more cards in the deck, in one shuffle.
		assertInZone(Zone.DECK, rock, endurance, guard, sword);
		assertEquals(freepsDeck + 4, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SUPPORT, armory, trap);
		assertInZone(Zone.ATTACHED, dwaxe);
		assertInDiscard(rest);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ChoosingNoConditionShufflesTheDrawDeckAndMovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpreadScenario();

		var rest = scn.GetFreepsCard("rest");
		var rock = scn.GetFreepsCard("rock");
		var armory = scn.GetFreepsCard("armory");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(rock, armory);
		scn.StackCardsOn(rock, guard);
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, rock, armory);
		assertInZone(Zone.STACKED, guard);
		scn.SkipToPhase(Phase.REGROUP);

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(rest);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rock, armory));
		scn.FreepsDeclineChoosing();

		// Ketura on Q-0112: the shuffle of no cards still shuffles the deck.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, rock, armory);
		assertInZone(Zone.STACKED, guard);
		assertInDiscard(rest);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheManeuverPhaseAndIsInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpreadScenario();

		var rest = scn.GetFreepsCard("rest");
		var armory = scn.GetFreepsCard("armory");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, armory);
		scn.MoveMinionsToTable(runner);

		// The maneuver phase, before the regroup phase of the same turn.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(rest));

		// The Runner left unassigned, the game reaches the regroup phase with it in play.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(rest));
		assertInHand(rest);
	}
}
