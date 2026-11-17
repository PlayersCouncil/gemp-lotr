package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("board", "13_131");
					put("eomer", "6_92");       // Éomer, Rohirrim Captain: strength 7, vitality 3; a skirmish ability used only on purpose
					put("eowyn", "5_122");      // Éowyn, Daughter of Éomund: strength 6, vitality 3; a maneuver ability only
					put("theoden", "8_92");     // Théoden, Tall and Proud: strength 7, vitality 3; a twilight discount and a "when killed" trigger
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion of another title; Valiant is its whole text
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingsBoardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: King's Board
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Exert Éomer to make Éowyn or Théoden strength +2.<br><b>Skirmish:</b> Exert Éowyn to make Éomer or Théoden strength +2.<br><b>Skirmish:</b> Exert Théoden to make Éomer or Éowyn strength +2.
		*/

		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");

		assertEquals("King's Board", board.getBlueprint().getTitle());
		assertNull(board.getBlueprint().getSubtitle());
		assertTrue(board.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, board.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, board.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, board.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(board, Keyword.SUPPORT_AREA));
		assertEquals(1, board.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingEomerMakesEowynOrTheodenStrengthPlusTwoForTheSkirmishUntilEomerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eomer, eowyn, theoden, guard);
		scn.MoveCardsToSupportArea(board);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, board);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, runner1},
				new PhysicalCardImpl[]{theoden, runner2});
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éomer"));
		scn.FreepsUseCardAction(board, "Exert Éomer");
		assertEquals(1, scn.GetWoundsOn(eomer));
		// Éowyn in this skirmish or Théoden waiting for his own; not the payer, Éomer, a [rohan] companion of another
		// title, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eowyn, theoden));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eomer, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eowyn);
		assertEquals(8, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Again, for the other one.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éomer"));
		scn.FreepsUseCardAction(board, "Exert Éomer");
		scn.FreepsChooseCard(theoden);
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertEquals(8, scn.GetStrength(eowyn));
		assertEquals(9, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Éomer is exhausted: that ability is no longer offered, though the Guard could exert and the board's other
		// abilities still are.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(board, "Exert Éomer"));
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éowyn"));

		// The next skirmish is a phase of its own: both are back to their printed strength.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(theoden);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(theoden));
	}

	@Test
	public void WithEowynAndTheodenAbsentTheExertionOfEomerBuysNothingAndOnceExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eomer, guard);
		scn.MoveCardsToSupportArea(board);
		scn.AddWoundsToChar(eomer, 1);

		assertEquals(1, scn.GetWoundsOn(eomer));
		scn.StartGame();
		assertInZone(Zone.SUPPORT, board);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);

		// Neither Éowyn nor Théoden is in play: the ability is offered, Éomer exerts, and nobody is strengthened.
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éomer"));
		scn.FreepsUseCardAction(board, "Exert Éomer");
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Exhausted, the payer cannot pay, and the Guard does not pay instead.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(board, "Exert Éomer"));
	}

	@Test
	public void ExertingEowynMakesEomerOrTheodenStrengthPlusTwoForTheSkirmishUntilEowynIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var eowyn = scn.GetFreepsCard("eowyn");
		var eomer = scn.GetFreepsCard("eomer");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eowyn, eomer, theoden, guard);
		scn.MoveCardsToSupportArea(board);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, board);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eomer, runner1},
				new PhysicalCardImpl[]{theoden, runner2});
		scn.FreepsResolveSkirmish(eomer);

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(7, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éowyn"));
		scn.FreepsUseCardAction(board, "Exert Éowyn");
		assertEquals(1, scn.GetWoundsOn(eowyn));
		// Éomer in this skirmish or Théoden waiting for his own; not the payer, Éowyn, a [rohan] companion of another
		// title, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, theoden));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eowyn, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eomer);
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(7, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Again, for the other one.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éowyn"));
		scn.FreepsUseCardAction(board, "Exert Éowyn");
		scn.FreepsChooseCard(theoden);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(9, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Éowyn is exhausted: that ability is no longer offered, though the Guard could exert and the board's other
		// abilities still are.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(board, "Exert Éowyn"));
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éomer"));

		// The next skirmish is a phase of its own: both are back to their printed strength.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(theoden);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(7, scn.GetStrength(theoden));
	}

	@Test
	public void WithEomerAndTheodenAbsentTheExertionOfEowynBuysNothingAndOnceExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveCardsToSupportArea(board);
		scn.AddWoundsToChar(eowyn, 1);

		assertEquals(1, scn.GetWoundsOn(eowyn));
		scn.StartGame();
		assertInZone(Zone.SUPPORT, board);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);

		// Neither Éomer nor Théoden is in play: the ability is offered, Éowyn exerts, and nobody is strengthened.
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éowyn"));
		scn.FreepsUseCardAction(board, "Exert Éowyn");
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Exhausted, the payer cannot pay, and the Guard does not pay instead.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(board, "Exert Éowyn"));
	}

	@Test
	public void ExertingTheodenMakesEomerOrEowynStrengthPlusTwoForTheSkirmishUntilTheodenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(theoden, eomer, eowyn, guard);
		scn.MoveCardsToSupportArea(board);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, board);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eomer, runner1},
				new PhysicalCardImpl[]{eowyn, runner2});
		scn.FreepsResolveSkirmish(eomer);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(eowyn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(board, "Exert Théoden"));
		scn.FreepsUseCardAction(board, "Exert Théoden");
		assertEquals(1, scn.GetWoundsOn(theoden));
		// Éomer in this skirmish or Éowyn waiting for a skirmish of her own; not the payer, Théoden, a [rohan] companion of another
		// title, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, eowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eomer);
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(eowyn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Again, for the other one.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(board, "Exert Théoden"));
		scn.FreepsUseCardAction(board, "Exert Théoden");
		scn.FreepsChooseCard(eowyn);
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(8, scn.GetStrength(eowyn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Théoden is exhausted: that ability is no longer offered, though the Guard could exert and the board's other
		// abilities still are.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(board, "Exert Théoden"));
		assertTrue(scn.FreepsActionAvailable(board, "Exert Éomer"));

		// The next skirmish is a phase of its own: both are back to their printed strength.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(eowyn));
	}

	@Test
	public void WithEomerAndEowynAbsentTheExertionOfTheodenBuysNothingAndOnceExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToSupportArea(board);
		scn.AddWoundsToChar(theoden, 1);

		assertEquals(1, scn.GetWoundsOn(theoden));
		scn.StartGame();
		assertInZone(Zone.SUPPORT, board);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);

		// Neither Éomer nor Éowyn is in play: the ability is offered, Théoden exerts, and nobody is strengthened.
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(board, "Exert Théoden"));
		scn.FreepsUseCardAction(board, "Exert Théoden");
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Exhausted, the payer cannot pay, and the Guard does not pay instead.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(board, "Exert Théoden"));
	}
}
