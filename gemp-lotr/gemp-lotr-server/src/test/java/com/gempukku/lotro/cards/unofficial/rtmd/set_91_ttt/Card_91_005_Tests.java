package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_005_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions to get over the 4 threshold
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("boromir", "1_97");
					put("guard", "1_7");        // Dwarf Guard: a sixth companion, for a second card
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion
					put("filler1", "1_3");
					put("filler2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_5", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions to get over the 4 threshold
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("boromir", "1_97");
					put("guard", "1_7");        // Dwarf Guard: a sixth companion, for a second card
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion
					put("filler1", "1_3");
					put("filler2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_5"
		);
	}

	@Test
	public void ShadowDrawForCompanionsStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_5
		 * Type: MetaSite
		 * Intensity: 2
		 * Game Text: For each companion over 4, each of your opponents may draw a card at the start of their Shadow phase.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_5", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowDrawsWhenCompanionsOver4() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_5: With 5 companions (Frodo + 4 others), shadow should draw 1 card.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// 5 companions total (Frodo + 4), that's 1 over 4
		scn.FreepsPass(); // move to site 2

		// Shadow phase should start with optional trigger to draw
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// One card for the one companion over 4, and none for the modifier's owner
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowDrawsTwoCardsOnOneOfferWithSixCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir", "guard");

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());

		// 6 companions total (Frodo + 5), that's 2 over 4
		scn.FreepsPass();

		// One offer for both cards
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningDrawsNothingAndTheOwnerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		// The draw is the opponent's alone: the modifier's owner is not offered it in his place
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowDoesNotDrawWith4OrFewerCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_5: No trigger when companions <= 4.

		var scn = GetFreepsScenario();

		// Only Frodo + 3 others = 4 companions, not over threshold
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas");
		// A fifth character who is no companion
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());

		scn.FreepsPass(); // move

		// Shadow should NOT get a draw trigger with only 4 companions
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void YouDoNotDrawWhenShadow() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_5: Shadow's own copy should not let Shadow draw.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());

		// 5 companions total (Frodo + 4), that's 1 over 4
		scn.FreepsPass(); // move to site 2

		// Shadow phase should not start with optional trigger to draw
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// Nor is the opponent offered one: this Shadow phase is not his
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnersOpponentDrawsInHisShadowPhaseWhenTheOwnerIsPlayerTwoToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 owns the modifier, and his own fellowship is the five: Frodo and these four
		scn.MoveCompanionsToTable(scn.GetShadowCard("gimli"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("legolas"), scn.GetShadowCard("boromir"));

		scn.StartGame();

		// Player 1's turn: the Shadow phase is the owner's own, and nobody is offered a draw
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Player 2's turn: player 1 is the Shadow player now
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The reconcile drew player 1's whole deck into his hand: put two cards back to be drawn
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		int ownerHand = scn.GetShadowHandCount();
		int ownerDeck = scn.GetShadowDeckCount();

		scn.ShadowPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// One card for the one companion over 4, and none for the modifier's owner
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(ownerHand, scn.GetShadowHandCount());
		assertEquals(ownerDeck, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
