package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class Card_01_365_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_365");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_365");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Aragorn signet, vitality 3; her text is about Nazgul
					put("sam", "1_311");        // Sam, Son of Hamfast: Aragorn signet, vitality 4; nothing of his is used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Frodo signet, vitality 4; a strength bonus only
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: Frodo signet
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected DeckValidationScenario GetDeckScenario() {
		return new DeckValidationScenario();
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Aragorn, King in Exile
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Ranger</b>.<br>At the start of each of your turns, you may heal another companion who has the Aragorn signet.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("King in Exile", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void AtTheStartOfTheTurnAnotherCompanionWithTheAragornSignetMayBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, arwen, sam, gandalf);
		// Two wounds on Arwen, so that one heal is told from two.
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(aragorn, 1);

		// The turn begins, and the game stops at the offer.
		scn.StartGame();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));

		// Arwen and Sam have the Aragorn signet.  Gandalf has Frodo's, and Aragorn is not "another companion".
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered
	// (F-0349).  The old at/ test (TimingAtTest.playMultipleOptionalEffectsInOrder) had this offer made with nobody
	// wounded.
	@Test
	public void WithNoOtherCompanionOfTheAragornSignetWoundedTheHealIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, arwen, sam, gandalf);
		// The wounded are a companion of another signet and Aragorn himself: nobody his text could heal.
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedItHealsNobodyAndIsOfferedAgainAtHisOwnersNextTurnAndNotAtTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's turn begins with his fellowship phase: nothing is offered and nobody is healed.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));

		// The owner's second turn: offered again, and taken.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.FreepsAcceptOptionalTrigger();

		// Arwen, the one wounded candidate, is healed of one wound without a choice being put.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// CR 4.2 "building your deck": a draw deck may hold four copies of Aragorn, King in Exile, or two of it and two
	// of Aragorn, Ranger of the North, but not four of each, since they have the same title.  Only the objection to
	// copies is read: these decks are no more than the named cards, and draw others for their size.
	@Test
	public void ADrawDeckHoldsFourCopiesOfTheTitleAragornWhateverTheSubtitle() throws DecisionResultInvalidException, CardNotFoundException {
		var dvs = GetDeckScenario();

		assertFalse(dvs.hasMaxSameNameError(dvs.validate(dvs.buildDeck(Map.of("1_365", 4), null, 0, 0))));
		assertFalse(dvs.hasMaxSameNameError(dvs.validate(dvs.buildDeck(Map.of("1_365", 2, "1_89", 2), null, 0, 0))));
		// One more of the other subtitle is a fifth Aragorn.
		assertTrue(dvs.hasMaxSameNameError(dvs.validate(dvs.buildDeck(Map.of("1_365", 2, "1_89", 3), null, 0, 0))));
		assertTrue(dvs.hasMaxSameNameError(dvs.validate(dvs.buildDeck(Map.of("1_365", 4, "1_89", 4), null, 0, 0))));
	}
}
