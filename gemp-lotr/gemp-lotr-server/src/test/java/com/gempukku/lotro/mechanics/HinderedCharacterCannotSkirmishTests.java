package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A hindered character takes no part in skirmishes.
 *
 * Comprehensive Rules 5.0, "hinder": "Hindered characters cannot be assigned to skirmishes.
 *    - If an assigned character is hindered, they are immediately removed from the skirmish assignment, which may
 *      cancel it.
 *    - If an actively skirmishing character is hindered, they are immediately removed from the skirmish, which may
 *      cancel it."
 *
 * In the first four tests the hindering is only how the board is laid, and it is laid by the rig.  In the last
 * five the hindering is the thing done, and a printed card does it each time: Smeagol, Once Like Us and Ulaire
 * Toldea, Blessed with Brutality in the assignment phase; Ulaire Attea, Bestowed with Dominion and Shelob, Terror
 * of Cirith Ungol in a skirmish.
 */
public class HinderedCharacterCannotSkirmishTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability and a response to Frodo's death
					put("merry", "1_302");      // Merry, Friend to Sam: [shire]; a skirmish ability, not used here
					// Smeagol, Once Like Us: a [shire] companion.  "Assignment: If you cannot spot a companion with
					// a culture other than [shire], exert Smeagol and hinder him to spot a minion.  Add (2) (and add
					// 2 threats if that minion is twilight cost 5 or more) to hinder that minion."
					put("smeagol", "103_116");

					// Úlairë Enquëa, Lieutenant of Morgul: fierce; his maneuver ability needs 6 companions or 5 burdens
					put("enquea", "1_231");
					// Tower Troll: cost 6, strength 15, vitality 3.  "Assignment: Exert this minion twice to assign
					// it to a companion (except the Ring-bearer).  If you cannot spot 6 companions, the Free Peoples
					// player may exert a companion to prevent this."
					put("troll", "15_117");
					// Undisciplined: an [orc] Assignment event, cost 8, toil 3.  "Assign an [orc] minion to a
					// companion (except the Ring-bearer)."
					put("undisciplined", "11_141");
					put("porter", "11_135");    // Porter Troll: an [orc] minion that is fierce; its other text is about being played
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					// Ulaire Toldea, Blessed with Brutality: fierce, strength 13, vitality 3.  "Each time this minion
					// is assigned to a stronger companion, you may exhaust this minion to hinder that companion."
					put("toldea", "103_79");
					// Unexpected Visitor: a condition on a [gondor] companion.  'Each minion gains this ability:
					// "Assignment: Assign this minion to bearer of Unexpected Visitor."'
					put("visitor", "7_126");
					// Úlairë Attëa, Bestowed with Dominion: fierce, strength 13.  "Skirmish: Hinder another minion
					// to make Úlairë Attëa strength +1."  His other text answers a minion played from hand.
					put("attea", "103_72");
					// Shelob, Terror of Cirith Ungol: fierce.  "Skirmish: If Gollum is skirmishing, exert Gollum and
					// Shelob to hinder a character he is skirmishing."  Her other text answers the end of a skirmish
					// she is in herself.
					put("shelob", "103_13");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: strength 5, vitality 4; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedCompanionIsOfferedInNeitherRoundOfAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.HinderCard(aragorn);
		scn.SkipToAssignments();

		// The first round.  The Free Peoples player has two companions to assign and not three.
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(2, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(sam));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsCanAssign(sam));
		assertFalse(scn.FreepsCanAssign(aragorn));

		// He assigns nobody, and the Shadow player is offered the same two.
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(2, scn.ShadowGetFreepsAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(sam));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowCanAssign(sam));
		assertFalse(scn.ShadowCanAssign(aragorn));
		scn.ShadowDeclineAssignments();

		// The fierce round: the same again for each player.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsHindered(aragorn));
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(2, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(sam));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.FreepsCanAssign(aragorn));

		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(2, scn.ShadowGetFreepsAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(sam));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.ShadowCanAssign(aragorn));
		scn.ShadowDeclineAssignments();

		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAssignmentAbilityOffersEveryCompanionItMayChooseButTheHinderedOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, sam, merry);

		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.HinderCard(aragorn);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troll));
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.ShadowUseCardAction(troll);

		// The Ring-bearer is left out by the Troll's own words, and Aragorn by being hindered.
		assertEquals(2, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(sam);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(sam, troll));
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAssignmentAbilityWithOnlyAHinderedCompanionToChooseIsPaidForAndAssignsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.HinderCard(aragorn);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The ability has a cost, so it is offered whatever it will do.
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.ShadowActionAvailable(troll));
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.ShadowUseCardAction(troll);

		// The cost is paid and the Troll is assigned to nobody: not to the hindered Aragorn, and not to the
		// Ring-bearer either.  The Free Peoples player is not asked to prevent an assignment that is not made,
		// and has his own assignment action to take or pass.
		assertEquals(2, scn.GetWoundsOn(troll));
		assertFalse(scn.IsCharAssigned(troll));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAssignmentEventInTheFierceRoundOffersNoHinderedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, sam, merry);

		var undisciplined = scn.GetShadowCard("undisciplined");
		var porter = scn.GetShadowCard("porter");

		scn.StartGame();
		scn.MoveMinionsToTable(porter);
		scn.MoveCardsToHand(undisciplined);
		scn.HinderCard(aragorn);
		scn.SkipToAssignments();
		// Nobody is assigned in the first round; the Porter Troll is fierce, so a second assignment phase follows.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// The pool pays the event's 8 in full.
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.ShadowPlayAvailable(undisciplined));
		scn.ShadowPlayCard(undisciplined);
		// Toil: the Porter Troll could exert to make the event cheaper, and does not.
		scn.ShadowDeclineChoosing();

		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(merry);

		// The Porter Troll is the only [orc] minion.
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(porter));
		assertInZone(Zone.DISCARD, undisciplined);
		assertTrue(scn.IsCharAssignedAgainst(merry, porter));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void HinderingAnAssignedMinionUndoesTheAssignmentAndItsCompanionMayBeAssignedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		// A fellowship of [shire] companions only, which Smeagol's ability asks for.
		scn.MoveCompanionsToTable(merry, smeagol);

		var troll = scn.GetShadowCard("troll");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troll, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The Troll assigns itself to Merry, and the Free Peoples player does not prevent it.
		scn.ShadowUseCardAction(troll);
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, smeagol));
		scn.ShadowChooseCard(merry);
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(merry, troll));
		assertFalse(scn.IsHindered(troll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());

		// Smeagol hinders the Troll, which costs 6: (2) and 2 threats.
		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troll, runner));
		scn.FreepsChooseCard(troll);

		assertTrue(scn.IsHindered(troll));
		assertTrue(scn.IsHindered(smeagol));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		// Neither the Troll nor Merry is assigned any longer.
		assertFalse(scn.IsCharAssigned(troll));
		assertFalse(scn.IsCharAssigned(merry));
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();

		// In the assignment that follows Merry is free to be assigned, and the Troll is not there to assign to.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(merry));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(smeagol));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(troll));
		assertTrue(scn.FreepsCanAssign(merry));
		assertFalse(scn.FreepsCanAssign(troll));

		scn.FreepsAssignToMinions(merry, runner);

		// No minion is left over for the Shadow player to assign.
		assertTrue(scn.IsCharAssignedAgainst(merry, runner));
		assertFalse(scn.IsCharAssigned(troll));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void HinderingAnAssignedCompanionUndoesTheAssignmentAndItsMinionMayBeAssignedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// Aragorn 8 + 6 = 14, one more than Toldea's 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(toldea));
		assertEquals(14, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(toldea, "Unexpected Visitor"));

		// Toldea assigns himself to Aragorn by the ability the Visitor gives him, and his own text answers it.
		scn.ShadowUseCardAction(toldea, "Unexpected Visitor");
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertEquals(0, scn.GetWoundsOn(toldea));

		scn.ShadowChooseOptionalTrigger(toldea);

		// Vitality 3: exhausting him is two exertions.
		assertEquals(2, scn.GetWoundsOn(toldea));
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Neither Aragorn nor Toldea is assigned any longer.
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(toldea));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();

		// In the assignment that follows Toldea is free to be assigned, and Aragorn is not there to be assigned.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(toldea));
		assertEquals(1, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsCanAssign(toldea));
		assertFalse(scn.FreepsCanAssign(aragorn));
	}

	@Test
	public void HinderingTheOnlyMinionInASkirmishEndsThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var attea = scn.GetShadowCard("attea");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, runner);
		scn.SkipToAssignments();
		// Attea is assigned to nobody.
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertFalse(scn.IsHindered(runner));
		assertTrue(scn.ShadowActionAvailable(attea));

		// The Runner is the only other minion, so it is the one hindered.
		scn.ShadowUseCardAction(attea);

		assertTrue(scn.IsHindered(runner));
		assertFalse(scn.IsCharSkirmishing(runner));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharAssigned(runner));
		assertFalse(scn.IsCharAssigned(aragorn));
		// The skirmish is over with nobody having won or lost it: Aragorn's 8 against the Runner's 5 would have
		// killed the Runner, which has 1 vitality.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// No skirmish is left of the first round.  Attea is fierce, so the fierce round is what follows.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void HinderingOneOfTwoMinionsInASkirmishLeavesTheOtherToFightIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var attea = scn.GetShadowCard("attea");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, runner, savage);
		scn.SkipToAssignments();
		// The Free Peoples player gives Aragorn the Runner, and the Shadow player adds the Savage; Attea is
		// assigned to nobody.
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));

		scn.ShadowUseCardAction(attea);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, savage));
		scn.ShadowChooseCard(runner);

		// The Runner is out of the skirmish, and the skirmish goes on with the Savage.
		assertTrue(scn.IsHindered(runner));
		assertFalse(scn.IsCharSkirmishing(runner));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();

		// Aragorn's 8 beats the Savage's 5 alone; against the 10 of both he would have lost, and taken two wounds
		// for the Savage's damage +1.  The Savage takes the wound for losing and the Runner none.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void HinderingTheCompanionInASkirmishEndsThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.SkipToAssignments();
		// Shelob is assigned to nobody.
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, gollum));
		assertFalse(scn.IsHindered(aragorn));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowActionAvailable(shelob));

		// Aragorn is the only character Gollum is skirmishing, so he is the one hindered.
		scn.ShadowUseCardAction(shelob);

		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(gollum));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(gollum));
		// The skirmish is over with nobody having won or lost it: Gollum's 5 against Aragorn's 8 would have cost
		// Gollum a second wound beside the one he took to pay for the ability.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(shelob));
		// No skirmish is left of the first round.  Shelob is fierce, so the fierce round is what follows.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
