package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_080_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "57_80");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8; a maneuver ability used only on purpose
					put("rohan", "17_93");      // Aragorn, Defender of Rohan: the Aragorn who is not [gondor]
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion of another title, strength 7

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is on being played
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Andúril, King's Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Game Text: Bearer must be Aragorn. He is strength +2 for each minion he is skirmishing.
		 * Assignment: Add a threat to make Aragorn defender +1 (limit +1).
		 */

		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", anduril.getBlueprint().getTitle());
		assertEquals("King's Blade", anduril.getBlueprint().getSubtitle());
		assertTrue(anduril.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, anduril.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, anduril.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, anduril.getBlueprint().getCardType());
		assertTrue(anduril.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, anduril.getBlueprint().getTwilightCost());
		// The erratum prints no strength bonus (the original was +2).
		assertEquals(0, anduril.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAragornForThreeAndNotOnBoromir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(anduril);
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(anduril));

		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(anduril));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(anduril);

		// Aragorn is the only bearer, so no choice of bearer is asked.
		assertEquals(3, scn.GetTwilight());
		assertEquals(aragorn, anduril.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAnAragornWhoIsNotGondor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var rohan = scn.GetFreepsCard("rohan");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(anduril);
		scn.MoveCompanionsToTable(rohan, boromir);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(anduril));
		scn.FreepsPlayCard(anduril);

		assertEquals(3, scn.GetTwilight());
		assertEquals(rohan, anduril.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrengthPlus2ForTheMinionHeIsSkirmishingOnlyOnceHisSkirmishBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		scn.SkipToAssignments();
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{boromir, troop});
		scn.FreepsResolveSkirmish(boromir);

		// Aragorn is assigned, but not yet skirmishing; the minion skirmishing Boromir is not his.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AddsAThreatToMakeAragornDefenderPlus1ForTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(anduril));
		assertEquals("false", scn.FreepsActionNoEffect(anduril));

		scn.FreepsUseCardAction(anduril);

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));

		scn.ShadowPass();
		scn.FreepsPass();
		// Defender +1 lets him take both minions.
		scn.FreepsAssignToMinions(aragorn, runner, savage);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		scn.FreepsResolveSkirmish(aragorn);
		// The skirmish phase: defender is gone, and he is skirmishing two minions: 8 + 2 + 2.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(12, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtItsLimitTheAbilityIsStillOfferedAndAddsAThreatForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(anduril);
		scn.ShadowPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		// Three companions: the threat limit is 3, so the cost can still be paid.
		assertEquals(3, scn.GetThreatLimit());
		assertTrue(scn.FreepsActionAvailable(anduril));
		assertEquals("limit", scn.FreepsActionNoEffect(anduril));

		scn.FreepsUseCardAction(anduril);

		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void TheLimitRenewsInTheNextAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(anduril);
		scn.ShadowPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals("limit", scn.FreepsActionNoEffect(anduril));

		// Aragorn (8 + 2) beats the Goblin Runner (5), who dies of the wound.
		scn.FreepsPass();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(anduril));
		assertEquals("false", scn.FreepsActionNoEffect(anduril));

		scn.FreepsUseCardAction(anduril);

		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWithThreatsAtTheLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AddThreats(3);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(anduril));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(anduril));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
	}
}
