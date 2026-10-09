package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_91_002_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("freepscard", "1_3");     // Axe Strike: a Free Peoples event
					put("shadowcard", "1_151");   // Uruk Savage: a Shadow minion
					put("gimli", "1_13");         // Gimli, Son of Gloin: a companion, and the Dwarf who exerts for Delving
					put("delving", "1_6");        // Delving.  Fellowship: Exert a Dwarf companion to draw 3 cards.
					put("elrond", "1_40");        // Elrond, Lord of Rivendell.  Fellowship: Exert Elrond to draw a card.

					// More cards to draw.  Dwarf Guard, so that each card named above is the only one of its
					// kind in the deck: a choice from a draw deck is answered by kind of card.
					put("deck1", "1_7");
					put("deck2", "1_7");
					put("deck3", "1_7");
					put("deck4", "1_7");
					put("deck5", "1_7");
					put("deck6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_2", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("freepscard", "1_3");     // Axe Strike: a Free Peoples event
					put("shadowcard", "1_151");   // Uruk Savage: a Shadow minion

					// Dwarf Guard: enough cards that the reconcile of turn 1 leaves a draw deck
					put("deck1", "1_7");
					put("deck2", "1_7");
					put("deck3", "1_7");
					put("deck4", "1_7");
					put("deck5", "1_7");
					put("deck6", "1_7");
					put("deck7", "1_7");
					put("deck8", "1_7");
					put("deck9", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_2"
		);
	}

	@Test
	public void SearchDeckStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_2
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: At the start of each of your fellowship phases, you may take any card into hand from your draw deck.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_2", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsGetsOptionalSearchAtStartOfFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_2: "At the start of each of your fellowship phases, you may take any card
		// into hand from your draw deck."
		// FP should get an optional trigger at the start of fellowship to search deck.
		var scn = GetFreepsScenario();

		var freepsCard = scn.GetFreepsCard("freepscard");
		var shadowCard = scn.GetFreepsCard("shadowcard");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(11, scn.GetShadowDeckCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// At the start of the fellowship phase, FP should have an optional trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// "Any card": an event, a minion, a companion and an ally, of either side; and every card of the deck
		assertTrue(scn.FreepsHasCardChoiceAvailable(freepsCard, shadowCard, gimli, elrond));
		assertEquals(11, scn.FreepsGetCardChoiceCount());
		// one card, and none need be taken once the deck is being looked through
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(freepsCard);

		assertInZone(Zone.HAND, freepsCard);
		assertInZone(Zone.DECK, shadowCard);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(10, scn.GetFreepsDeckCount());
		// The deck that was looked through is shuffled; the other player's is left alone
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(11, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLeavesHandAndDeckAsTheyWereAndNobodyElseIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Declined before the deck was looked through: nothing taken, and no shuffle
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		// "You may" is the modifier's owner and nobody else: the other player is asked nothing
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferComesAgainAtTheOwnersNextFellowshipPhaseAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var shadowCard = scn.GetFreepsCard("shadowcard");

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// Turn 1: the offer is taken, the deck looked through, and no card chosen
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDeclineChoosing();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 2: the other player's fellowship phase is not "yours", and nobody is offered anything at its start
		scn.SkipToOtherPlayersTurn();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Turn 3: the owner's next fellowship phase offers it again
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		// Wherever the reconciles left it, the Uruk Savage is in the draw deck for this
		scn.MoveCardsToTopOfDeck(shadowCard);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(shadowCard);

		assertInZone(Zone.HAND, shadowCard);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferIsStillMadeWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		// The whole draw deck is in hand before the game begins
		scn.FreepsDrawCards(11);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(11, scn.GetFreepsHandCount());

		// An optional look through an empty draw deck is still offered, and finds nothing
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(11, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCardTakenIsOneOfTheFourOfTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var freepsCard = scn.GetFreepsCard("freepscard");
		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(delving);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetFreepsDeckCount());

		// The first card of the phase: taken at its start, which is not "at the start of your turn"
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(freepsCard);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetFreepsDeckCount());

		// The second, third and fourth
		scn.FreepsPlayCard(delving);
		// 2 - 1 for the event + 3 drawn
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());

		// A draw is now a fifth, and does not happen, though Elrond has exerted for it
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("Q-0176: 'any card', with no 'reveal': is the card taken named to the other player in the game log, as a card taken by its type is?")
	public void TheCardTakenIsNotMadeKnownToTheOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var shadowCard = scn.GetFreepsCard("shadowcard");

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.MarkGameLog();
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(shadowCard);

		// The text says neither "reveal" nor what kind of card, so nothing has to be shown: the other
		// player has no decision, and the game log does not name the card
		assertInZone(Zone.HAND, shadowCard);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(shadowCard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowDoesNotGetSearchAtStartOfFreepsFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_2: Should only trigger for the FP player's fellowship phase.
		var scn = GetShadowScenario();

		scn.StartGame();

		// Shadow should NOT get an optional trigger from their copy
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowOwnerIsOfferedTheSearchAtTheStartOfHisOwnFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var savage = scn.GetShadowCard("shadowcard");

		scn.StartGame();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Player 2's turn: the fellowship phase is now his
		scn.SkipToOtherPlayersTurn();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		// Wherever the reconcile left it, the Uruk Savage is in his draw deck for this
		scn.MoveCardsToTopOfDeck(savage);
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowAcceptOptionalTrigger();
		// What is left of his own draw deck is what he chooses from
		assertEquals(deck, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage));
		scn.ShadowChooseCard(savage);

		assertInZone(Zone.HAND, savage);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
