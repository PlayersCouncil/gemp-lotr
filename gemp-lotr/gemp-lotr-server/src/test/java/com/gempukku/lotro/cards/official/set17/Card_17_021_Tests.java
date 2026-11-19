package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "17_21");
					put("pipe2", "17_21");
					put("ship", "18_27");       // Ship of Smoke: a [gandalf] pipeweed condition, cost 0; its trigger needs Gandalf, who is never in play here
					put("toby", "1_305");       // Old Toby: a [shire] pipeweed possession in the support area; its draw is never offered (it is never played)
					put("bilbospipe", "1_285"); // Bilbo's Pipe: a [shire] pipe possession on a Hobbit, no pipeweed; its ability is never used

					// In the draw deck.
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion
					put("sleep", "1_84");       // Sleep, Caradhras: a [gandalf] event
					put("leaf", "17_106");      // Halfling Leaf: a [shire] condition

					put("weary", "4_212");      // Weary: an [isengard] condition on a companion; its trigger needs an [isengard] tracker, and there is none
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongstemmedPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Long-stemmed Pipe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Pipeweed</b>. When you play this, you may search your deck for a [gandalf] card and place it in your discard pile.<br><b>Regroup:</b> Discard another pipeweed from play to discard a condition. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pipe");

		assertEquals("Long-stemmed Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedItSearchesTheDeckForAGandalfCardAndPutsItInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var avenger = scn.GetFreepsCard("avenger");
		var sleep = scn.GetFreepsCard("sleep");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToHand(pipe);
		assertInHand(pipe);

		scn.StartGame();
		assertInZone(Zone.DECK, avenger, sleep, leaf);
		assertEquals(0, scn.GetFreepsDiscardCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(pipe);
		assertInZone(Zone.SUPPORT, pipe);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pipe));
		scn.FreepsAcceptOptionalTrigger();

		// The whole deck is shown first, the [shire] card with it; then the [gandalf] cards of two types may be
		// chosen and the [shire] card is not among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaf, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, true));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, true));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaf));
		scn.FreepsChooseCardBPFromSelection(sleep);

		assertInDiscard(sleep);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, avenger, leaf);
		// A deck that was looked through is shuffled (CR 4.2 "draw deck"), and the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheSearchMayBeDeclinedAndTheDeckIsNotShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var avenger = scn.GetFreepsCard("avenger");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToHand(pipe);
		assertInHand(pipe);

		scn.StartGame();
		assertInZone(Zone.DECK, avenger, sleep);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(pipe);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pipe));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, pipe);
		assertInZone(Zone.DECK, avenger, sleep);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherPipeweedPlayedAfterItDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var ship = scn.GetFreepsCard("ship");
		scn.MoveCardsToSupportArea(pipe);
		scn.MoveCardsToHand(ship);
		assertInZone(Zone.SUPPORT, pipe);
		assertInHand(ship);

		scn.StartGame();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(ship);
		assertInZone(Zone.SUPPORT, ship);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pipe));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsAnotherPipeweedToDiscardAConditionAndThenItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var pipe2 = scn.GetFreepsCard("pipe2");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var weary = scn.GetShadowCard("weary");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(pipe, pipe2, ship, toby);
		scn.AttachCardsTo(frodo, bilbospipe);
		scn.AttachCardsTo(frodo, weary);
		assertInZone(Zone.SUPPORT, pipe, pipe2, ship, toby);
		assertAttachedTo(bilbospipe, frodo);
		assertAttachedTo(weary, frodo);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		// Another pipeweed from play, a condition or a possession, of either culture; not this one, and not a pipe.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe2, ship, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, bilbospipe));
		scn.FreepsChooseCard(toby);
		assertInDiscard(toby);

		// A condition of either side, this one among them; not a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary, pipe2, ship, pipe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbospipe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weary);

		assertInDiscard(weary);
		// This copy is discarded; the other copy stays.
		assertInDiscard(pipe);
		assertInZone(Zone.SUPPORT, pipe2, ship);
		assertAttachedTo(bilbospipe, frodo);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherPipeweedInPlayTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var weary = scn.GetShadowCard("weary");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(pipe);
		scn.AttachCardsTo(frodo, bilbospipe);
		scn.AttachCardsTo(frodo, weary);
		// A pipeweed in hand is not in play.
		scn.MoveCardsToHand(toby);
		assertInZone(Zone.SUPPORT, pipe);
		assertAttachedTo(bilbospipe, frodo);
		assertAttachedTo(weary, frodo);
		assertInHand(toby);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pipe));
		assertAttachedTo(weary, frodo);
	}
}
