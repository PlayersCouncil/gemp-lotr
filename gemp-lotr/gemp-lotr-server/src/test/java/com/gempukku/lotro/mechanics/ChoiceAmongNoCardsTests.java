package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A choice among cards that an earlier effect set aside (the definition word `ChooseArbitraryCards`), when that
 * effect set none aside: nobody is shown a list of no cards to choose from, and none is chosen.
 *
 * This is the engine's way with every other choice of cards: with no candidate the choice is passed over, and
 * what was to be done with the card chosen is done with none.  Found on War-beacon 103_35, whose reveal of an
 * empty draw deck was followed by a prompt to take cards into hand with no card in it (F-0338).
 */
public class ChoiceAmongNoCardsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Birthday Present: a [shire] condition.  "Fellowship: Add a burden to choose 2 [shire] events
					// with different card titles from your discard pile.  Choose an opponent and make him or her
					// choose 1 of those cards for you to take into hand."
					put("present", "10_104");
					put("stout", "1_315");      // Stout and Sturdy: a [shire] event
					put("stealth", "1_298");    // Hobbit Stealth: a [shire] event
					put("sword", "1_299");      // Hobbit Sword: a [shire] card that is no event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithCardsSetAsideEachPlayerIsAskedInTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var present = scn.GetFreepsCard("present");
		var stout = scn.GetFreepsCard("stout");
		var stealth = scn.GetFreepsCard("stealth");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(present);
		scn.MoveCardsToDiscard(stout, stealth, sword);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(present);
		assertEquals(1, scn.GetBurdens());

		// The two events are set aside, and he is asked to choose 2 of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stout, stealth));
		assertFalse(scn.FreepsHasBPChoice(sword));
		scn.FreepsChooseCards(stout, stealth);

		// Then his opponent is asked to choose 1 of those.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stout, stealth));
		scn.ShadowChooseCards(stealth);

		assertInHand(stealth);
		assertInDiscard(stout, sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCardSetAsideNeitherPlayerIsAskedAndNoneIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var present = scn.GetFreepsCard("present");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(present);
		// A [shire] card that is no event: nothing in the discard pile is set aside.
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();

		// The cost can be paid, so the ability is offered.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(present));
		scn.FreepsUseCardAction(present);
		assertEquals(1, scn.GetBurdens());

		// Neither the choice of 2 nor the opponent's choice of 1 among them is put to anybody.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(sword);
	}
}
