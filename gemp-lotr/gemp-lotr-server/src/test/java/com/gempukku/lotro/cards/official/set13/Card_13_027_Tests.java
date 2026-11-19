package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wells", "13_27");
					// Shake Off the Shadow: another [elven] support-area condition of the same player, its abilities
					// used only on purpose; its copies also stand as cards to draw
					put("shake", "8_13");
					put("shake2", "8_13");
					put("shake3", "8_13");
					put("shake4", "8_13");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, home 6F; a fellowship ability used only on purpose
					// Celeborn, Lord of the Galadhrim: Elf companion; a trigger at the start of his skirmishes only
					put("celeborn2", "10_7");
					// Galadriel, Lady Redeemed: Elf companion; a fellowship or regroup ability used only on purpose
					put("galadriel2", "10_11");
					// Arwen, Lady of Rivendell: Elf ally, home 3F; Aragorn +1 and a wound response for him (no Aragorn here)
					put("arwen2", "102_6");
					// Elrond, Lord of Rivendell: an Elf ally of another title; heals allies of site 3 at the start of
					// his owner's turns and has a fellowship ability used only on purpose
					put("elrond", "1_40");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion of another title; an archery ability (no minion here)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Rivendell Waterfall 1_342 as site 3: with a ranger there (Arwen) the move limit is
	// +1 for the turn, so the turn has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wells", "13_27");
					// Shake Off the Shadow: [elven] conditions to draw, abilities used only on purpose
					put("shake", "8_13");
					put("shake2", "8_13");
					put("shake3", "8_13");
					put("shake4", "8_13");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, a ranger; +3 only against a Nazgul
					// Galadriel, Lady of Light: Elf ally, home 6F; heals allies of site 6 at the start of her owner's
					// turns and has a fellowship ability used only on purpose
					put("galadriel", "1_45");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion of another title; an archery ability (no minion here)
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WellsofDeepMemoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Wells of Deep Memory
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the end of your turn, discard this from play if the fellowship did not move more than once this turn.<br>Each time the fellowship moves during the regroup phase, you may draw a card for each of the following you can spot: Arwen, Celeborn, or Galadriel.
		*/

		var scn = GetScenario();

		var wells = scn.GetFreepsCard("wells");

		assertEquals("Wells of Deep Memory", wells.getBlueprint().getTitle());
		assertNull(wells.getBlueprint().getSubtitle());
		assertTrue(wells.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, wells.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, wells.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, wells.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(wells, Keyword.SUPPORT_AREA));
		assertEquals(2, wells.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardedAtTheEndOfATurnInWhichTheFellowshipMovedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wells = scn.GetFreepsCard("wells");
		var shake = scn.GetFreepsCard("shake");
		scn.MoveCardsToSupportArea(wells, shake);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wells, shake);

		// No minion: from the Shadow phase the game goes to the regroup phase, after the one move.
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertInZone(Zone.SUPPORT, wells, shake);

		// The regroup move declined, the turn ends: the Wells is discarded, the other condition stays.
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInDiscard(wells);
		assertInZone(Zone.SUPPORT, shake);
	}

	@Test
	public void StaysAfterATurnOfTwoMovesAndIsDiscardedAfterTheNextTurnOfOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wells = scn.GetFreepsCard("wells");
		var shake = scn.GetFreepsCard("shake");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCardsToSupportArea(wells, shake);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wells, shake);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// The regroup move's draw, for Celeborn, declined.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetMoveCount());

		// Two moves this turn: the Wells stays at its end.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInZone(Zone.SUPPORT, wells, shake);

		// The other player's turn, and then this player's next one, of one move.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.SUPPORT, wells, shake);
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertInZone(Zone.SUPPORT, wells);

		// The two moves of the turn before do not count: discarded.
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInDiscard(wells);
		assertInZone(Zone.SUPPORT, shake);
	}

	@Test
	public void EachRegroupMoveOffersToDrawOneForArwenAndOneForGaladrielAndTheFellowshipPhaseMoveDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var wells = scn.GetFreepsCard("wells");
		var arwen = scn.GetFreepsCard("arwen");
		var galadriel = scn.GetFreepsCard("galadriel");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(wells);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wells);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("shake"), scn.GetFreepsCard("shake2"), scn.GetFreepsCard("shake3"),
				scn.GetFreepsCard("shake4"));
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());

		// The move at the end of the fellowship phase is not answered.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(wells));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A regroup move: one card for Arwen and one for Galadriel; Legolas, an Elf of another title, adds none.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wells));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// At Rivendell Waterfall with a ranger the turn has a third move: offered again, and declined.
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wells));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithArwenCelebornAndGaladrielToSpotItDrawsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wells = scn.GetFreepsCard("wells");
		var arwen2 = scn.GetFreepsCard("arwen2");
		var celeborn2 = scn.GetFreepsCard("celeborn2");
		var galadriel2 = scn.GetFreepsCard("galadriel2");
		scn.MoveCompanionsToTable(celeborn2, galadriel2);
		scn.MoveAlliesToTable(arwen2);
		scn.MoveCardsToSupportArea(wells);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wells);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("shake"), scn.GetFreepsCard("shake2"), scn.GetFreepsCard("shake3"),
				scn.GetFreepsCard("shake4"));
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wells));
		scn.FreepsAcceptOptionalTrigger();
		// An ally Arwen, a companion Celeborn and a companion Galadriel: three, and the fourth card stays.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithCelebornAloneOfTheThreeItDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wells = scn.GetFreepsCard("wells");
		var celeborn = scn.GetFreepsCard("celeborn");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(celeborn, elrond);
		scn.MoveCardsToSupportArea(wells);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wells);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("shake"), scn.GetFreepsCard("shake2"), scn.GetFreepsCard("shake3"),
				scn.GetFreepsCard("shake4"));
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wells));
		scn.FreepsAcceptOptionalTrigger();
		// The ally Celeborn; Elrond and Legolas are Elves of other titles.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoneOfTheThreeToSpotTheRegroupMoveMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wells = scn.GetFreepsCard("wells");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(wells);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wells);
		assertEquals(0, scn.GetFreepsHandCount());

		// A draw of nothing is not offered (Q-0033).
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(wells));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
	}
}
