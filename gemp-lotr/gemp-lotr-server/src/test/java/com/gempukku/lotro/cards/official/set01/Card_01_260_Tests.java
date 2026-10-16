package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_260_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("few1", "1_260");
					put("few2", "1_260");

					// With Frodo, six of these make seven companions; none of them is an archer.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf; a skirmish ability used only on purpose
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf, vitality 3; a response that discards 3 cards from hand, used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability used only on purpose
					put("guard1", "1_7");       // Dwarf Guard: vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a companion for Durin's Secret to spot; a fellowship ability used only on purpose
					put("secret", "1_71");      // Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally, so a character who is not a companion

					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text; a minion so that there is a maneuver phase
					put("stand1", "1_63");      // Stand Against Darkness: Maneuver: exert an Elf to discard a [sauron] minion, condition or possession
					put("stand2", "1_63");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheNumberMustBeFewStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Number Must Be Few
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. Plays to your support area.
		 * 	While you can spot 7 companions, the move limit for this turn is -1 (to a minimum of 1).
		 */

		var scn = GetScenario();

		var few = scn.GetFreepsCard("few1");

		assertEquals("The Number Must Be Few", few.getBlueprint().getTitle());
		assertNull(few.getBlueprint().getSubtitle());
		assertFalse(few.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, few.getBlueprint().getSide());
		assertEquals(Culture.SAURON, few.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, few.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(few, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(few, Keyword.SUPPORT_AREA));
		assertEquals(2, few.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideSevenCompanionsItGoesToTheSupportAreaAndMakesTheMoveLimitOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		scn.MoveCardsToHand(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "guard2");

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowPlayAvailable(few));

		scn.ShadowPlayCard(few);
		assertInZone(Zone.SUPPORT, few);
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SixCompanionsAndAnAllyLeaveTheLimitAtTwoAndASeventhCompanionEndsTheTurnAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToSupportArea(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1");
		// An ally is a character and not a companion: six companions and an ally are not seven companions.
		scn.MoveAlliesToTable("celeborn");
		scn.MoveCardsToHand(guard2);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, few);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(guard2);
		assertInZone(Zone.FREE_CHARACTERS, guard2);
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ARaisedLimitIsLoweredByExactlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCardsToSupportArea(few);
		// Frodo and these six are seven companions; Gimli is the Dwarf and Arwen the Elf that Durin's Secret needs.
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "gandalf");
		scn.MoveCardsToHand(secret);

		scn.StartGame();

		// The limit cannot go below 1, so from 2 a penalty of 1 and a penalty of 2 read the same: raise the limit first.
		assertEquals(1, scn.GetMoveLimit()); // 2 - 1
		scn.FreepsPlayCard(secret);
		assertInDiscard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenACompanionDiesInTheRegroupPhaseThePenaltyHoldsAndTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var slayer = scn.GetShadowCard("slayer");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToSupportArea(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "guard2");

		scn.StartGame();
		// Vitality 2: one wound more kills him.
		scn.AddWoundsToChar(guard1, 1);
		assertEquals(1, scn.GetWoundsOn(guard1));
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard1);
		assertInZone(Zone.DEAD, guard1);
		// Six companions now, but a change to the move limit for a turn holds for the whole turn (CR 4.2 "move limit").
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TwoCopiesLeaveTheLimitAtOneAndDiscardingThemDoesNotGiveTheMovesBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few1 = scn.GetShadowCard("few1");
		var few2 = scn.GetShadowCard("few2");
		var troop = scn.GetShadowCard("troop");
		var arwen = scn.GetFreepsCard("arwen");
		var secret = scn.GetFreepsCard("secret");
		var stand1 = scn.GetFreepsCard("stand1");
		var stand2 = scn.GetFreepsCard("stand2");
		scn.MoveCardsToSupportArea(few1, few2);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "gandalf");
		scn.MoveCardsToHand(secret, stand1, stand2);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, few1, few2);
		// 2 - 1 - 1 would be 0: the minimum is 1.
		assertEquals(1, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(1, scn.GetMoveLimit()); // 2 + 1 - 1 - 1

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsPlayCard(stand1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(few1, few2));
		scn.FreepsChooseCard(few1);
		assertInDiscard(few1);
		assertInZone(Zone.SUPPORT, few2);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetMoveLimit());

		scn.ShadowPass();
		scn.FreepsPlayCard(stand2);
		assertInDiscard(few2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		// Both copies gone, and both penalties still stand for the turn: 2 + 1 - 1 - 1, not 3.
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AfterACompanionLeavesThePenaltyHoldsThroughASecondMoveAndASeventhAgainLowersItNoFurther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var secret = scn.GetFreepsCard("secret");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCardsToSupportArea(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "gandalf");
		scn.MoveCardsToHand(secret, guard3);

		scn.StartGame();
		scn.FreepsPlayCard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1

		// The companion lost is only the setting: what is read is the limit afterwards.
		assertInZone(Zone.FREE_CHARACTERS, guard1);
		scn.MoveCardsToDiscard(guard1);
		assertInDiscard(guard1);
		assertEquals(2, scn.GetMoveLimit());

		// Seven companions again: the penalty was given for this turn already, and is not given twice.
		scn.FreepsPlayCard(guard3);
		assertInZone(Zone.FREE_CHARACTERS, guard3);
		assertEquals(2, scn.GetMoveLimit());

		// One move made of two: the fellowship is asked about another, and makes it.
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// The Shadow phase after the second move: the penalty is still there.
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Two moves of two: the turn ends without the question of a third.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The other player's turn: 2 again.
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ThePenaltyIsGivenAgainOnTheNextFreePeoplesTurnAndNotOnItsOwnersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		scn.MoveCardsToSupportArea(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "guard2");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, few);
		assertEquals(1, scn.GetMoveLimit());

		// Player 2's turn: his own Shadow card is not active, and his fellowship is Frodo alone.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		// Player 1's next turn: the seven companions are still there, and the limit is 1 again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.SUPPORT, few);
		assertEquals(1, scn.GetMoveLimit());
	}
}
