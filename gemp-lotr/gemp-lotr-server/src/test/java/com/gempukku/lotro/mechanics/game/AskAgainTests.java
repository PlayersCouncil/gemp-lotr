package com.gempukku.lotro.mechanics.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Asking a waiting decision again (TurnProcedure.askAgain, reached through DefaultLotroGame.askAgain): the decision a
 * game process asked directly is withdrawn, the state-based checks are made and whatever they stack is played out,
 * and the process asks again of the board as it now stands.  Nothing in an ordinary game does this; a scenario's
 * sandbox does, after its board has been changed by hand (Stop, edit, Start).  The board here is changed with the
 * rig's bare zone moves (MoveCardToZone), which leave a pending decision exactly as it was, so that what the new
 * decision offers is the asking again's doing and nobody else's.
 *
 * Cards: Legolas 1_50 (2 twilight, vitality 3; nothing to do in the fellowship phase), Boromir 1_97 with Athelas
 * 1_94 ("Fellowship: Discard Athelas to heal a companion or remove a Shadow condition"), and Goblin Runner 1_178 as
 * a minion to have a maneuver phase at all.
 */
public class AskAgainTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("legolas", "1_50");
					put("boromir", "1_97");
					put("athelas", "1_94");
					put("runner", "1_178");
				}}
		);
	}

	@Test
	public void AFellowshipPromptAskedAgainOffersACardAddedToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var legolas = scn.GetFreepsCard("legolas");
		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(legolas));
		AwaitingDecision before = scn.FreepsGetAwaitingDecision();

		//Changed under the waiting decision, which goes on offering what it offered
		scn.MoveCardToZone(legolas, Zone.HAND);
		scn.SetTwilight(3);
		assertFalse(scn.FreepsPlayAvailable(legolas));
		assertTrue(scn.game().canAskAgain());

		scn.game().askAgain();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertNotSame(before, scn.FreepsGetAwaitingDecision());
		assertTrue(scn.FreepsPlayAvailable(legolas));
		assertEquals(3, scn.GetTwilight());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		//And it is an ordinary decision: answering it plays the card
		scn.FreepsPlayCard(legolas);
		assertInZone(legolas, Zone.FREE_CHARACTERS);
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void AManeuverPromptAskedAgainAsksTheSamePlayerAndKeepsThePassCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var runner = scn.GetShadowCard("runner");
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		//One pass in a row: the Shadow player is asked
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.game().askAgain();
		//The same player, not the next of the order
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.AwaitingFreepsManeuverPhaseActions());

		//Still one pass in a row: the Shadow player's pass is the second, and the phase is over
		scn.ShadowPass();
		assertNotEquals(Phase.MANEUVER, scn.GetCurrentPhase());
	}

	@Test
	public void ACharacterLeftWithNoVitalityDiesBeforeThePromptReturns() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.AddWoundsToChar(legolas, 3);
		//Nothing has looked at him yet
		assertInZone(legolas, Zone.FREE_CHARACTERS);

		scn.game().askAgain();

		assertInZone(legolas, Zone.DEAD);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADecisionAskedInsideAnActionIsNotAskedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(legolas, boromir);
		scn.AttachCardsTo(boromir, athelas);
		scn.StartGame();
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.game().askAgain();

		scn.FreepsUseCardAction(athelas);
		//Somewhere inside Athelas's effect (which to do, or which companion to heal): the action is on the stack
		AwaitingDecision inside = scn.FreepsGetAwaitingDecision();
		assertNotNull(inside);
		assertFalse(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.game().canAskAgain());
		assertThrows(IllegalStateException.class, () -> scn.game().askAgain());
		//And the decision is untouched
		assertSame(inside, scn.FreepsGetAwaitingDecision());
	}

	@Test
	public void AnOrdinaryGameIsNotChangedByBeingAbleTo() throws DecisionResultInvalidException, CardNotFoundException {
		//The same game twice, one of them asked again with nothing changed: they go on alike
		var scn = GetScenario();
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardToZone(legolas, Zone.HAND);
		scn.StartGame(false);
		scn.MoveCardToZone(legolas, Zone.HAND);
		scn.game().askAgain();
		scn.game().askAgain();
		assertTrue(scn.FreepsPlayAvailable(legolas));
		scn.FreepsPlayCard(legolas);
		scn.FreepsPass();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
	}

	private static void assertInZone(com.gempukku.lotro.game.PhysicalCardImpl card, Zone zone) {
		assertEquals(zone, card.getZone());
	}
}
