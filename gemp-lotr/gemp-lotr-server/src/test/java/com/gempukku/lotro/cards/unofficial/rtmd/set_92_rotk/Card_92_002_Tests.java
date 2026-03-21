package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_002_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Grima, Wormtongue: Isengard Man minion, twilight 2, str 4, vit 3
		put("grima", "4_154");
		// Saruman's Ambition: Isengard condition, support area
		put("condition1", "1_133");
		put("condition2", "1_133");
		// Goblin Runner for a second minion (needed to survive to next turn)
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_2"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_2", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_2
		 * Type: MetaSite
		 * Game Text: Shadow: Once per turn, exert your [Isengard] Man to play an [Isengard] condition from your draw deck.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_2", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ShadowCanExertIsengardManToPlayConditionFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: Exert Isengard Man to play Isengard condition from draw deck.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveMinionsToTable(grima);
		// Leave condition in draw deck

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// Shadow phase: the ability should be available
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));

		// Should exert Grima (cost)
		assertEquals(1, scn.GetWoundsOn(grima));

		// Should be choosing an Isengard condition from deck — dismiss the revealed cards
		scn.ShadowDismissRevealedCards();

		scn.ShadowChooseCard(condition1);

		// Condition should now be in support area
		assertInZone(Zone.SUPPORT, condition1);
	}

	@Test
	public void CannotUseWithoutIsengardMan() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: No Isengard Man on table = ability not available.

		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");
		// Don't put Grima on table, only runner (Orc, not Man)
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.FreepsPass(); // move

		// Ability should NOT be available without an Isengard Man
		assertFalse(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
	}

	@Test
	public void CannotUseTwiceInSameShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "Once per turn" — cannot use twice in the same shadow phase.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");
		var condition1 = scn.GetShadowCard("condition1");

		scn.MoveMinionsToTable(grima);

		scn.StartGame();
		scn.FreepsPass(); // move

		// First use: should succeed
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCard(condition1);

		// Second use in same phase: should NOT be available
		assertFalse(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
	}

	@Test
	public void CannotUseInDoubleMoveShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "Once per turn" — after using in the first shadow phase,
		// if FP moves again during regroup, the second shadow phase should NOT allow use.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");
		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(grima, runner);

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// First shadow phase: use the ability
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowPassCurrentPhaseAction();

		// Skip to regroup and choose to move again
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();

		// Second shadow phase (after double move): should NOT be available
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
	}

	@Test
	public void CanUseAgainOnNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "Once per turn" — after a full turn cycle, should be usable again.

		var scn = GetShadowScenario();

		var grima = scn.GetShadowCard("grima");

		scn.MoveMinionsToTable(grima);

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		// Shadow phase: use the ability
		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));
		scn.ShadowDismissRevealedCards();

		// Skip the rest of this turn and start next turn
		scn.SkipToSite(3);

		// We should be in fellowship phase at site 3
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		//scn.MoveCardsToTopOfDeck(condition1);
		scn.MoveMinionsToTable(grima);
		scn.FreepsPass(); // move to site 4

		// Shadow phase of new turn: ability should be available again
		assertTrue(scn.ShadowActionAvailable(scn.GetShadowCard("mod")));
	}

	@Test
	public void FreepsCopyDoesNotWorkForShadow() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_2: "your [Isengard] Man" — Freeps copy should not give Shadow the ability.

		var scn = GetFreepsScenario();

		var grima = scn.GetShadowCard("grima");

		scn.MoveMinionsToTable(grima);

		scn.StartGame();
		scn.FreepsPass(); // move

		// Shadow should NOT have the ability from Freeps' copy
		assertFalse(scn.ShadowActionAvailable(scn.GetFreepsCard("mod")));
	}
}
