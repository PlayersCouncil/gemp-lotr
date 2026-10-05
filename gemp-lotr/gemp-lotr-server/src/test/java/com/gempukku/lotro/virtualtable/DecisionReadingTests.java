package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the checks that read what a decision is offering by the card it concerns (an optional
 * trigger, an assignment, one of several actions of a card), and the count of how often a deck has been shuffled.
 */
public class DecisionReadingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Three for the Elven-Kings: "Each time the fellowship moves, you may spot an [elven] or [gandalf]
					// ring to place a card from hand beneath your draw deck and draw a card..."  Its definition words
					// the prompt itself, so the game does not offer it under the card's name.
					put("three", "101_12");
					put("elrond", "1_40");          // Elrond, Lord of Rivendell: ally, home site 3. Fellowship: Exert Elrond to draw a card.
					put("vilya", "3_27");           // Vilya: an [elven] ring, borne by Elrond
					put("aragorn", "1_89");         // Aragorn, Ranger of the North

					// Library of Rivendell.  "Fellowship: Stack your tale or artifact from play or from hand here to
					// draw a card."  The game offers stacking from play and stacking from hand as two actions.
					put("library", "101_11");
					put("redbook", "2_113");        // Red Book of Westmarch: a tale, in the support area
					put("staff", "2_22");           // Gandalf's Staff: an artifact

					put("gaffer", "1_291");         // The Gaffer: the ally Bag End plays from the draw deck
					put("deck1", "1_3");            // an event, neither a tale nor an artifact
					put("deck2", "1_3");
					put("deck3", "1_3");

					put("runner", "1_178");         // Goblin Runner
				}},
				// Site 1 is Bag End: "Fellowship: Exert a Hobbit to play The Gaffer from your draw deck."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OptionalTriggerCheckGoesByTheCardWhateverThePromptSays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(three);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		// Nothing optional is being asked in the fellowship phase.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(three));

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		// The prompt does not name the card.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Three for the Elven-Kings"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(three));
		// The trigger is the condition's, not the ring's or its bearer's, and not the Shadow player's to answer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(vilya));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(three));
	}

	@Test
	public void AssignmentTargetCheckReadsTheDecisionEachPlayerIsFacing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Not an assignment decision.
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aragorn));

		scn.SkipToAssignments();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		// An ally away from home (Elrond's is site 3, and this is site 2) cannot be assigned.
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(aragorn));

		// The Free Peoples player assigns nobody, and the Shadow player is asked.
		scn.FreepsDeclineAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(runner));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(elrond));
	}

	@Test
	public void OneOfSeveralActionsOfACardIsFoundAndTakenByItsWording() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var redbook = scn.GetFreepsCard("redbook");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCardsToSupportArea(library, redbook);
		scn.MoveCardsToHand(staff);

		scn.StartGame();
		// What stacking draws is a card that cannot itself be stacked.
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("deck1"), scn.GetFreepsCard("deck2"));

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsActionAvailable(library));
		assertTrue(scn.FreepsActionAvailable(library, "from play"));
		assertTrue(scn.FreepsActionAvailable(library, "FROM HAND"));
		assertFalse(scn.FreepsActionAvailable(library, "from the discard pile"));
		// The wording alone is not enough: the action has to be that card's.
		assertFalse(scn.FreepsActionAvailable(staff, "from hand"));
		assertFalse(scn.ShadowActionAvailable(library, "from hand"));

		assertInZone(Zone.HAND, staff);
		assertInZone(Zone.SUPPORT, redbook);

		// The staff is the only tale or artifact in hand, so it is stacked without a choice.
		scn.FreepsUseCardAction(library, "from hand");
		assertInZone(Zone.STACKED, staff);
		assertInZone(Zone.SUPPORT, redbook);

		// With the hand emptied of artifacts only the other action is left.
		assertTrue(scn.FreepsActionAvailable(library, "from play"));
		assertFalse(scn.FreepsActionAvailable(library, "from hand"));

		scn.FreepsUseCardAction(library, "from play");
		assertInZone(Zone.STACKED, redbook);
		assertFalse(scn.FreepsActionAvailable(library));
	}

	@Test
	public void ShuffleCountRisesWhenADeckIsShuffledAndNotWhenACardIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();

		int freeps = scn.GetFreepsDeckShuffleCount();
		int shadow = scn.GetShadowDeckShuffleCount();
		assertInZone(Zone.DECK, gaffer);

		// Drawing a card leaves the deck in its order.
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(freeps, scn.GetFreepsDeckShuffleCount());

		// Bag End plays The Gaffer from the draw deck, which is then reshuffled (CR 4.2 "draw deck").
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gaffer);
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(freeps + 1, scn.GetFreepsDeckShuffleCount());
		// The other player's deck is untouched.
		assertEquals(shadow, scn.GetShadowDeckShuffleCount());
	}
}
