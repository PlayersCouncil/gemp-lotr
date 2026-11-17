package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paths", "7_69");

					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] Sméagol; to play, add a burden; a skirmish ability only
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Sméagol; an assignment ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is no Sméagol; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: exerts for Sleep, Caradhras; a strength bonus only
					put("sleep", "1_84");       // Sleep, Caradhras: Fellowship event, exert Gandalf to discard every condition
					put("lord", "1_21");        // Lord of Moria: another Free Peoples condition in the support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SecretPathsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Secret Paths
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Sméagol and add 2 threats.<br>The move limit for this turn is +1.<br>Discard this condition at the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("paths");

		assertEquals("Secret Paths", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsSpottedThenAddsTwoThreatsAndRaisesTheMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		// A Ring-bound companion who is no Sméagol; with Frodo, room for 3 threats.
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(paths, smeagol);
		assertInHand(paths, smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(paths));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(paths));

		scn.FreepsPlayCard(paths);
		assertInZone(Zone.SUPPORT, paths);
		assertEquals(2, scn.GetThreats());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		// Frodo and Sméagol: room for 2 threats.
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(paths);
		assertInHand(paths);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(paths));
		scn.FreepsPlayCard(paths);

		assertInZone(Zone.SUPPORT, paths);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTwoThreatsCannotBothBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToHand(paths, aragorn);
		assertInHand(paths, aragorn);

		scn.StartGame();
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		// Three companions and 2 threats: only 1 more can be added, and the cost is judged payable in full.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(paths));

		// A fourth companion: room for 2 more.
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.FreepsPlayAvailable(paths));

		scn.FreepsPlayCard(paths);
		assertInZone(Zone.SUPPORT, paths);
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFellowshipMayMoveAThirdTimeThisTurnAndTheConditionIsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var lord = scn.GetFreepsCard("lord");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(paths);
		assertInHand(paths);
		assertInZone(Zone.SUPPORT, lord);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(paths);
		assertEquals(3, scn.GetMoveLimit());

		// The first move, then no minions, and the regroup phase's second move.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());
		assertInZone(Zone.SUPPORT, paths);

		// A third move is offered, and made.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// Still there in the regroup phase after the third move; the turn then ends, and it is discarded with it.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SUPPORT, paths);
		scn.SkipToMovementDecision();
		scn.PassReconcile(VirtualTableScenario.P1);
		scn.PassReconcile(VirtualTableScenario.P2);

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInDiscard(paths);
		assertInZone(Zone.SUPPORT, lord);
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheMoveLimitStaysRaisedForTheTurnAfterTheConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var sleep = scn.GetFreepsCard("sleep");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(smeagol, gandalf);
		scn.MoveCardsToHand(paths, sleep);
		assertInHand(paths, sleep);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(paths);
		assertInZone(Zone.SUPPORT, paths);
		assertEquals(3, scn.GetMoveLimit());

		// Sleep, Caradhras discards every condition, this one with them, in the same fellowship phase.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(paths);

		// CR 4.2 "move limit": a modification for a turn is in effect for the whole turn.
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
