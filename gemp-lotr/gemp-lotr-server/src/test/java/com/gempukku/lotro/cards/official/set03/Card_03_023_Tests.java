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
					put("nenya", "3_23");
					put("galadriel", "1_45");   // Galadriel, Lady of Light: Elf ally; a start-of-turn heal of site-6 allies and a fellowship ability used only on purpose
					put("galadriel2", "10_11"); // Galadriel, Lady Redeemed: Elf companion; a fellowship/regroup ability used only on purpose
					put("arwen", "3_7");        // Arwen, Elven Rider: an Elf companion of another title; her response is only for the Ring-bearer
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an Elf ally of another title; her text needs Freeps' site 3 in play

					// For the hand: a companion, a possession, an event and a minion, none played.
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion
					put("sword", "1_160");      // Uruk-hai Sword: [isengard] possession
					put("defiance", "1_37");    // Defiance: [elven] skirmish event
					put("runner", "1_178");     // Goblin Runner: [moria] minion
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
		 * Vitality: +1
		 * Game Text: Bearer must be Galadriel.<br>At the start of each regroup phase, you may discard up to 2 cards from hand to draw the same number of cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nenya");

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
		assertInZone(Zone.HAND, filler1, filler2, filler3);
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

	@Test
	public void CanBePlayedOnlyOnGaladrielAndGoesOnTheAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(nenya);

		scn.StartGame();

		// An Elf companion and an Elf ally of other titles, and the Ring-bearer: no bearer.
		assertInZone(Zone.HAND, nenya);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nenya));

		scn.MoveAlliesToTable(galadriel);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(nenya));
		// Galadriel is the one bearer it may go on, so it goes on her unasked.
		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedOnGaladrielTheCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel2");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(galadriel, arwen);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(nenya);

		scn.StartGame();

		assertInZone(Zone.HAND, nenya);
		assertTrue(scn.FreepsPlayAvailable(nenya));
		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseAnyTwoCardsFromHandAreDiscardedToDrawTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var defiance = scn.GetFreepsCard("defiance");
		var runner = scn.GetFreepsCard("runner");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var galadriel2 = scn.GetFreepsCard("galadriel2");

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel);
		scn.AttachCardsTo(galadriel, nenya);
		scn.SetFreepsHand(legolas, sword, defiance, runner);
		scn.SetFreepsDrawDeck(arwen, silinde, galadriel2);

		assertAttachedTo(nenya, galadriel);
		assertInZone(Zone.HAND, legolas, sword, defiance, runner);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());

		// No minion: the Shadow phase leads straight to the regroup phase.  Nothing is offered as the Shadow phase starts.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nenya));
		scn.FreepsAcceptOptionalTrigger();

		// Any card of the hand, whatever its type or side; one or two of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, sword, defiance, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, sword);

		// Two discarded, the top two cards of the deck drawn.
		assertInZone(Zone.DISCARD, legolas, sword);
		assertInZone(Zone.HAND, defiance, runner, arwen, silinde);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedItLeavesTheHandAndTheDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var defiance = scn.GetFreepsCard("defiance");
		var runner = scn.GetFreepsCard("runner");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel);
		scn.AttachCardsTo(galadriel, nenya);
		scn.SetFreepsHand(defiance, runner);
		scn.SetFreepsDrawDeck(arwen, silinde);

		assertAttachedTo(nenya, galadriel);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nenya));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.HAND, defiance, runner);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, arwen, silinde);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
