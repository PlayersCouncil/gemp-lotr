package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_93_009_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: companion in draw deck to be played
					put("boromir", "1_96"); // Boromir: second companion to prove selectivity
					put("gimli", "1_13");   // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, vitality 3
					put("legolas", "1_50"); // Legolas, Greenleaf: a companion kept out of the draw deck
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally, a character who is no companion
					put("runner", "1_178"); // Goblin Runner: a minion
					put("filler", "1_3");   // Axe Strike: a Free Peoples event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_9", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: companion in draw deck to be played
					put("boromir", "1_96"); // Boromir: second companion to prove selectivity
					put("gimli", "1_13");   // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_9"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_9
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Fellowship: Add (3) to play a companion from your draw deck; they come into
		 * play exhausted.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_9", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void PlaysCompanionFromDeckExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");

		// Place Aragorn in draw deck
		scn.MoveCardsToTopOfDeck(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, aragorn);

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Should see the deck contents and be able to select Aragorn
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(aragorn);

		// Aragorn should now be in play, exhausted (vitality 4 = 3 wounds)
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// Cost was 3 twilight, +4 from Aragorn
		assertEquals(3 + 4, scn.GetTwilight());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CompanionsOfTheDrawDeckAreOfferedAndNothingElseAndEachComesIntoPlayExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var bounder = scn.GetFreepsCard("bounder");
		var runner = scn.GetFreepsCard("runner");
		var filler = scn.GetFreepsCard("filler");

		// A companion of his that is not in his draw deck
		scn.MoveCardsToDiscard(legolas);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(7, scn.GetShadowDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int otherShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// The (3) is paid before the deck is looked through
		assertEquals(3, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();

		// Two Men of [gondor] and a [dwarven] Dwarf; not the ally, the minion or the event of the same
		// deck, and not the companion in his discard pile. He need not play any of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, runner, filler, legolas));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(gimli);

		// Gimli costs 2 and has 3 vitality: exhausted is 2 wounds
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3 + 2, scn.GetTwilight());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, aragorn, boromir, bounder, runner, filler);
		assertInZone(Zone.DISCARD, legolas);
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// No limit: a second use plays Aragorn, who costs 4 and has 4 vitality: 3 wounds
		shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsChooseCardBPFromSelection(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(5 + 3 + 4, scn.GetTwilight());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);

		// The other player's deck was not looked through
		assertEquals(7, scn.GetShadowDeckCount());
		assertEquals(otherShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCompanionInTheDrawDeckTheDeckIsLookedThroughAndNothingIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();

		// His companions are in his hand: the draw deck holds an ally, a minion and an event
		scn.MoveCardsToHand(aragorn, boromir, gimli, legolas);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// He cannot know what his deck holds until he looks
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();

		// Nothing to play: the (3) stays paid, the deck is shuffled, and that is all
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, aragorn, boromir, gimli, legolas);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(mod));

		// A minion, so that there is a maneuver phase
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void OwnerGatingShadowCantUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToTopOfDeck(aragorn);

		scn.StartGame();

		// Shadow owns the mod — Freeps can't use it
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerTwoUsesItInHisOwnFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 owns the modifier, and these are his own cards
		var mod = scn.GetShadowCard("mod");
		var frodo = scn.GetShadowRingBearer();
		var boromir = scn.GetShadowCard("boromir");
		var gimli = scn.GetShadowCard("gimli");

		scn.StartGame();

		// Player 2's turn: the fellowship phase is now his
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The reconcile drew his whole deck into his hand: two companions are put back on it
		scn.MoveCardsToTopOfDeck(boromir, gimli);
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetTwilight());
		int shuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, gimli));
		scn.ShadowChooseCardBPFromSelection(gimli);

		// (3) and Gimli's 2; vitality 3, so 2 wounds
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3 + 2, scn.GetTwilight());
		assertEquals(1, scn.GetShadowDeckCount());
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
