package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glory", "17_97");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and no other text
					put("merry", "103_85");      // Merry, Master Holbytla: the [rohan] companion who is a Hobbit, vitality 4; enduring; abilities used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Glóin: a [dwarven] companion, strength 6; damage +1; a skirmish ability used only on purpose
					put("herdsman", "7_247");    // Rohirrim Herdsman: a [rohan] Man who is an ally, strength 4; a regroup ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability only
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: the Aragorn to exert for One Whom Men Would Follow
					put("whom", "1_109");        // One Whom Men Would Follow: Maneuver event; exert Aragorn to make an ally participate in skirmishes

					put("tracking", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; hunter 1 is its whole text
					put("wildman", "15_91");     // Ravaging Wild Man: [men] Man, strength 10; hunter 1 while a hunter can be spotted; its win trigger needs 2 [men] Men
					put("butcher", "17_122");    // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2; hunter 6 is its whole text
					put("butcher2", "17_122");
					put("sentry", "15_170");     // Sentry Uruk: [uruk-hai] Uruk-hai, strength 10; gains hunter 1 while a hunter can be spotted (damage +1 beside a damage +1 character)
					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1; its play trigger does not fire when cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo loses to a Goblin Runner and goes on
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void ForDeathandGloryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: For Death and Glory
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While the Ring-bearer is assigned to a skirmish, each minion skirmishing a [rohan] companion loses <b>hunter</b> and is unable to gain hunter.<br><b>Skirmish:</b> Exert a [rohan] companion to make that companion gain <b>hunter 1</b> until the start of the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glory");

		assertEquals("For Death and Glory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileTheRingBearerWaitsEachMinionSkirmishingARohanCompanionLosesHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var glory = scn.GetFreepsCard("glory");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		var tracking = scn.GetShadowCard("tracking");
		var wildman = scn.GetShadowCard("wildman");
		var butcher = scn.GetShadowCard("butcher");
		var butcher2 = scn.GetShadowCard("butcher2");
		scn.MoveCompanionsToTable(guard, gimli, merry);
		scn.MoveCardsToSupportArea(glory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, tracking, wildman, butcher, butcher2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{guard, tracking},
				new PhysicalCardImpl[]{gimli, butcher}, new PhysicalCardImpl[]{merry, butcher2});
		// The Wild Man left over goes to the Guard as well: two minions in one skirmish.
		scn.ShadowAssignToMinions(guard, wildman);

		// Nobody is skirmishing yet.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.GetKeywordCount(tracking, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher2, Keyword.HUNTER));

		// The Guard's skirmish, Frodo waiting: an Uruk-hai and a [men] Man skirmishing a [rohan] companion lose it; a
		// minion assigned to Merry, whose skirmish has not begun, keeps it.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(guard));
		assertEquals(0, scn.GetKeywordCount(tracking, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher2, Keyword.HUNTER));
		scn.PassSkirmishActions();

		// A minion skirmishing a companion of another culture keeps it.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(gimli));
		assertEquals(6, scn.GetKeywordCount(butcher, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher2, Keyword.HUNTER));
		scn.PassSkirmishActions();

		// Merry, a [rohan] companion who is a Hobbit, Frodo still waiting.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertEquals(0, scn.GetKeywordCount(butcher2, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnceTheRingBearersSkirmishIsOverTheMinionKeepsHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var glory = scn.GetFreepsCard("glory");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(glory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, tracking);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{guard, tracking});
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// Frodo is no longer assigned once his skirmish is over (CR 4.2 "skirmish phase(s)").
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(guard));
		assertEquals(1, scn.GetKeywordCount(tracking, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerAssignedTakesNoHunterAway() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var glory = scn.GetFreepsCard("glory");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(guard, sam);
		scn.MoveCardsToSupportArea(glory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, tracking);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{guard, tracking});
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.IsCharAssigned(sam));

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(guard));
		assertEquals(1, scn.GetKeywordCount(tracking, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingARohanAllyKeepsHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var glory = scn.GetFreepsCard("glory");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		var whom = scn.GetFreepsCard("whom");
		var runner = scn.GetShadowCard("runner");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(whom);
		scn.MoveCardsToSupportArea(glory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, tracking);
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn exerts to make the [rohan] Man ally take part in skirmishes.
		scn.FreepsPlayCard(whom);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{herdsman, tracking});

		scn.FreepsResolveSkirmish(herdsman);
		assertTrue(scn.IsCharSkirmishing(herdsman));
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(1, scn.GetKeywordCount(tracking, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsARohanCompanionToMakeHimHunter1AndTheMinionsHeSkirmishesCannotGainIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var glory = scn.GetFreepsCard("glory");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var herdsman = scn.GetFreepsCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		var sentry = scn.GetShadowCard("sentry");
		var wildman = scn.GetShadowCard("wildman");
		scn.MoveCompanionsToTable(guard, merry, gimli);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(glory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, sentry, wildman);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{guard, sentry},
				new PhysicalCardImpl[]{gimli, wildman});

		// No hunter on the table, so neither minion has one to gain from.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(guard));
		assertEquals(0, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.HUNTER));
		assertTrue(scn.FreepsActionAvailable(glory));
		scn.FreepsUseCardAction(glory);

		// A [rohan] companion in the skirmish or in none, a Man or a Hobbit; not a companion of another culture or a
		// [rohan] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.HUNTER));
		// Now a hunter can be spotted: the Sentry skirmishing the Guard is unable to gain hunter, the Wild Man
		// assigned to Gimli gains it.
		assertEquals(0, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The next skirmish: Merry is still hunter 1, and the Wild Man skirmishing a [dwarven] companion keeps his.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(gimli));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.HUNTER));
		scn.PassSkirmishActions();

		// Gone at the start of the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.HUNTER));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var glory = scn.GetFreepsCard("glory");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(glory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(glory));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{guard, runner2});
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(glory));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileEveryRohanCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var glory = scn.GetFreepsCard("glory");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard, merry, gimli);
		scn.MoveCardsToSupportArea(glory);
		// Vitality 3 and 4: each can take one more wound and cannot exert.
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(merry, 3);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{guard, runner2});
		scn.FreepsResolveSkirmish(guard);

		// Gimli could exert, but he is no [rohan] companion.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(glory));
	}
}
