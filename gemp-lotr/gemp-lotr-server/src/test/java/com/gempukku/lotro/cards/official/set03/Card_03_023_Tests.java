package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_23");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Galadriel bearing Nenya; six cards with no text for the hand and the draw deck.
	protected VirtualTableScenario GetNenyaScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nenya", "3_23");
					put("galadriel", "1_45");   // Galadriel, Lady of Light: ally; a start-of-turn heal and a fellowship ability
					for (int i = 1; i <= 6; i++) {
						put("filler" + i, "1_177");     // Goblin Patrol Troop: no text at all
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Nenya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: 1
		 * Game Text: Bearer must be Galadriel.<br>At the start of each regroup phase, you may discard up to 2 cards from hand to draw the same number of cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Nenya", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	// "You may discard up to 2 cards from hand to draw the same number of cards": the discard stands before the
	// "to", and on an optional trigger with nothing else to pay it is of at least one card (the shape of F-0300,
	// Robin Smallburrow 18_111).  The definition asked for 0 to 2: the offer was made with an empty hand, and
	// once taken could be answered with no card, a second way of saying no.
	// F-0448.
	@Test
	public void TheOfferIsNotMadeWithAnEmptyHandAndOnceTakenDiscardsAtLeastOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNenyaScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel);
		scn.AttachCardsTo(galadriel, nenya);

		// The first regroup phase: no card in hand, three in the draw deck and more.
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, and a second regroup phase with three cards in hand.
		scn.SetFreepsHand(filler1, filler2, filler3);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nenya));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsChooseCards(filler1);

		// One discarded and one drawn.
		assertInZone(Zone.DISCARD, filler1);
		assertInZone(Zone.HAND, filler2, filler3);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void NenyaTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
