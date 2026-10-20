package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_109_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gaffer1", "103_109");
					put("gaffer2", "103_109");
					put("sam", "1_311");            // Sam, Son of Hamfast: Hobbit companion, strength 3, vitality 4
					put("merry", "1_302");          // Merry, Friend to Sam: Hobbit companion, strength 3, vitality 4
					put("pippin", "103_31");        // Pippin, Trollslayer: a Hobbit companion who is a [gondor] card; strength 3, enduring
					put("bilbo", "1_284");          // Bilbo, Retired Adventurer: Hobbit ally whose home is site 3; strength 2, vitality 3
					put("smeagol", "103_116");      // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4

					put("runner1", "1_178");        // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("runner5", "1_178");
					put("runner6", "1_178");
					put("savage", "1_151");         // Uruk Savage: strength 5, vitality 3, damage +1
					put("troop", "1_177");          // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("ted", "18_75");            // Ted Sandyman, Chief's Men's Ally: a minion who is a Hobbit; strength 3, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForMyOldGafferStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: For My Old Gaffer
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a Hobbit skirmishing more than 1 minion to resolve skirmishes between that Hobbit and
		 * 	those minions individually.  Until the regroup phase, add a burden each time that Hobbit loses a
		 * 	skirmish and a threat each time that Hobbit wins.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gaffer1");

		assertEquals("For My Old Gaffer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheHobbitFightsEachOfThoseMinionsInASkirmishOfItsOwnAddingAThreatForEachWinAndABurdenForEachLoss() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer1");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		// Five companions with Frodo, so the threat limit is 5.
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveCardsToHand(gaffer);

		scn.StartGame();
		// Sam is made strength 7: enough to beat a Runner or a Savage (5 each) and to lose to the Patrol Troop
		// (13) without being overwhelmed. All three together are 23.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 4));
		// Pippin, another Hobbit, is made strength 6: enough to beat a Runner.
		scn.ApplyAdHocModifier(new StrengthModifier(null, pippin, null, 3));
		scn.MoveMinionsToTable(runner1, runner2, runner3, runner4, savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { sam, runner1 },
				new PhysicalCardImpl[] { merry, runner2 },
				new PhysicalCardImpl[] { aragorn, runner3 },
				new PhysicalCardImpl[] { pippin, runner4 }
		);
		scn.ShadowAssignToMinions(sam, savage, troop);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(7, scn.GetStrength(sam));
		assertTrue(scn.IsCharSkirmishing(runner1));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(gaffer));

		// The Free Peoples player picks which of those minions is fought first.
		scn.FreepsPlayCard(gaffer);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner1, savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner2, runner3));
		scn.FreepsChooseCard(runner1);
		assertInZone(Zone.DISCARD, gaffer);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsCharSkirmishing(runner1));
		// The other two wait, still assigned to him.
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharSkirmishing(troop));
		assertTrue(scn.IsCharAssignedAgainst(savage, sam));
		assertTrue(scn.IsCharAssignedAgainst(troop, sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 7 against 5: Sam wins and the Runner dies.
		scn.ShadowPass();
		scn.FreepsPass();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, runner1);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		// His other minions come before anybody else's skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, runner2, runner3));
		scn.FreepsChooseCard(savage);
		assertTrue(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharSkirmishing(troop));

		// 7 against 5: Sam wins again; the Savage has vitality 3 and lives.
		scn.FreepsPass();
		scn.ShadowPass();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		// One minion left: the choice is still put.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, savage));
		scn.FreepsChooseCard(troop);

		// 7 against 13: Sam loses, and is not overwhelmed.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		// Now the others. Merry, another Hobbit, loses to a Runner: no burden.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, aragorn));
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());

		// Pippin, another Hobbit, beats a Runner, 6 against 5: no threat.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(6, scn.GetStrength(pippin));
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner4);
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		// Aragorn beats a Runner: no threat.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner3);
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(troop));
	}

	@Test
	public void LosingToTwoMinionsOneAtATimeAddsTwoBurdensAndNoneOnceTheRegroupPhaseHasCome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer1");
		var gaffer2 = scn.GetFreepsCard("gaffer2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(gaffer, gaffer2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);
		scn.ShadowAssignToMinions(sam, runner2);
		scn.FreepsResolveSkirmish(sam);

		// Two Runners together are 10, double Sam's 3: fought together they would kill him.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(gaffer);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner1, runner2));
		scn.FreepsChooseCard(runner1);

		// 3 against 5: he loses, and takes one wound.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// In each of those skirmishes he is skirmishing one minion: a second copy cannot be played.
		scn.FreepsChooseCard(runner2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gaffer2));
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, runner1, runner2);

		// The fellowship moves again, and Sam loses another skirmish after the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { sam, runner1 },
				new PhysicalCardImpl[] { aragorn, runner2 }
		);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void BeatingTwoMinionsOneAtATimeAddsTwoThreatsAndNoneOnceTheRegroupPhaseHasCome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer1");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		// Three companions with Frodo, so the threat limit is 3.
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(gaffer);

		scn.StartGame();
		// Sam is made strength 6: more than one Runner's 5, less than two Runners' 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 3));
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);
		scn.ShadowAssignToMinions(sam, runner2);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(6, scn.GetStrength(sam));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(gaffer);
		scn.FreepsChooseCard(runner1);
		scn.ShadowPass();
		scn.FreepsPass();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, runner1);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseCard(runner2);
		scn.FreepsPass();
		scn.ShadowPass();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, runner2);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		// The fellowship moves again, and Sam wins another skirmish after the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner3);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner3);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHobbitAmongTheMinionsIsNotThatHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer1");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ted = scn.GetShadowCard("ted");
		var runner1 = scn.GetShadowCard("runner1");
		// Three companions with Frodo, so the threat limit is 3.
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(gaffer);

		scn.StartGame();
		// Sam is made strength 6: Ted Sandyman is 3 and a Runner 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 3));
		scn.MoveMinionsToTable(ted, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, ted);
		scn.ShadowAssignToMinions(sam, runner1);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(6, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(gaffer);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ted, runner1));
		scn.FreepsChooseCard(ted);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsCharSkirmishing(ted));
		assertFalse(scn.IsCharSkirmishing(runner1));

		// 6 is double Ted's 3: Sam wins and Ted is overwhelmed. Ted has lost a skirmish, but he is not the Hobbit
		// who was skirmishing more than 1 minion.
		scn.ShadowPass();
		scn.FreepsPass();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, ted);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseCard(runner1);
		scn.FreepsPass();
		scn.ShadowPass();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, runner1);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IfTheHobbitIsKilledInOneOfThoseSkirmishesABurdenIsAddedAndTheOtherMinionsFightNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer1");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(gaffer);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { sam, troop },
				new PhysicalCardImpl[] { aragorn, runner3 }
		);
		scn.ShadowAssignToMinions(sam, runner1, runner2);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(gaffer);
		scn.FreepsChooseCard(troop);
		assertTrue(scn.IsCharAssignedAgainst(runner1, sam));
		assertTrue(scn.IsCharAssignedAgainst(runner2, sam));

		// 13 is more than double Sam's 3: he is overwhelmed and dies.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, sam);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// The two Runners have nobody left to fight; only Aragorn's skirmish is still to come.
		assertFalse(scn.IsCharAssigned(runner1));
		assertFalse(scn.IsCharAssigned(runner2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner1, runner2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner3);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedUnlessAHobbitIsSkirmishingMoreThanOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer1");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ted = scn.GetShadowCard("ted");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var runner5 = scn.GetShadowCard("runner5");
		var runner6 = scn.GetShadowCard("runner6");
		scn.MoveCompanionsToTable(sam, merry, smeagol, aragorn);
		scn.MoveCardsToHand(gaffer);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, runner1, runner2, runner3, runner4, runner5, runner6);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { sam, runner1 },
				new PhysicalCardImpl[] { aragorn, ted },
				new PhysicalCardImpl[] { smeagol, runner3 },
				new PhysicalCardImpl[] { merry, runner5 }
		);
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[] { aragorn, runner2 },
				new PhysicalCardImpl[] { smeagol, runner4 },
				new PhysicalCardImpl[] { merry, runner6 }
		);

		// A Hobbit skirmishing exactly one minion, while another Hobbit is assigned to two and waiting.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharAssignedAgainst(runner5, merry));
		assertTrue(scn.IsCharAssignedAgainst(runner6, merry));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gaffer));
		scn.PassSkirmishActions();

		// A companion who is no Hobbit skirmishing two minions, one of them a Hobbit.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(ted));
		assertTrue(scn.IsCharSkirmishing(runner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gaffer));
		scn.PassSkirmishActions();

		// A [shire] companion who is no Hobbit skirmishing two minions.
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.IsCharSkirmishing(runner3));
		assertTrue(scn.IsCharSkirmishing(runner4));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gaffer));
		scn.PassSkirmishActions();

		// A Hobbit skirmishing two minions.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(runner5));
		assertTrue(scn.IsCharSkirmishing(runner6));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gaffer));
	}

	@Test
	public void AHobbitOfAnotherCultureAHobbitAllyAndTheRingBearerMayEachBeTheHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer1 = scn.GetFreepsCard("gaffer1");
		var gaffer2 = scn.GetFreepsCard("gaffer2");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var runner5 = scn.GetShadowCard("runner5");
		var runner6 = scn.GetShadowCard("runner6");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();
		// Bilbo is made strength 3, so that one Runner (5) beats him without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 1));
		// Bilbo's home is site 3: there he can be assigned.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(gaffer1, gaffer2);
		scn.MoveMinionsToTable(runner1, runner2, runner3, runner4, runner5, runner6);
		scn.FreepsPass();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { pippin, runner1 },
				new PhysicalCardImpl[] { bilbo, runner3 },
				new PhysicalCardImpl[] { frodo, runner5 }
		);
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[] { pippin, runner2 },
				new PhysicalCardImpl[] { bilbo, runner4 },
				new PhysicalCardImpl[] { frodo, runner6 }
		);

		// Pippin, a [gondor] Hobbit. The event is not played: two Runners together overwhelm him.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.FreepsPlayAvailable(gaffer1));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);

		// Bilbo, an ally: he loses to each Runner in turn, 3 against 5.
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(gaffer1));
		scn.FreepsPlayCard(gaffer1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner3, runner4));
		scn.FreepsChooseCard(runner3);
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsChooseCard(runner4);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// Frodo, the Ring-bearer.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishing(runner5));
		assertTrue(scn.IsCharSkirmishing(runner6));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gaffer2));
	}

	@Test
	public void InALaterFierceSkirmishThoseMinionsFightTogetherAgainAndALossStillAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer1 = scn.GetFreepsCard("gaffer1");
		var gaffer2 = scn.GetFreepsCard("gaffer2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(gaffer1, gaffer2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		// Both Runners are made fierce, so that they fight a second time before the regroup phase.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, runner1, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, runner2, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);
		scn.ShadowAssignToMinions(sam, runner2);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPlayCard(gaffer1);
		scn.FreepsChooseCard(runner1);
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();
		scn.FreepsChooseCard(runner2);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();
		// 3 against 5, twice.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());

		// The fierce skirmish: both Runners are assigned to Sam again. Nothing says the first sentence lasts
		// beyond the skirmish the event was played in, so this one is fought against both together unless
		// another copy is played.
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(sam, runner1, runner2);
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(runner1));
		assertTrue(scn.IsCharSkirmishing(runner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gaffer2));

		// 10 is more than double his 3: Sam is overwhelmed, and that loss adds a third burden.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, sam);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
