package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_108_Tests
{
	// Fellowship sites: 1 to 3 are region 1, 4 to 6 region 2, 7 to 9 region 3.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortheshire", "103_108");
					put("sam", "1_311");            // Sam, Son of Hamfast: Hobbit companion, Ring-bound and not the Ring-bearer; strength 3, vitality 4
					put("merry", "1_302");          // Merry, Friend to Sam: Hobbit companion, strength 3, vitality 4
					put("pippin", "103_31");        // Pippin, Trollslayer: a Hobbit companion who is a [gondor] card
					put("businessman", "13_157");   // Westfarthing Businessman: a Hobbit companion who is not unique
					put("bilbo", "1_284");          // Bilbo, Retired Adventurer: Hobbit ally
					put("smeagol", "103_116");      // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4

					put("ted", "18_75");            // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit
					put("savage", "1_151");         // Uruk Savage: strength 5, vitality 3, damage +1
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A table whose Ring-bearer is a Hobbit who is not Frodo.
	protected VirtualTableScenario GetSamRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortheshire", "103_108");
					put("frodo", "13_149");         // Frodo, Frenzied Fighter: the one Frodo with no Ring-bearer keyword
					put("merry", "1_302");          // Merry, Friend to Sam: Hobbit companion

					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				"13_156",                           // Sam, Bearer of Great Need: resistance +1 for each Hobbit, and a regroup ability
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FortheShireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: For the Shire
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Spot a Hobbit companion (except the Ring-bearer).
		 * 	In region 1, hinder all other Hobbits.
		 * 	In region 2, make that Hobbit defender +2 until the regroup phase.
		 * 	In region 3, make that Hobbit <b>enduring</b> until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fortheshire");

		assertEquals("For the Shire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsAHobbitCompanionWhoIsNotTheRingBearerAndInRegionOneHindersEveryOtherHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var businessman = scn.GetFreepsCard("businessman");
		var bilbo = scn.GetFreepsCard("bilbo");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ted = scn.GetShadowCard("ted");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, merry, pippin, businessman, smeagol, aragorn);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();
		// Site 3 is the last site of region 1.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(fortheshire);
		scn.MoveMinionsToTable(ted, runner);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fortheshire));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(pippin));
		assertFalse(scn.IsHindered(businessman));
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(ted));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(fortheshire);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry, pippin, businessman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bilbo, smeagol, aragorn, ted));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(merry);
		// Every other Hobbit: companions of either culture, unique or not, the Ring-bearer, an ally, a minion.
		assertTrue(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(pippin));
		assertTrue(scn.IsHindered(businessman));
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(bilbo));
		assertTrue(scn.IsHindered(ted));
		assertFalse(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(smeagol));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(runner));
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(merry, Keyword.ENDURING));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, fortheshire);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilThereIsAHobbitCompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ted = scn.GetShadowCard("ted");
		var runner = scn.GetShadowCard("runner");
		// The Ring-bearer, a [shire] companion who is no Hobbit, a companion of another culture, a Hobbit ally
		// and a Hobbit minion: none of them is a Hobbit companion other than the Ring-bearer.
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(fortheshire);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fortheshire));

		scn.MoveCompanionsToTable(sam);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(fortheshire));

		// Sam is the only Hobbit who can be spotted, so nothing is asked. Site 2 is in region 1.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(fortheshire);
		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(bilbo));
		assertTrue(scn.IsHindered(ted));
		assertFalse(scn.IsHindered(smeagol));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void IsPlayableInTheAssignmentPhaseAndNotInTheArcheryPhaseOrASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(fortheshire);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fortheshire));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fortheshire));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(sam, runner);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fortheshire));
	}

	@Test
	public void InRegionTwoTheHobbitSpottedIsDefenderPlusTwoUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, merry, aragorn);

		scn.StartGame();
		// Site 4 is the first site of region 2.
		scn.SkipToSite(3);
		scn.MoveCardsToHand(fortheshire);
		scn.MoveMinionsToTable(savage, runner);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(sam, Keyword.DEFENDER));

		scn.FreepsPlayCard(fortheshire);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry));
		scn.FreepsChooseCard(merry);
		assertTrue(scn.HasKeyword(merry, Keyword.DEFENDER));
		assertEquals(2, scn.GetKeywordCount(merry, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(sam, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(frodo, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(merry, Keyword.ENDURING));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(sam));
		assertInZone(Zone.DISCARD, fortheshire);

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, savage },
				new PhysicalCardImpl[] { sam, runner }
		);
		// Aragorn's 8 beats the Savage's 5.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// In the second skirmish, still.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetKeywordCount(merry, Keyword.DEFENDER));

		// Sam's 3 loses to the Runner's 5.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));
	}

	@Test
	public void AtSiteSixItIsStillDefenderPlusTwoAndNotEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();
		// Site 6 is the last site of region 2.
		scn.SkipToSite(5);
		scn.MoveCardsToHand(fortheshire);
		scn.AddWoundsToChar(merry, 1);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));

		scn.FreepsPlayCard(fortheshire);
		scn.FreepsChooseCard(merry);
		assertEquals(2, scn.GetKeywordCount(merry, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(merry, Keyword.ENDURING));
		// Wounded and not enduring: still 3.
		assertEquals(3, scn.GetStrength(merry));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void InRegionThreeTheHobbitSpottedIsEnduringUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, merry, aragorn);

		scn.StartGame();
		// Site 7 is the first site of region 3.
		scn.SkipToSite(6);
		scn.MoveCardsToHand(fortheshire);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.MoveMinionsToTable(savage, runner);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.HasKeyword(merry, Keyword.ENDURING));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));

		scn.FreepsPlayCard(fortheshire);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry));
		scn.FreepsChooseCard(merry);
		assertTrue(scn.HasKeyword(merry, Keyword.ENDURING));
		// 3, and 2 for his one wound
		assertEquals(5, scn.GetStrength(merry));
		assertFalse(scn.HasKeyword(sam, Keyword.ENDURING));
		assertEquals(3, scn.GetStrength(sam));
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(sam));
		assertInZone(Zone.DISCARD, fortheshire);

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, savage },
				new PhysicalCardImpl[] { sam, runner }
		);
		// Aragorn's 8 beats the Savage's 5.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// In the second skirmish, still.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(merry, Keyword.ENDURING));
		assertEquals(5, scn.GetStrength(merry));

		// Sam's 3 loses to the Runner's 5.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(merry, Keyword.ENDURING));
		assertEquals(3, scn.GetStrength(merry));
	}

	@Test
	public void WithARingBearerWhoIsNotFrodoAFrodoWhoIsNotTheRingBearerMayBeSpottedAndTheRingBearerMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamRingBearerScenario();

		var fortheshire = scn.GetFreepsCard("fortheshire");
		var sam = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(frodo, merry);
		scn.MoveCardsToHand(fortheshire);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals("Sam", sam.getBlueprint().getTitle());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(merry));

		scn.FreepsPlayCard(fortheshire);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));

		scn.FreepsChooseCard(frodo);
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(merry));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
