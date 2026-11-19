package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_91_012_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion
					put("lorien", "1_53");      // Lorien Elf: an Elf companion who is not unique and not an Archer; to play, spot an Elf (cheat him in)
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, who is no companion; a fellowship ability used only on purpose
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an Elf ally, who is no companion; has the text of the owner's site 3 once it is laid
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf; stronger only beside 3 Ents
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion of vitality 2; to play, spot a Dwarf
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("saved", "8_20");       // Saved From the Fire: Fellowship event, spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take up to 3 cards of his culture from the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_12", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion
					put("lorien", "1_53");      // Lorien Elf: an Elf companion who is not unique and not an Archer; to play, spot an Elf (cheat him in)
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, who is no companion; a fellowship ability used only on purpose
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an Elf ally, who is no companion; has the text of the owner's site 3 once it is laid
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf; stronger only beside 3 Ents
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion of vitality 2; to play, spot a Dwarf
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_12"
		);
	}

	@Test
	public void MoveLimitPlusOneStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_12
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: While you can spot a Dwarf companion, an Elf companion, and Gandalf, your move limit is +1.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_12", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void MoveLimitIncreasedWithAllThree() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Move limit +1 when spotting Dwarf, Elf, and Gandalf.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf");

		scn.StartGame();

		// Normal move limit is 2, should be 3 with the modifier
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MoveLimitUnchangedWithoutDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Missing a Dwarf — move limit should remain normal.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("legolas", "gandalf");
		// A Dwarf who is no companion, beside two companions who are no Dwarves
		scn.MoveAlliesToTable("grimir");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MoveLimitUnchangedWithoutElf() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Missing an Elf — move limit should remain normal.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "gandalf");
		// An Elf who is no companion, beside two companions who are no Elves
		scn.MoveAlliesToTable("silinde");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MoveLimitUnchangedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Missing Gandalf — move limit should remain normal.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "legolas");
		// A companion of Gandalf's culture who is not Gandalf
		scn.MoveCompanionsToTable("avenger");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowCopyDoesNotAffectFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: "your move limit" — Shadow's copy should not affect FP move limit.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf");

		scn.StartGame();

		// Shadow's copy should not grant +1 to FP
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingGandalfRaisesTheMoveLimitToThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		// The Elf here is another one than Legolas, who is the Elf of the other tests
		scn.MoveCompanionsToTable("gimli", "lorien");
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Given in the fellowship phase, it is the turn's and not that phase's: read again after the move
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheDwarfDiesTheMoveLimitIsStillThreeAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var guard = scn.GetFreepsCard("guard");
		var slayer = scn.GetShadowCard("slayer");
		// The Dwarf Guard is the only Dwarf companion
		scn.MoveCompanionsToTable("guard", "legolas", "gandalf");

		scn.StartGame();
		// Vitality 2: one wound more kills him.
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		// No Dwarf companion is left, and the limit is still 3: a change to the move limit holds for the whole turn
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void TheBonusHoldsThroughASecondMoveAfterTheDwarfHasGoneAndEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var guard = scn.GetFreepsCard("guard");
		var saved = scn.GetFreepsCard("saved");
		// The Dwarf Guard is the only Dwarf companion
		scn.MoveCompanionsToTable("guard", "legolas", "gandalf");
		scn.MoveCardsToHand(saved);

		scn.StartGame();

		assertEquals(3, scn.GetMoveLimit());

		// The Dwarf is placed in the dead pile in the fellowship phase, the first of the turn
		scn.FreepsPlayCard(saved);
		scn.FreepsChooseCard(guard);
		// The search of the deck: shown first, and nothing taken.
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DEAD, guard);
		// No Dwarf companion is left, and the limit is still 3
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second move
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		scn.FreepsChooseToMove();

		// The Shadow phase after it: the bonus is still there
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// So after two moves the fellowship is asked about a third
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();

		// The other player's turn: 2 again
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheBonusIsGivenAgainOnTheOwnersNextTurnAndNotOnTheOtherPlayersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf");

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		// Player 2's turn: the three are not his, and the bonus of the turn before has ended with it
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The owner's next turn: the three are still there, and the limit is 3 again and no more
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOwnersMoveLimitIsRaisedOnHisOwnTurnWhenHeIsPlayerTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 owns the modifier, and the three are in his own fellowship
		scn.MoveCompanionsToTable(scn.GetShadowCard("gimli"), scn.GetShadowCard("legolas"), scn.GetShadowCard("gandalf"));

		scn.StartGame();

		// Player 1's turn
		assertEquals(2, scn.GetMoveLimit());

		// Player 2's turn
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
