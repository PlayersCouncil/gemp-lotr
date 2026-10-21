package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shards", "3_44");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a [gondor] companion; no ability
					put("armor", "1_92");       // Armor: a [gondor] possession; no ability
					put("resisted", "3_45");    // Some Who Resisted: a [gondor] Maneuver event
					put("again", "1_317");      // There and Back Again: a [shire] condition, a card of another culture
					// Here Is Good Rock: [dwarven] support-area condition that its own skirmish ability stacks Free Peoples
					// cards on; another card in play with cards stacked on it
					put("rock", "4_50");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Free Peoples card stacked on the Rock
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase; cheated in, it adds nothing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheShardsofNarsilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Shards of Narsil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	<b>Fellowship:</b> Stack a [gondor] card from hand here.
		 * 	<b>Fellowship:</b> Add (1) to take a card stacked here into hand.
		*/

		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");

		assertEquals("The Shards of Narsil", shards.getBlueprint().getTitle());
		assertNull(shards.getBlueprint().getSubtitle());
		assertTrue(shards.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, shards.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, shards.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, shards.getBlueprint().getCardType());
		assertNull(shards.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(shards, Keyword.SUPPORT_AREA));
		assertEquals(1, shards.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityStacksAGondorCardOfAnyTypeFromHandOnTheShards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var resisted = scn.GetFreepsCard("resisted");
		var again = scn.GetFreepsCard("again");
		var rock = scn.GetFreepsCard("rock");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(shards, rock);
		scn.StackCardsOn(rock, gimli);
		scn.MoveCardsToHand(boromir, armor, resisted, again);
		assertEquals(0, scn.GetStackedCards(shards).size());
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertInHand(boromir, armor, resisted, again);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(shards, "Stack"));

		scn.FreepsUseCardAction(shards, "Stack");
		// A [gondor] companion, possession and event; not the [shire] card.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, armor, resisted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(again));
		scn.FreepsChooseCard(armor);

		assertInZone(Zone.STACKED, armor);
		assertEquals(1, scn.GetStackedCards(shards).size());
		assertSame(armor, scn.GetStackedCards(shards).get(0));
		// "Here": the Rock's stack is as it was.
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertSame(gimli, scn.GetStackedCards(rock).get(0));
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(boromir, resisted, again);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StackingIsNotOfferedWithNoGondorCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var boromir = scn.GetFreepsCard("boromir");
		var resisted = scn.GetFreepsCard("resisted");
		var again = scn.GetFreepsCard("again");
		scn.MoveCardsToSupportArea(shards);
		// [gondor] cards in play and in the discard pile; in hand, only a [shire] card.
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToDiscard(resisted);
		scn.MoveCardsToHand(again);
		assertInDiscard(resisted);
		assertInHand(again);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(shards, "Stack"));
	}

	@Test
	public void StackingIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(armor);
		assertInHand(armor);

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(shards, "Stack"));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInHand(armor);
		assertFalse(scn.FreepsActionAvailable(shards, "Stack"));
	}

	@Test
	public void FellowshipAbilityAddsOneToTakeACardStackedOnTheShardsIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var boromir = scn.GetFreepsCard("boromir");
		var resisted = scn.GetFreepsCard("resisted");
		var rock = scn.GetFreepsCard("rock");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(shards, rock);
		// A [gondor] companion and a [gondor] event on the Shards; another card on the Rock.
		scn.StackCardsOn(shards, boromir, resisted);
		scn.StackCardsOn(rock, gimli);
		assertEquals(2, scn.GetStackedCards(shards).size());
		assertInZone(Zone.STACKED, boromir, resisted);
		assertEquals(1, scn.GetStackedCards(rock).size());

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(shards, "take"));

		scn.FreepsUseCardAction(shards, "take");
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, resisted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(resisted);

		assertInHand(resisted);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetStackedCards(shards).size());
		assertSame(boromir, scn.GetStackedCards(shards).get(0));
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertSame(gimli, scn.GetStackedCards(rock).get(0));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingStackedOnTheShardsTheTakeIsPaidForAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var rock = scn.GetFreepsCard("rock");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(shards, rock);
		scn.StackCardsOn(rock, gimli);
		assertEquals(0, scn.GetStackedCards(shards).size());
		assertEquals(1, scn.GetStackedCards(rock).size());

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(shards, "take"));

		scn.FreepsUseCardAction(shards, "take");
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertSame(gimli, scn.GetStackedCards(rock).get(0));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(shards);
		scn.StackCardsOn(shards, boromir);
		assertEquals(1, scn.GetStackedCards(shards).size());

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(shards, "take"));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetStackedCards(shards).size());
		assertFalse(scn.FreepsActionAvailable(shards, "take"));
	}
}
