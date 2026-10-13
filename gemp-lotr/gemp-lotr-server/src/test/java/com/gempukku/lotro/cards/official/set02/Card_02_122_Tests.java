package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_122_Tests
{

	protected VirtualTableScenario GetDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "2_122");
					put("axe", "1_3");          // Axe Strike: an event with no part in this card
					put("axe2", "1_3");         // a second one, so that a draw of one card is told from a draw of two
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Gandalf, The Grey Pilgrim
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: At the start of each of your turns, you may draw a card.
		*/

		var scn = GetDrawScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("The Grey Pilgrim", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void AtTheStartOfHisOwnersTurnGandalfOffersADrawAndTakingItDrawsTheTopCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		// The start of the turn, before the fellowship phase: the two Axe Strikes are the whole draw deck.
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		scn.MoveCardsToTopOfDeck(axe2, axe);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.HAND, axe);
		assertInZone(Zone.DECK, axe2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheDrawLeavesHandAndDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.DECK, axe);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDrawIsOfferedAgainAtHisOwnersNextTurnAndNotAtTheOtherPlayersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();

		// The other player's turn: Gandalf is not active, and player 1 is asked nothing as it starts.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		// Back to his owner's turn: the offer is made again, before the fellowship phase.  The reconcile of turn 1
		// drew both Axe Strikes, so they are put back on the deck for the draw to find.
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.MoveCardsToTopOfDeck(axe2, axe);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, deck);
		assertInZone(Zone.DECK, axe);

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.HAND, axe);
		assertInZone(Zone.DECK, axe2);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Ketura on Q-0029: an optional draw is still offered with no card in the draw deck ("a bit of a chance for
	// them to *realize* they have no cards in their deck ... I'd say let it ask with 0 cards in deck"), and her
	// later rule that an optional action which would do nothing is withheld (Q-0034) left that standing.  The old
	// at/ test (TimingAtTest.playMultipleOptionalEffectsInOrder) read this offer with an empty draw deck.
	@Test
	public void AtTheStartOfTheTurnTheDrawIsOfferedEvenWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("axe"), scn.GetFreepsCard("axe2"));

		scn.StartGame();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		// Taken, it draws nothing, and the turn goes on.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
