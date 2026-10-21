package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inside", "19_24");
					put("inside2", "19_24");

					put("guard1", "5_83");        // Household Guard: a [rohan] Man companion, strength 6, vitality 3; Valiant and nothing else
					put("guard2", "5_83");
					// [rohan] Men who are allies. Their home is site 4 of the Towers block, so on this path they
					// never fight. Guma has a fellowship ability and Hlafwine a skirmish ability, used only on purpose.
					put("guma", "4_277");         // Guma, Plains Farmer: strength 4
					put("hlafwine", "4_281");     // Hlafwine, Village Farmhand: strength 4
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, and a Man only while 3 [rohan]
					// companions (or Eowyn) can be spotted. He stands beside one Household Guard at most.
					put("merry", "103_85");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, [gondor]; strength 8

					put("runner1", "1_178");      // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("brigand", "4_10");       // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BroughtDownfromInsideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Brought Down from Inside
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Strength: +1
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	At the start of each skirmish you may transfer this condition to a [rohan] Man.
		 * 	At the end of each turn, if bearer did not win a skirmish this turn, return this condition to hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("inside");

		assertEquals("Brought Down from Inside", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnlyOnARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var inside = scn.GetFreepsCard("inside");
		var guard = scn.GetFreepsCard("guard1");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(inside);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(inside));

		scn.FreepsPlayCard(inside);

		// A [rohan] Man, companion or ally. Not Merry ([rohan], a Hobbit while only he and the Guard are
		// [rohan] companions), not Aragorn (a Man, [gondor]).
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, frodo));
		scn.FreepsChooseCard(guma);

		assertAttachedTo(inside, guma);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfEachSkirmishItMayBeTransferredToAnotherRohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inside = scn.GetFreepsCard("inside");
		var guard = scn.GetFreepsCard("guard1");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(guma, hlafwine);
		scn.AttachCardsTo(guma, inside);
		assertAttachedTo(inside, guma);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);

		// Not at the start of the maneuver phase, nor of the assignment phase.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(inside));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(inside));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{guard, savage});
		var pool = scn.GetTwilight();

		// Aragorn's skirmish: no [rohan] Man is in it, and the offer is made all the same.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(inside));

		scn.FreepsAcceptOptionalTrigger();

		// The Guard (a companion, assigned to another skirmish) and Hlafwine (an ally) may take it. Merry is
		// no Man and Aragorn is not [rohan]; Guma has it already.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, hlafwine));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, merry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// The +1 goes with it, and a transfer by a card's text costs nothing.
		assertAttachedTo(inside, guard);
		assertNotAttachedTo(inside, guma);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 kills the Runner.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// The next skirmish of the same turn, the bearer's own: offered again, now from the Guard to the two allies.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(inside));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guma, hlafwine));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, merry, aragorn));
		scn.FreepsChooseCard(hlafwine);

		assertAttachedTo(inside, hlafwine);
		assertNotAttachedTo(inside, guard);
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NoTransferIsOfferedWhenNoOtherRohanManCouldTakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inside = scn.GetFreepsCard("inside");
		var guard = scn.GetFreepsCard("guard1");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner1");

		// Beside the bearer: a [rohan] companion who is no Man, and a Man who is not [rohan].
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.AttachCardsTo(guard, inside);
		assertAttachedTo(inside, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(inside));
		assertAttachedTo(inside, guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ReturnsToHandAtTheEndOfATurnInWhichItsBearerLostASkirmishThoughAnotherRohanManWonOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inside = scn.GetFreepsCard("inside");
		var inside2 = scn.GetFreepsCard("inside2");
		var bearer = scn.GetFreepsCard("guard1");
		var other = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner1");
		var brigand = scn.GetShadowCard("brigand");

		scn.MoveCompanionsToTable(bearer, other, aragorn);
		// The card is not unique: a second copy on the other Guard, who wins.
		scn.AttachCardsTo(bearer, inside);
		scn.AttachCardsTo(other, inside2);
		assertAttachedTo(inside, bearer);
		assertAttachedTo(inside2, other);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{other, runner}, new PhysicalCardImpl[]{bearer, brigand});

		// The other Guard's skirmish. Each copy offers its own transfer; both are declined: nothing moves.
		scn.FreepsResolveSkirmish(other);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(inside));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(inside2));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(inside, bearer);
		assertAttachedTo(inside2, other);

		// The other Guard's 6 + 1 beats the Runner's 5 and kills it: a [rohan] Man has won a skirmish this turn.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// The bearer's 6 + 1 loses to the Brigand's 8.
		scn.FreepsResolveSkirmish(bearer);
		scn.FreepsDeclineOptionalTrigger();
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bearer));

		// Still borne when the fellowship decides not to move again.
		scn.SkipToMovementDecision();
		assertAttachedTo(inside, bearer);
		assertInZone(Zone.ATTACHED, inside);
		assertAttachedTo(inside2, other);

		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// The turn is over: the loser's copy is in its owner's hand, and the winner's stays on him.
		assertInZone(Zone.HAND, inside);
		assertNotAttachedTo(inside, bearer);
		assertAttachedTo(inside2, other);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void StaysWhenItsBearerWonASkirmishAndReturnsAtTheEndOfTheNextTurnInWhichHeWinsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inside = scn.GetFreepsCard("inside");
		var bearer = scn.GetFreepsCard("guard1");
		var other = scn.GetFreepsCard("guard2");
		var runner = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(bearer, other);
		scn.AttachCardsTo(bearer, inside);
		assertAttachedTo(inside, bearer);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bearer, runner);

		// The bearer keeps it and wins: 6 + 1 against the Runner's 5.
		scn.FreepsResolveSkirmish(bearer);
		scn.FreepsDeclineOptionalTrigger();
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// The turn ends and the condition is still on him.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertAttachedTo(inside, bearer);

		// The other player's turn goes by; it is not that turn's end that asks.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(inside, bearer);

		// Its owner's next turn: no minion, no skirmish. Last turn's win is not this turn's.
		scn.SkipToOtherPlayersTurn();
		assertInZone(Zone.HAND, inside);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ReturnsToHandWhenTransferredFromAManWhoWonToAManWhoWonNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inside = scn.GetFreepsCard("inside");
		var winner = scn.GetFreepsCard("guard1");
		var idle = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(winner, idle, aragorn);
		scn.AttachCardsTo(winner, inside);
		assertAttachedTo(inside, winner);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{winner, runner1}, new PhysicalCardImpl[]{aragorn, runner2});

		// The bearer wins his skirmish with the condition on him.
		scn.FreepsResolveSkirmish(winner);
		scn.FreepsDeclineOptionalTrigger();
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner1);
		assertAttachedTo(inside, winner);

		// At the start of Aragorn's skirmish it goes to the other Guard, the only other [rohan] Man, who
		// fights nobody this turn.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(inside));
		scn.FreepsAcceptOptionalTrigger();
		assertAttachedTo(inside, idle);
		scn.PassSkirmishActions();

		scn.SkipToMovementDecision();
		assertAttachedTo(inside, idle);

		// "Bearer" is the Man who bears it when the turn ends, and he did not win a skirmish.
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());
		assertInZone(Zone.HAND, inside);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void StaysWhenTransferredToAManWhoHadAlreadyWonASkirmishThatTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inside = scn.GetFreepsCard("inside");
		var idle = scn.GetFreepsCard("guard1");
		var winner = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(idle, winner, aragorn);
		scn.AttachCardsTo(idle, inside);
		assertAttachedTo(inside, idle);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{winner, runner1}, new PhysicalCardImpl[]{aragorn, runner2});

		// The Guard who does not bear it wins his skirmish: 6 against the Runner's 5.
		scn.FreepsResolveSkirmish(winner);
		scn.FreepsDeclineOptionalTrigger();
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner1);
		assertAttachedTo(inside, idle);

		// At the start of Aragorn's skirmish it goes to him, the only other [rohan] Man.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(inside));
		scn.FreepsAcceptOptionalTrigger();
		assertAttachedTo(inside, winner);
		scn.PassSkirmishActions();

		// Its bearer when the turn ends did win a skirmish this turn: it stays.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertAttachedTo(inside, winner);
	}
}
