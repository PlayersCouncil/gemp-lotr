package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_020_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "101_20");

					// Merry, Friend to Sam: an unbound Hobbit with the Frodo signet.  His skirmish ability is
					// only ever used on purpose.
					put("merry", "1_302");
					// Gimli, Dwarf of Erebor: a Dwarf with the Aragorn signet.  Damage +1, and a fellowship
					// action that is never used here.
					put("gimli", "1_12");
					// Sam, Son of Hamfast: a Hobbit with the Aragorn signet, Ring-bound as every Sam is.
					put("sam", "1_311");
					// Sam, Faithful Companion: a Ring-bound Hobbit with the Frodo signet.
					put("frodosam", "1_310");
					// Farmer Maggot, Chaser of Rascals: a Hobbit ally whose home is site 1.  Only companions
					// are bound or unbound.
					put("maggot", "1_288");
					// Aragorn, Ranger of the North: strength 8, the Gandalf signet.  His maneuver ability
					// would make him a defender, not Boromir, and is never used.
					put("aragorn", "1_89");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					// Ulaire Nelya, Lieutenant of Morgul: strength 10, vitality 3, fierce.  His ability is a
					// Shadow-phase action.
					put("nelya", "1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,    // Frodo, Son of Drogo: the Frodo signet
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Boromir, Redeemed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Boromir is strength +2 for each minion he is skirmishing.
		 * At the start of each assignment phase, make Boromir <b>defender +1</b> if you can spot an unbound
		 * Hobbit or another companion with the Aragorn signet.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Redeemed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysFromHandAsACompanionForThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(boromir);

		scn.StartGame();

		assertInHand(boromir);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(boromir));

		scn.FreepsPlayCard(boromir);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlusTwoWhileSkirmishingOneMinionAndNotForAMinionAssignedToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(boromir, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();

		assertEquals(5, scn.GetStrength(boromir));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { boromir, savage }, new PhysicalCardImpl[] { aragorn, runner });

		// Assigned is not skirmishing: no skirmish has begun.
		assertTrue(scn.IsCharAssignedAgainst(boromir, savage));
		assertEquals(5, scn.GetStrength(boromir));

		// Boromir's skirmish.  The Goblin Runner is assigned to Aragorn and waits its turn: it is not a
		// minion Boromir is skirmishing.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, savage));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		// 5 + 2 for the Uruk Savage
		assertEquals(7, scn.GetStrength(boromir));

		// Boromir 7 beats the Savage's 5.  In Aragorn's skirmish Boromir is skirmishing nothing.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals(5, scn.GetStrength(boromir));
		// Aragorn skirmishing a minion is at his printed strength: the bonus is Boromir's alone.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlusSixWhileSkirmishingThreeMinionsAndNotBeforeHisSkirmishBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner1, runner2, troop);
		scn.SkipToAssignments();

		assertEquals(5, scn.GetStrength(boromir));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { boromir, savage }, new PhysicalCardImpl[] { aragorn, troop });
		// The Shadow player piles the two Goblin Runners left over onto Boromir.
		scn.ShadowAssignToMinions(boromir, runner1, runner2);

		// Aragorn's skirmish first.  Boromir is assigned to three minions and skirmishing none of them.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssignedAgainst(boromir, savage));
		assertTrue(scn.IsCharAssignedAgainst(boromir, runner1));
		assertTrue(scn.IsCharAssignedAgainst(boromir, runner2));
		assertEquals(5, scn.GetStrength(boromir));

		// Aragorn loses to the Goblin Patrol Troop, which is then no longer in any skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner2));
		// 5 + 2 for the [isengard] Uruk-hai + 2 + 2 for the two [moria] Orcs; nothing for the Troop
		assertEquals(11, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnUnboundHobbitAtTheStartOfTheAssignmentPhaseMakesBoromirDefenderPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner1");
		// Merry's signet is Frodo's, so it is as an unbound Hobbit that he counts.
		scn.MoveCompanionsToTable(boromir, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		scn.PassArcheryActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Defender +1: the Free Peoples player may assign Boromir to a second minion.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(boromir, savage, runner);
		assertTrue(scn.IsCharAssignedAgainst(boromir, savage));
		assertTrue(scn.IsCharAssignedAgainst(boromir, runner));
		assertFalse(scn.IsCharAssigned(merry));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AnotherCompanionWithTheAragornSignetMakesBoromirDefenderPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		// Gimli is no Hobbit, so it is by his Aragorn signet that he counts.
		scn.MoveCompanionsToTable(boromir, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		scn.PassArcheryActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		// Gimli's signet earns Boromir the keyword, not Gimli.
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NoUnboundHobbitAndNoOtherAragornSignetLeavesBoromirWithoutDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var frodosam = scn.GetFreepsCard("frodosam");
		var maggot = scn.GetFreepsCard("maggot");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner1");
		// Everything that nearly counts: two Ring-bound Hobbits whose signet is Frodo's (the Ring-bearer and
		// Sam), a Hobbit who is an ally, a companion with the Gandalf signet, and Boromir's own Aragorn
		// signet, which is not another companion's.
		scn.MoveCompanionsToTable(boromir, frodosam, aragorn);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);

		assertEquals(Race.HOBBIT, frodo.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(frodo, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(frodosam, Keyword.RING_BOUND));
		assertEquals(Signet.ARAGORN, boromir.getBlueprint().getSignet());

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Nothing is offered and nothing is made.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsGoneByTheRegroupPhaseAndMadeAgainInTheAssignmentPhaseOfASecondMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));

		// Boromir (5 + 2 = 7) beats the Uruk Savage (5), which survives its one wound.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));

		// A second move in the same turn, and so a second assignment phase.
		scn.PassRegroupActions();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Defender +1 once more, and not +2: the first did not last the turn.
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DefenderPlusOneIsMadeWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		scn.PassArcheryActions();

		// "Make Boromir defender +1 if you can spot": nothing for the player to decide.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DefenderPlusOneLastsOnlyForTheAssignmentPhaseItWasMadeIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var nelya = scn.GetShadowCard("nelya");
		var runner = scn.GetShadowCard("runner1");
		// Sam is Ring-bound, so he is no unbound Hobbit: he counts as another companion with the Aragorn
		// signet.
		scn.MoveCompanionsToTable(boromir, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		// Spotting Sam costs Sam nothing.
		assertEquals(0, scn.GetWoundsOn(sam));

		// It lasts long enough to assign by: Boromir takes both minions.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(boromir, nelya, runner);
		assertTrue(scn.IsCharAssignedAgainst(boromir, nelya));
		assertTrue(scn.IsCharAssignedAgainst(boromir, runner));

		// The assignment phase is over once the skirmish has begun, and the keyword with it.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));

		// Boromir (5 + 2 + 2 = 9) loses to Ulaire Nelya and the Goblin Runner (10 + 5) and survives.  Nelya
		// is fierce, and the fierce assignment phase is another assignment phase: Boromir is made defender +1
		// again, unasked.  +1 in each assignment phase, not +1 and then +2.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
