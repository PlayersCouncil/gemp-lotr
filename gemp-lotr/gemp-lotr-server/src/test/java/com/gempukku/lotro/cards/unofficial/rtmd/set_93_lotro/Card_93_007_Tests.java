package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_93_007_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: companion, part of the 4-companion baseline
					put("boromir", "1_96"); // Boromir: companion, part of the 4-companion baseline
					put("legolas", "1_50"); // Legolas: companion, part of the 4-companion baseline
					put("gimli", "1_13"); // Gimli: companion, part of the 4-companion baseline
					put("guard", "1_7"); // Dwarf Guard: 5th companion, pushes the count over 4
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally, who is no companion
					put("filler1", "1_3"); // Axe Strike: a card to draw
					put("filler2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: companion, part of the 4-companion baseline
					put("boromir", "1_96"); // Boromir: companion, part of the 4-companion baseline
					put("legolas", "1_50"); // Legolas: companion, part of the 4-companion baseline
					put("gimli", "1_13"); // Gimli: companion, part of the 4-companion baseline
					put("guard", "1_7"); // Dwarf Guard: 5th companion, pushes the count over 4
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally, who is no companion
					put("filler1", "1_3"); // Axe Strike: a card to draw
					put("filler2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_7"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_7
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Each time your fellowship moves, the Shadow player may draw a card for each
		 * companion you can spot over 4.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_7", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowDrawsForCompanionsOver4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");

		// 5 companions + Frodo = 6 total, that's 2 over 4
		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli, guard);

		scn.StartGame();

		int shadowHandBefore = scn.GetShadowHandCount();
		assertEquals(0, scn.GetFreepsHandCount());

		// Move — trigger fires
		scn.FreepsPass();

		// Optional trigger — accept. One offer for both cards.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowAcceptOptionalTrigger();

		int shadowHandAfter = scn.GetShadowHandCount();

		// 6 companions total, 2 over 4 = draw 2
		assertEquals(shadowHandBefore + 2, shadowHandAfter);
		// Nothing for the modifier's owner
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OneCardIsDrawnForTheFifthCompanionAtEachMoveAndAnAllyIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");

		// Frodo and four more: a Hobbit, two Men, an Elf and a Dwarf, of four cultures.
		// Bounder is a sixth character and no companion.
		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The first move of the turn, from site 1 to site 2
		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// 5 companions, 1 over 4: one card, and it is the Shadow player's
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Shadow player's reconcile in the regroup phase draws the rest of his deck: two cards are
		// put back on it to be drawn.
		scn.SkipToMovementDecision();
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"));
		int hand = scn.GetShadowHandCount();
		assertEquals(2, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The second move, from site 2 to site 3: the offer is made again
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningDrawsNothingAndTheOwnerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		// The draw is the Shadow player's alone: the modifier's owner is not offered it in his place
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoDrawWith4OrFewerCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");

		// 3 companions + Frodo = 4 total, exactly 4 = 0 over 4
		scn.MoveCompanionsToTable(aragorn, boromir, legolas);

		scn.StartGame();

		int shadowHandBefore = scn.GetShadowHandCount();

		// Move — trigger should NOT fire (0 cards to draw)
		scn.FreepsPass();

		// Nobody is asked about a draw of no cards
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		int shadowHandAfter = scn.GetShadowHandCount();

		assertEquals(shadowHandBefore, shadowHandAfter);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OwnerGatingDoesNotFireForShadowOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli, guard);

		scn.StartGame();

		int shadowHandBefore = scn.GetShadowHandCount();
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsPass();

		// The fellowship of six that moved is not the owner's: neither player is offered a draw
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		int shadowHandAfter = scn.GetShadowHandCount();

		// Shadow owns the mod — trigger doesn't fire
		assertEquals(shadowHandBefore, shadowHandAfter);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerTwoGivesPlayerOneTheDrawWhenHisOwnFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 owns the modifier, and his own fellowship is the five: Frodo and these four
		var mod = scn.GetShadowCard("mod");
		scn.MoveCompanionsToTable(scn.GetShadowCard("aragorn"), scn.GetShadowCard("boromir"),
				scn.GetShadowCard("legolas"), scn.GetShadowCard("gimli"));

		scn.StartGame();

		// Player 1's turn: the fellowship that moves is Frodo alone, and it is not the owner's
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Player 2's turn: player 1 is the Shadow player now
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The reconcile drew player 1's whole deck into his hand: two cards are put back to be drawn
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"));
		int hand = scn.GetFreepsHandCount();
		assertEquals(2, scn.GetFreepsDeckCount());
		int ownerHand = scn.GetShadowHandCount();
		int ownerDeck = scn.GetShadowDeckCount();

		scn.ShadowPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// One card for the one companion over 4, and none for the modifier's owner
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(ownerHand, scn.GetShadowHandCount());
		assertEquals(ownerDeck, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
