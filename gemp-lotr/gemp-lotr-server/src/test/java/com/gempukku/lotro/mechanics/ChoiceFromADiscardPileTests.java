package com.gempukku.lotro.mechanics;

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
 * A choice of cards from a discard pile: the words it is asked in, and whom the game log says chose.
 *
 * The words.  Every choice from a discard pile was asked as "Choose card from discard", whatever its definition
 * said: the `text` that `chooseCardsFromDiscard` has always taken was read and never shown.  A definition that
 * gives the choice words of its own (`text` on `chooseCardsFromDiscard` or `putCardsFromDiscardIntoHand`) is now
 * asked in them; that is what lets a choice whose minimum is 0 say that none need be chosen, in place of a yes/no
 * asked beforehand (Ketura on Q-0143: "let's get rid of the pop-up and just make sure the message displayed to the
 * user is sufficiently clear").  A definition that gives none is asked as before.
 *
 * The log.  "<player> chooses <cards>" named whoever was performing the action, which is not the player who chose
 * when a card has its opponent choose.
 */
public class ChoiceFromADiscardPileTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Hugin, Emissary from Laketown: "Fellowship: Exert Hugin to reveal a Free Peoples card from
					// your discard pile and place it beneath your draw deck."  Its choice has the definition's own
					// words: "Choose card to reveal and put beneath your deck".  Vitality 2.
					put("hugin", "2_24");
					// Stern People: "Regroup: Discard cards from hand equal to the number of cards in an
					// opponent's hand to make the move limit +1 for this turn.  Each Shadow player may take up to 4
					// cards into hand from his or her discard pile.  Discard this condition."  Its choice has no
					// words of its own, and it is the Shadow player who makes it.
					put("stern", "7_251");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples card for a discard pile
					put("guard", "1_7");        // Dwarf Guard: another
					put("runner", "1_178");     // Goblin Runner: a Shadow card for a discard pile
					put("troop", "1_177");      // Goblin Patrol Troop: another
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AChoiceIsAskedInTheWordsItsDefinitionGives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hugin = scn.GetFreepsCard("hugin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveAlliesToTable(hugin);
		scn.MoveCardsToDiscard(aragorn, guard);

		scn.StartGame();

		scn.FreepsUseCardAction(hugin);
		assertEquals(1, scn.GetWoundsOn(hugin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard));
		assertTrue(scn.FreepsDecisionAvailable("Choose card to reveal and put beneath your deck"));
		assertFalse(scn.FreepsDecisionAvailable("Choose card from discard"));

		scn.FreepsChooseCardBPFromSelection(aragorn);
		scn.DismissRevealedCards();
		assertInZone(Zone.DECK, aragorn);
		assertEquals(aragorn, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DISCARD, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AChoiceWithNoWordsOfItsOwnIsAskedAsEveryOtherIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stern = scn.GetFreepsCard("stern");
		scn.MoveCardsToSupportArea(stern);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(runner, troop);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		// The Shadow player's hand is empty, so the cost is a discard of no cards.
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsUseCardAction(stern);

		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void TheLogNamesThePlayerWhoChoseAndNotThePlayerPerformingTheAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stern = scn.GetFreepsCard("stern");
		scn.MoveCardsToSupportArea(stern);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(runner, troop);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MarkGameLog();
		// Stern People is the Free Peoples player's card and that player uses it; the Shadow player chooses.
		scn.FreepsUseCardAction(stern);
		scn.ShadowChooseCardBPFromSelection(runner);

		assertInZone(Zone.HAND, runner);
		assertInZone(Zone.DISCARD, troop);
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays(scn.P2() + " chooses"));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays(scn.P1() + " chooses"));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
