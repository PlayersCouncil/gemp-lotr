package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herb", "103_90");
					put("aragornspipe", "1_91");  // Aragorn's Pipe: [gondor], cost 1, bearer must be a [gondor] companion
					put("bilbospipe", "1_285");   // Bilbo's Pipe: [shire], cost 1, bearer must be a Hobbit
					put("gafferspipe", "1_292");  // The Gaffer's Pipe: [shire], cost 1, bearer must be a Hobbit
					put("gandalfspipe", "1_74");  // Gandalf's Pipe: [gandalf], cost 0, bearer must be Gandalf
					put("frodospipe", "3_107");   // Frodo's Pipe: [shire], cost 1, bearer must be Frodo
					put("toby", "1_305");         // Old Toby: a pipeweed possession, and no pipe
					put("sword", "1_299");        // Hobbit Sword: a hand weapon a Hobbit bears

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the [gondor] companion
					put("sam", "1_311");          // Sam, Son of Hamfast: a second Hobbit
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: a fellowship ability never used here
					put("theoden", "7_255");      // Théoden, Rekindled King
					put("king", "102_55");        // Theoden, Last of His House: his title is written without the accent
					put("guard", "5_83");         // Household Guard: a [rohan] Man who is not Théoden

					put("runner", "1_178");       // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TeachMeYourHerbloreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Teach Me Your Herb-lore
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Play up to 2 pipes from your draw deck.  If you cannot spot Theoden, add a burden for each pipe played.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herb");

		assertEquals("Teach Me Your Herb-lore", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysTwoPipesFromTheDrawDeckOnBearersOfTheirOwnAndWithNoTheodenAddsABurdenForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var herb = scn.GetFreepsCard("herb");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var toby = scn.GetFreepsCard("toby");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var king = scn.GetFreepsCard("king");
		// A [rohan] Man who is not Theoden is in play, and a Theoden is in the dead pile.
		scn.MoveCompanionsToTable(aragorn, sam, guard);
		scn.MoveCardsToDeadPile(king);
		// A pipe in hand and a pipe in the discard pile.
		scn.MoveCardsToHand(herb, gafferspipe);
		scn.MoveCardsToDiscard(frodospipe);

		scn.StartGame();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DECK, aragornspipe, bilbospipe, gandalfspipe, toby, sword);
		assertTrue(scn.FreepsPlayAvailable(herb));

		scn.FreepsPlayCard(herb);

		assertEquals(2, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();
		// A [gondor] pipe and a [shire] pipe.  Not Gandalf's Pipe, which nobody here can bear; not the
		// pipeweed or the sword; not the pipe in hand or the pipe in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragornspipe, bilbospipe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalfspipe, toby, sword, gafferspipe, frodospipe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(bilbospipe);

		// Bearer must be a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guard));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(bilbospipe, sam);
		// 2 for the event + 1 for the pipe
		assertEquals(3, scn.GetTwilight());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragornspipe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbospipe, gandalfspipe, toby, sword, gafferspipe, frodospipe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(aragornspipe);

		// Aragorn is the only [gondor] companion.
		assertAttachedTo(aragornspipe, aragorn);
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, gandalfspipe, toby, sword);
		assertInZone(Zone.HAND, gafferspipe);
		assertInZone(Zone.DISCARD, frodospipe, herb);
		// A deck that has been looked through is reshuffled; the other player's is left alone.
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheodenInPlayTwoPipesArePlayedAndNoBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var herb = scn.GetFreepsCard("herb");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var king = scn.GetFreepsCard("king");
		scn.MoveCompanionsToTable(aragorn, sam, king);
		scn.MoveCardsToHand(herb);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(herb);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(bilbospipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(aragornspipe);

		assertAttachedTo(bilbospipe, frodo);
		assertAttachedTo(aragornspipe, aragorn);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, herb);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnePipePlayedAndTheNextDeclinedAddsOneBurdenThoughAnotherPipeIsAlreadyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var herb = scn.GetFreepsCard("herb");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		// A pipe that was in play before the event: pipes to spot will be 2, pipes played 1.
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToDiscard(gandalfspipe, frodospipe);
		scn.MoveCardsToHand(herb);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(herb);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbospipe, gafferspipe));
		scn.FreepsChooseCardBPFromSelection(bilbospipe);
		scn.FreepsChooseCard(sam);

		assertAttachedTo(bilbospipe, sam);

		// A second pipe could be played, on Frodo; "up to 2" lets it be left.
		int shufflesBeforeTheSecondLook = scn.GetFreepsDeckShuffleCount();
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gafferspipe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		// The deck was looked through a second time, and is shuffled again.
		assertTrue(scn.GetFreepsDeckShuffleCount() > shufflesBeforeTheSecondLook);
		assertInZone(Zone.DECK, gafferspipe);
		assertNotAttachedTo(gafferspipe, frodo);
		// 2 for the event + 1 for the pipe
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, herb);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoMoreThanTwoPipesArePlayedAndTheodenKeepsTheBurdensAway() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var herb = scn.GetFreepsCard("herb");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(aragorn, sam, gandalf, theoden);
		scn.MoveCardsToHand(herb);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(herb);
		scn.FreepsDismissRevealedCards();
		// Five pipes of three cultures, each with someone to bear it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragornspipe, bilbospipe, gafferspipe, gandalfspipe, frodospipe));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(gandalfspipe);

		assertAttachedTo(gandalfspipe, gandalf);
		// 2 for the event + 0 for Gandalf's Pipe
		assertEquals(2, scn.GetTwilight());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragornspipe, bilbospipe, gafferspipe, frodospipe));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(frodospipe);

		assertAttachedTo(frodospipe, frodo);
		assertEquals(3, scn.GetTwilight());
		// Three pipes that could still be played are left where they were.
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, aragornspipe, bilbospipe, gafferspipe);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, herb);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithPipesInTheDrawDeckThePlayerMayPlayNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(herb);

		scn.StartGame();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(herb);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragornspipe, bilbospipe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, aragornspipe, bilbospipe);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, herb);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoPipeInTheDrawDeckItIsStillPlayedAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var toby = scn.GetFreepsCard("toby");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToDiscard(aragornspipe, bilbospipe, gafferspipe, gandalfspipe, frodospipe);
		scn.MoveCardsToHand(herb);

		scn.StartGame();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, toby);
		assertTrue(scn.FreepsPlayAvailable(herb));

		scn.FreepsPlayCard(herb);
		// The deck is looked through all the same.
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, herb, aragornspipe, bilbospipe, gafferspipe, gandalfspipe, frodospipe);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayableInTheFellowshipPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(herb);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(herb));

		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DECK, aragornspipe);
		assertFalse(scn.FreepsPlayAvailable(herb));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
