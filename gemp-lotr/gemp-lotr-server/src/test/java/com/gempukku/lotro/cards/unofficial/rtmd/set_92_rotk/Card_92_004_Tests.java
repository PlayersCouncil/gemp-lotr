package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_004_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "1_285");          // Bilbo's Pipe: [shire] possession, pipe, cost 1; bearer must be a Hobbit
					put("aragornpipe", "1_91");    // Aragorn's Pipe: [gondor] possession, pipe, cost 1; bearer must be a [gondor] companion
					put("gandalfpipe", "1_74");    // Gandalf's Pipe: [gandalf] possession, pipe, cost 0; bearer must be Gandalf, and no Gandalf is in this deck
					put("weed1", "1_305");         // Old Toby: [shire] possession, pipeweed, cost 1, to the support area; when played, may draw a card
					put("weed2", "1_305");
					put("leaf", "17_106");         // Halfling Leaf: [shire] condition, pipeweed, cost 1, to the support area; to play, spot a Hobbit
					put("smoke", "18_27");        // Ship of Smoke: [gandalf] condition, pipeweed, cost 0, to the support area; its trigger works only in play with Gandalf spotted
					put("sword", "1_299");         // Hobbit Sword: [shire] possession, hand weapon, cost 1; bearer must be a Hobbit. Neither pipe nor pipeweed
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: the [gondor] companion who may bear Aragorn's Pipe
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "1_285");          // Bilbo's Pipe: [shire] possession, pipe, cost 1; bearer must be a Hobbit
					put("aragornpipe", "1_91");    // Aragorn's Pipe: [gondor] possession, pipe, cost 1; bearer must be a [gondor] companion
					put("gandalfpipe", "1_74");    // Gandalf's Pipe: [gandalf] possession, pipe, cost 0; bearer must be Gandalf, and no Gandalf is in this deck
					put("weed1", "1_305");         // Old Toby: [shire] possession, pipeweed, cost 1, to the support area; when played, may draw a card
					put("weed2", "1_305");
					put("leaf", "17_106");         // Halfling Leaf: [shire] condition, pipeweed, cost 1, to the support area; to play, spot a Hobbit
					put("sword", "1_299");         // Hobbit Sword: [shire] possession, hand weapon, cost 1; bearer must be a Hobbit. Neither pipe nor pipeweed
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: the [gondor] companion who may bear Aragorn's Pipe
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_4"
		);
	}

	@Test
	public void PipeForPipeweedStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_4
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Fellowship: Play a pipe to play a pipeweed from your draw deck (or vice versa).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void PlayPipeToPlayPipeweedFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: Play a pipe from hand → play a pipeweed from draw deck.
		// Only pipe in hand, so it auto-selects as the cost.
		// Pipe auto-attaches to Frodo (only hobbit).
		// Then deck search for pipeweed.

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");
		var weed2 = scn.GetFreepsCard("weed2");
		var leaf = scn.GetFreepsCard("leaf");
		var smoke = scn.GetFreepsCard("smoke");
		var aragornpipe = scn.GetFreepsCard("aragornpipe");
		var sword = scn.GetFreepsCard("sword");

		// Aragorn is there so that his pipe, left in the deck, is a card that could be played
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(pipe);
		// weed1 stays in deck, with Halfling Leaf, Aragorn's Pipe and the sword.  The second Old Toby lies in
		// the discard pile: a choice from a deck goes by the card's printing, and either copy could answer it.
		scn.MoveCardsToDiscard(weed2);

		scn.StartGame();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.HAND, pipe);
		assertInZone(Zone.DECK, weed1);

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Pipe auto-selected (only pipe/pipeweed in hand), auto-attached to Frodo (only hobbit), and paid for
		assertAttachedTo(pipe, frodo);
		assertEquals(1, scn.GetTwilight());

		// Now searching deck for pipeweed — dismiss revealed cards, then choose
		scn.FreepsDismissRevealedCards();
		// A [shire] possession, a [shire] condition and a [gandalf] condition are each a pipeweed; a pipe and a sword in the deck are not
		assertTrue(scn.FreepsHasCardChoiceAvailable(weed1, leaf, smoke));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragornpipe, sword));
		scn.FreepsChooseCard(weed1);

		assertInZone(Zone.SUPPORT, weed1);
		assertInZone(Zone.ATTACHED, pipe);
		assertInZone(Zone.DECK, leaf, smoke, aragornpipe, sword);
		assertInZone(Zone.DISCARD, weed2);
		// 1 for Bilbo's Pipe + 1 for Old Toby
		assertEquals(2, scn.GetTwilight());

		// Old Toby's own "you may draw a card"
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// A deck that was looked through is shuffled; the other player's is not
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayPipeweedToPlayPipeFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: Play a pipeweed from hand → play a pipe from draw deck.

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var pipe = scn.GetFreepsCard("pipe");
		var aragornpipe = scn.GetFreepsCard("aragornpipe");
		var weed1 = scn.GetFreepsCard("weed1");
		var weed2 = scn.GetFreepsCard("weed2");
		var leaf = scn.GetFreepsCard("leaf");
		var smoke = scn.GetFreepsCard("smoke");
		var sword = scn.GetFreepsCard("sword");

		// With Aragorn in play there are two pipes in the deck that could be played, of two cultures
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(weed1);
		// pipe stays in deck

		scn.StartGame();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Weed auto-selected (only pipe/pipeweed in hand), plays to support area
		scn.FreepsDeclineOptionalTrigger(); //old toby's text
		assertInZone(Zone.SUPPORT, weed1);
		assertEquals(1, scn.GetTwilight());

		// Now searching deck for pipe — dismiss revealed cards, then choose
		scn.FreepsDismissRevealedCards();
		// The pipeweeds and the sword still in the deck are no pipes
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, aragornpipe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weed2, leaf, smoke, sword));
		scn.FreepsChooseCard(pipe);

		// Pipe auto-attaches to Frodo
		assertAttachedTo(pipe, frodo);
		assertInZone(Zone.SUPPORT, weed1);
		assertInZone(Zone.DECK, aragornpipe, weed2, leaf, smoke, sword);
		// 1 for Old Toby + 1 for Bilbo's Pipe
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChoosesBetweenPipeAndPipeweedInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: With both pipe and pipeweed in hand, player chooses which to play as cost.

		var scn = GetFreepsScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");
		var weed2 = scn.GetFreepsCard("weed2");
		var sword = scn.GetFreepsCard("sword");
		var aragornpipe = scn.GetFreepsCard("aragornpipe");
		var smoke = scn.GetFreepsCard("smoke");

		// The sword is a possession Frodo could bear, and it is neither a pipe nor a pipeweed.  Two pipes of two
		// cultures (Aragorn is there to bear his) and two pipeweeds of two cultures are the price that may be paid.
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(pipe, aragornpipe, weed1, smoke, sword);
		// weed2 stays in deck

		scn.StartGame();

		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));

		// Should offer choice between the pipes and the pipeweeds
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, aragornpipe, weed1, smoke));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.FreepsGetChoiceMin());

		// Choose pipe → should then search for pipeweed
		scn.FreepsChooseCard(pipe);

		// Pipe auto-attaches to Frodo
		// Now searching deck for pipeweed
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(weed2);

		assertInZone(Zone.ATTACHED, pipe);
		assertInZone(Zone.SUPPORT, weed2);
		assertInZone(Zone.HAND, aragornpipe, weed1, smoke, sword);

		// Old Toby's own "you may draw a card"
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotUseWithoutPipeOrPipeweedInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: No pipe or pipeweed in hand = ability not available.

		var scn = GetFreepsScenario();

		// Don't put any pipes or pipeweeds in hand that can be played: a sword Frodo could bear, and a pipe
		// that only Gandalf may bear.  Every other pipe and pipeweed is in the draw deck.
		scn.MoveCardsToHand(scn.GetFreepsCard("sword"), scn.GetFreepsCard("gandalfpipe"));

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(scn.GetFreepsCard("mod")));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoPipeweedInTheDrawDeckThePipeIsStillPlayedAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		// There is no penalty for finding nothing in a draw deck: the pipe is the price, and it is paid.

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var pipe = scn.GetFreepsCard("pipe");
		var aragornpipe = scn.GetFreepsCard("aragornpipe");

		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(pipe);
		// Every pipeweed is in the discard pile; a pipe that could be played stays in the deck
		scn.MoveCardsToFreepsDiscard("weed1", "weed2", "leaf", "smoke");

		scn.StartGame();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(4, scn.GetFreepsDiscardCount());

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// The deck is shown and there is nothing in it to choose
		scn.FreepsDismissRevealedCards();

		assertAttachedTo(pipe, frodo);
		// 1 for Bilbo's Pipe and nothing more
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DECK, aragornpipe);
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HavingLookedThePlayerMayPlayNoPipeweed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");
		var weed2 = scn.GetFreepsCard("weed2");
		var leaf = scn.GetFreepsCard("leaf");
		var smoke = scn.GetFreepsCard("smoke");

		scn.MoveCardsToHand(pipe);

		scn.StartGame();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(mod);
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(weed1, leaf, smoke));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertAttachedTo(pipe, frodo);
		assertInZone(Zone.DECK, weed1, weed2, leaf, smoke);
		// 1 for Bilbo's Pipe and nothing more
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HavingLookedThePlayerMayPlayNoPipe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");

		scn.MoveCardsToHand(weed1);

		scn.StartGame();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(mod);
		// Old Toby's own "you may draw a card"
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.SUPPORT, weed1);
		assertInZone(Zone.DECK, pipe);
		// 1 for Old Toby and nothing more
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDeckIsShuffledBeforeAPipeweedPlayedFromItOffersItsDraw() throws DecisionResultInvalidException, CardNotFoundException {
		// "When you finish looking through it, reshuffle it": the look is over once Old Toby has been taken
		// out and played, so the card he then draws should come off a shuffled deck.

		var scn = GetFreepsScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");

		scn.MoveCardsToHand(pipe);
		// One Old Toby only in the deck, so that the choice can only be this one
		scn.MoveCardsToFreepsDiscard("weed2");

		scn.StartGame();

		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(weed1);

		assertInZone(Zone.SUPPORT, weed1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);

		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowCopyDoesNotWorkForFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: Shadow's copy should not give FP the ability.

		var scn = GetShadowScenario();

		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCardsToHand(pipe);
		// Frodo could bear the pipe, and the pipeweeds are in the deck: only the owner is missing

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(scn.GetShadowCard("mod")));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
