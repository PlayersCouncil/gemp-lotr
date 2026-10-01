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

public class Card_94_028_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: inert deck card, one of the two cards offered
		put("merry", "4_310"); // Merry, Learned Guide: inert deck card, the other card offered
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_28", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_28"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
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

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1,
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertTrue(scn.ShadowDecisionAvailable("Choose the card"));
		assertEquals(2, scn.ShadowGetBPChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry));

		scn.ShadowChooseCardBPFromSelection(merry);

		assertInHand(merry);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(aragorn, scn.GetFreepsBottomOfDeck());
	}

	@Test
	public void WithoutTheModTheTopCardIsDrawnWithNoChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToTopOfDeck(merry, aragorn);

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1,
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(aragorn);
		assertEquals(merry, scn.GetFreepsTopOfDeck());
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

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1,
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(aragorn);
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
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsDrawsAreUnfiltered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToTopOfDeck(merry, aragorn);

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P1,
				new DrawCardsEffect(null, VirtualTableScenario.P1, 1));

		assertFalse(scn.ShadowDecisionAvailable("Choose the card"));
		assertInHand(aragorn);
	}

	@Test
	public void ScopingWhenShadowOwnsModShadowDrawsAreFiltered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetShadowCard("aragorn");
		var merry = scn.GetShadowCard("merry");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(merry, aragorn);
		scn.SkipToPhase(Phase.SHADOW);

		scn.carryOutEffectInPhaseActionByPlayer(VirtualTableScenario.P2,
				new DrawCardsEffect(null, VirtualTableScenario.P2, 1));

		assertTrue(scn.FreepsDecisionAvailable("Choose the card"));
		assertEquals(2, scn.FreepsGetBPChoiceCount());

		scn.FreepsChooseCardBPFromSelection(merry);

		assertInHand(merry);
		assertEquals(aragorn, scn.GetShadowBottomOfDeck());
	}
}
