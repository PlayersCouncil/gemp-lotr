package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_149_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beasts", "7_149");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Desert Soldier: a [raider] Southron Man; an optional trigger at the start of its own skirmish only
					put("soldier", "4_220");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of the Free Peoples player's
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this
					// turn +1.  It lifts the limit to 3, so that a -1 is not hidden by the floor of 1.
					put("secret", "1_71");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf; a skirmish ability used only on purpose
					put("arwen", "3_7");        // Arwen, Elven Rider: the Elf; a response that discards 3 cards from hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatBeastsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Great Beasts
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [raider] Man.<br><b>Regroup:</b> Remove (9) and spot 3 [raider] Men to make the move limit -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beasts");

		assertEquals("Great Beasts", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaWithASouthronToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beasts = scn.GetShadowCard("beasts");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(beasts);
		assertInZone(Zone.HAND, beasts);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(beasts));

		scn.ShadowPlayCard(beasts);
		assertInZone(Zone.SUPPORT, beasts);
		assertNull(beasts.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beasts = scn.GetShadowCard("beasts");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(beasts, axeman);
		assertInZone(Zone.HAND, beasts, axeman);

		scn.StartGame();
		// A [raider] minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(khamul, brigand);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, beasts);
		assertFalse(scn.ShadowPlayAvailable(beasts));

		// An Easterling is a [raider] Man.
		scn.ShadowPlayCard(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.ShadowPlayAvailable(beasts));

		scn.ShadowPlayCard(beasts);
		assertInZone(Zone.SUPPORT, beasts);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionRemoves9ToMakeTheMoveLimitMinus1ForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("gandalf", "gimli", "arwen");
		scn.MoveCardsToHand(secret);

		var beasts = scn.GetShadowCard("beasts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(beasts);
		assertInZone(Zone.SUPPORT, beasts);

		scn.StartGame();
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit()); // 2 + 1

		// Three [raider] Men, two Southrons and an Easterling, who stand unassigned until the regroup phase.
		scn.MoveMinionsToTable(explorer, axeman, soldier);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(1, scn.GetMoveCount());
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(beasts));
		scn.ShadowUseCardAction(beasts);
		assertEquals(1, scn.GetTwilight()); // 10 - 9
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1
		assertInZone(Zone.SUPPORT, beasts);

		// The fellowship may still move a second time, and does.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// The Shadow phase after the second move: the -1 has outlasted the regroup phase it was used in.
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The other player's turn: his limit is 2, and nothing of the -1 is left.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void IsNotOfferedWithTwoRaiderMenAndCardsThatAreNotBesideThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beasts = scn.GetShadowCard("beasts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(beasts);

		scn.StartGame();
		// Two [raider] Men; beside them a [raider] minion who is no Man, a Man who is not [raider], and the Free
		// Peoples player's own Man.
		scn.MoveMinionsToTable(explorer, axeman, khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Khamul is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(beasts));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseNorWithOnly8ToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beasts = scn.GetShadowCard("beasts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(beasts);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, axeman, soldier);

		// The maneuver phase, with three [raider] Men and (9) in the pool.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(9);
		scn.FreepsPass();
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(beasts));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The regroup phase with (8): one short of the cost.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(8);
		scn.FreepsPass();
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(beasts));

		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(beasts));
		assertEquals(2, scn.GetMoveLimit());
	}
}
