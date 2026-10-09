package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInHand;
import static org.junit.Assert.*;

public class Card_94_028_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: inert deck card, one of the two cards offered
					put("merry", "4_310");  // Merry, Learned Guide: inert deck card, the other card offered
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_28", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: inert deck card, one of the two cards offered
					put("merry", "4_310");  // Merry, Learned Guide: inert deck card, the other card offered
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_28"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: inert deck card, one of the two cards offered
					put("merry", "4_310");  // Merry, Learned Guide: inert deck card, the other card offered
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Player 1 has the modifier, a card that draws three, and a draw deck deep enough to tell the top two from the rest
	protected VirtualTableScenario GetFreepsDeeperDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");    // Gimli, Son of Gloin: the Dwarf companion who exerts for Delving (vitality 3)
					put("delving1", "1_6");  // Delving: "Fellowship: Exert a Dwarf companion to draw 3 cards.", cost 1
					put("delving2", "1_6");  // second copy
					// Six cards that lie in the draw deck and do nothing there
					put("aragorn", "1_89");  // Aragorn, Ranger of the North
					put("merry", "4_310");   // Merry, Learned Guide
					put("sam", "1_311");     // Sam, Son of Hamfast
					put("legolas", "1_50");  // Legolas, Greenleaf
					put("boromir", "1_96");  // Boromir, Lord of Gondor
					put("arwen", "1_30");    // Arwen, Daughter of Elrond
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_28", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_28
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: Each time you are about to draw a card (except when reconciling), instead your opponent looks at
		 * the 2 top cards from your draw deck, chooses 1 for you to take into hand, and the other is placed beneath
		 * your draw deck.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_28", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(5, mod.getBlueprint().getIntensity());
	}

	@Test
	public void OpponentPicksWhichOfTheTop2IsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		// Also covers: an ordinary (non-reconcile) card-effect draw played during the regroup phase is still
		// filtered, unlike a reconcile draw -- see ReconciliationDrawsAreNotFiltered.
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToTopOfDeck(merry, aragorn);

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1, // lint-ok: a draw by no card, so that nothing but the draw is on the board
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertTrue(scn.ShadowDecisionAvailable("Choose the card"));
		assertEquals(2, scn.ShadowGetBPChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry));

		scn.ShadowChooseCardBPFromSelection(merry);

		assertInHand(merry);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(aragorn, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithoutTheModTheTopCardIsDrawnWithNoChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToTopOfDeck(merry, aragorn);

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1, // lint-ok: a draw by no card, so that nothing but the draw is on the board
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(aragorn);
		assertEquals(merry, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOnlyOneCardLeftItIsDrawnWithNoChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToDiscard(merry);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetFreepsDeckCount());

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1, // lint-ok: a draw by no card, so that nothing but the draw is on the board
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReconciliationDrawsAreNotFiltered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(merry, aragorn);
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToStay();

		// The Free Peoples player's reconcile draws back up to hand size normally -- "(except when reconciling)" --
		// with no opponent decision at all, unlike an ordinary card-effect draw during the same phase.
		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(merry, aragorn);
		// The turn is over: player 2's fellowship phase.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsDrawsAreUnfiltered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToTopOfDeck(merry, aragorn);

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1, // lint-ok: a draw by no card, so that nothing but the draw is on the board
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ScopingWhenShadowOwnsModShadowDrawsAreFiltered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetShadowCard("aragorn");
		var merry = scn.GetShadowCard("merry");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(merry, aragorn);
		scn.SkipToPhase(Phase.SHADOW);

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P2, // lint-ok: a draw by no card, so that nothing but the draw is on the board
				new DrawCardsEffect(null, VirtualTableScenario.P2, 1));

		assertTrue(scn.FreepsDecisionAvailable("Choose the card"));
		assertEquals(2, scn.FreepsGetBPChoiceCount());

		scn.FreepsChooseCardBPFromSelection(merry);

		assertInHand(merry);
		assertEquals(aragorn, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADrawOfThreeCardsIsThreeLooksEachAtTheNextTwoCardsOfTheOwnersDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsDeeperDeckScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var delving1 = scn.GetFreepsCard("delving1");
		var delving2 = scn.GetFreepsCard("delving2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving1);
		scn.MoveCardsToDiscard(delving2);
		scn.StartGame();
		// The owner's draw deck, top first: Aragorn, Merry, Sam, Legolas, Boromir, Arwen.
		scn.MoveCardsToTopOfDeck(arwen, boromir, legolas, sam, merry, aragorn);

		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		int opponentsDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsPlayAvailable(delving1));
		scn.FreepsPlayCard(delving1);

		// First card: the opponent, and not the owner, is shown the top two and no third, and picks exactly one.
		assertTrue(scn.ShadowDecisionAvailable("Choose the card"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.ShadowGetBPChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(merry);
		assertInHand(merry);
		assertEquals(sam, scn.GetFreepsTopOfDeck());
		assertEquals(aragorn, scn.GetFreepsBottomOfDeck());

		// Second card: the next two, and not the one just put beneath.
		assertTrue(scn.ShadowDecisionAvailable("Choose the card"));
		assertEquals(2, scn.ShadowGetBPChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, legolas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, aragorn));
		scn.ShadowChooseCardBPFromSelection(sam);
		assertEquals(legolas, scn.GetFreepsBottomOfDeck());

		// Third card.
		assertTrue(scn.ShadowDecisionAvailable("Choose the card"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, arwen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, legolas));
		scn.ShadowChooseCardBPFromSelection(arwen);

		// One card taken for each of the three draws and none besides; the three passed over lie beneath in
		// the order they went there, and the opponent's own deck was never touched.
		assertInHand(merry, sam, arwen);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(aragorn, scn.GetFreepsTopOfDeck());
		assertEquals(boromir, scn.GetFreepsBottomOfDeck());
		assertEquals(opponentsDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFifthCardOfAFellowshipPhaseIsNotTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsDeeperDeckScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var delving1 = scn.GetFreepsCard("delving1");
		var delving2 = scn.GetFreepsCard("delving2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving1, delving2);
		scn.StartGame();
		// The owner's draw deck, top first: Aragorn, Merry, Sam, Legolas, Boromir, Arwen.
		scn.MoveCardsToTopOfDeck(arwen, boromir, legolas, sam, merry, aragorn);

		scn.FreepsPlayCard(delving1);
		scn.ShadowChooseCardBPFromSelection(merry);
		scn.ShadowChooseCardBPFromSelection(sam);
		scn.ShadowChooseCardBPFromSelection(arwen);
		// The second Delving and the three cards taken; Aragorn, Legolas and Boromir are left in the deck.
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());

		// The second Delving would draw three more. A card taken this way counts toward the Rule of 4 as a
		// card drawn does: there is room for one.
		scn.FreepsPlayCard(delving2);
		assertTrue(scn.ShadowDecisionAvailable("Choose the card"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, legolas));
		scn.ShadowChooseCardBPFromSelection(aragorn);

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(merry, sam, arwen, aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(boromir, scn.GetFreepsTopOfDeck());
		assertEquals(legolas, scn.GetFreepsBottomOfDeck());
		// Each Delving was paid for.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOpeningHandIsDrawnAsItLiesWithNoChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsDeeperDeckScenario();

		// Nine cards in each deck; this start of the game leaves the opening hands of eight where they were dealt.
		scn.StartGame(false);

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
