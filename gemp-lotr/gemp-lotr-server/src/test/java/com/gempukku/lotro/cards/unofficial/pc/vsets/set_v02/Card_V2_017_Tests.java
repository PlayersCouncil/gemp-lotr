package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_017_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("die", "102_17");

					// Aragorn, Heir of Elendil: [gondor], strength 8, vitality 4, not valiant; "Defender +1" is
					// its whole text.
					put("aragorn", "4_109");
					// Aragorn, Ranger of the North: as the other but for "Maneuver: Exert Aragorn to make him
					// defender +1 until the regroup phase", which is how he comes by one wound by legal play.
					put("ranger", "1_89");
					// Aragorn, Defender of Rohan: the Aragorn of another culture, and valiant; strength 8,
					// vitality 4.  His text does nothing unless the Ring-bearer is assigned to a skirmish.
					put("rohanaragorn", "17_93");
					// Elendil, The Tall: a [gondor] Man like Aragorn, strength 8, vitality 5; his abilities need
					// an artifact.
					put("elendil", "9_32");

					put("guard1", "5_83");       // Household Guard: valiant [rohan] Man, strength 6; "Valiant" is its whole text
					put("guard2", "5_83");
					// Elven Defender: valiant [elven] Elf, strength 5; +2 of his own only while an [elven] token
					// can be spotted, and no test has one.
					put("defender", "68_9");
					put("rider", "4_286");       // Rider of Rohan: a [rohan] Man who is not valiant, strength 5; his ability needs a mount
					// Guma, Plains Farmer: a [rohan] Man who is an ally, strength 4, vitality 2, not valiant; a
					// fellowship ability.
					put("guma", "4_277");
					// Northern Signal-fire, Flame of Calenhad: a [rohan] possession of the support area.
					// "Maneuver: Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of
					// your other Men gain each of the unloaded keywords (except race) on that Man."  How an
					// ally comes to be valiant in play: none is in print.
					put("flame", "103_86");

					put("marksman", "1_176");    // Goblin Marksman: strength 7, vitality 1; "Archer" is its whole text
					// Morgul Slayer: strength 7, vitality 2; "Regroup: Exert this minion to wound a companion
					// (except the Ring-bearer)."
					put("slayer", "3_93");
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, no text
					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");

					// Fierce in Despair: a Shadow regroup event, cost 7.  "Spot 2 [raider] Men to discard a
					// companion (except the Ring-bearer)."
					put("despair", "7_148");
					// Desert Nomad: a [raider] Man, strength 9; damage +1 only while 2 threats can be spotted.
					put("nomad1", "7_132");
					put("nomad2", "7_132");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IWillDieasOneofThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: I Will Die as One of Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Aragorn three times to make each valiant companion strength +2 until the regroup phase.
		 * If Aragorn dies during this turn, make each valiant companion strength +1 for the rest of the turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("die");

		assertEquals("I Will Die as One of Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAragornThreeTimesToMakeEachValiantCompanionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, guard, defender, rider);
		scn.MoveCardsToHand(die);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(defender, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(rider));
		// Frodo's 3 + 1 for the Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(13, scn.GetStrength(troop));

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.HAND, die);
		assertTrue(scn.FreepsPlayAvailable(die));

		scn.FreepsPlayCard(die);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, die);
		// Vitality 4 with 3 wounds: Aragorn is exhausted and alive.  Nobody else exerts.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The valiant companions, a Man and an Elf, with nothing asked.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(defender));
		// Not the companions who are not valiant, nor the minion.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(13, scn.GetStrength(troop));

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceAragornCanExertOnlyTwiceThoughAnotherGondorManCouldExertThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("ranger");
		var elendil = scn.GetFreepsCard("elendil");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(aragorn, elendil, guard);
		scn.MoveCardsToHand(die);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(die));

		// Aragorn exerts once for his own ability.  Vitality 4 with 1 wound: he can exert twice and no more.
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetVitality(aragorn));
		// Elendil, with vitality 5 and no wounds, could exert three times.
		assertEquals(5, scn.GetVitality(elendil));
		assertEquals(0, scn.GetWoundsOn(elendil));
		assertInZone(Zone.HAND, die);
		assertFalse(scn.FreepsPlayAvailable(die));
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishesAndEndsAtTheRegroupPhaseWhenAragornLives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		scn.MoveCompanionsToTable(aragorn, guard, defender);
		scn.MoveCardsToHand(die);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		// This Aragorn is [rohan], and valiant himself.
		assertTrue(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
		assertTrue(scn.FreepsPlayAvailable(die));

		scn.FreepsPlayCard(die);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(defender));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { defender, runner1 },
				new PhysicalCardImpl[] { guard, runner2 }
		);

		// The Elven Defender's 5 + 2 beats a Runner's 5, which his 5 alone would not.
		scn.FreepsResolveSkirmish(defender);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner1);
		assertEquals(0, scn.GetWoundsOn(defender));

		// One skirmish phase has ended, and the Household Guard fights the next still at 8.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(defender));
		assertEquals(10, scn.GetStrength(aragorn));

		// Aragorn has lived, so in the regroup phase nothing is left.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void WhenAragornDiesThatTurnEachValiantCompanionIsAFurtherPlus1ForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		var guma = scn.GetFreepsCard("guma");
		var flame = scn.GetFreepsCard("flame");
		scn.MoveCompanionsToTable(aragorn, guard, defender);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(flame);
		scn.MoveCardsToHand(die);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		// No ally is valiant in print.  The Flame of Calenhad makes one: the Household Guard exerts, and until
		// the regroup phase the player's other Men have his keyword: Guma, valiant and not a companion, and
		// Aragorn with him.
		assertFalse(scn.HasKeyword(guma, Keyword.VALIANT));
		scn.FreepsUseCardAction(flame);
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(4, scn.GetStrength(guma));
		scn.ShadowPass();

		scn.FreepsPlayCard(die);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(defender));
		// The valiant ally is not made stronger.
		assertEquals(4, scn.GetStrength(guma));

		// The Marksman's one arrow goes to Aragorn, who has a single vitality left.
		scn.SkipToArcheryWounds();
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.DEAD, aragorn);
		// The rules' own business with the death and the event's are required at the same moment, and the
		// Free Peoples player says which goes first.
		scn.FreepsResolveRuleFirst();

		// 6 + 2 + 1 and 5 + 2 + 1.
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(defender));
		// Not the companion who is not valiant, nor the valiant ally, nor the minion.
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(7, scn.GetStrength(marksman));

		// The Household Guard fights the Marksman at 9 and kills it.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, marksman);
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(guard));
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, marksman);

		// In the regroup phase the +2 has gone and the +1 has not.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(defender));
		assertEquals(4, scn.GetStrength(guma));

		// The fellowship moves again, and at the second site of the turn it is still +1.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetStrength(guard));
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(defender));

		// The turn ends, and so does the +1.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
	}

	@Test
	public void AnotherGondorManDyingThatTurnGivesNoFurtherBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elendil = scn.GetFreepsCard("elendil");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(aragorn, elendil, guard);
		scn.MoveCardsToHand(die);
		// Vitality 5 with 4 wounds: like Aragorn once he has exerted, Elendil is one wound from death.
		scn.AddWoundsToChar(elendil, 4);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(die);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(guard));

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(elendil);
		assertInZone(Zone.DEAD, elendil);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(8, scn.GetStrength(guard));

		// The Household Guard's 8 beats the Marksman's 7, and in the regroup phase nothing is left.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, marksman);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(8, scn.GetStrength(guard));
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, marksman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
	}

	@Test
	public void AragornBeingDiscardedThatTurnGivesNoFurtherBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		scn.MoveCompanionsToTable(aragorn, guard, defender);
		scn.MoveCardsToHand(die);

		var despair = scn.GetShadowCard("despair");
		var nomad1 = scn.GetShadowCard("nomad1");
		var nomad2 = scn.GetShadowCard("nomad2");
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		// Enough in the pool for Fierce in Despair's 7 when the regroup phase comes.
		scn.SetTwilight(7);
		scn.MoveMinionsToTable(nomad1, nomad2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(die);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(defender));

		// Nobody fights.  In the regroup phase the +2 is over.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));

		// Fierce in Despair discards Aragorn.  He has left play in the turn the event was played, and he
		// has not been killed: he is in the discard pile and not the dead pile.
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(despair));
		scn.ShadowPlayCard(despair);
		scn.ShadowChooseCard(aragorn);
		assertInZone(Zone.DISCARD, aragorn);

		// No +1, and nothing is asked: the regroup phase goes on.
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornDyingInTheRegroupPhaseStillGivesPlus1UntilTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, guard, defender, rider);
		scn.MoveCardsToHand(die);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(die);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(defender));

		// Nobody fights.  In the regroup phase the +2 is over.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));

		// The Morgul Slayer wounds Aragorn, the [rohan] one, who dies of it.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);
		assertInZone(Zone.DEAD, aragorn);
		scn.FreepsResolveRuleFirst();
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(defender));
		assertEquals(5, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Still so when the fellowship is asked whether it moves again; it stays, and the next turn begins
		// without it.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(defender));
		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
	}

	@Test
	public void AragornDyingOnALaterTurnGivesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(die);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(die);
		assertInZone(Zone.DISCARD, die);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(guard));

		// The turn ends with Aragorn alive; the other player's turn goes by; and it is the first player's
		// turn again, at site 2, which is no sanctuary.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(guard));

		// On this turn a Marksman's arrow kills him.
		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ValiantCompanionsWhoArriveLaterGetOnlyWhatIsGivenAfterTheyArrive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var defender = scn.GetFreepsCard("defender");
		scn.MoveCompanionsToTable(aragorn, guard1);
		scn.MoveCardsToHand(die);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(die);
		assertEquals(8, scn.GetStrength(guard1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second Household Guard arrives after the event was played: no +2 for him.
		scn.MoveCompanionsToTable(guard2);
		assertTrue(scn.HasKeyword(guard2, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(8, scn.GetStrength(guard1));

		// Aragorn, with one vitality left, fights the Goblin Patrol Troop and loses: 8 against 13.  The
		// first Guard's skirmish with the Runner is still to come.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, troop },
				new PhysicalCardImpl[] { guard1, runner }
		);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The rules' own business with the death and the event's are required at the same moment, and the
		// Free Peoples player says which goes first.
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The +1 is made now, for the valiant companions there are now: both Guards.
		assertEquals(9, scn.GetStrength(guard1));
		assertEquals(7, scn.GetStrength(guard2));

		// An Elven Defender arrives after the death: nothing for him.
		scn.MoveCompanionsToTable(defender);
		assertTrue(scn.HasKeyword(defender, Keyword.VALIANT));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(9, scn.GetStrength(guard1));
		assertEquals(7, scn.GetStrength(guard2));
	}
}
