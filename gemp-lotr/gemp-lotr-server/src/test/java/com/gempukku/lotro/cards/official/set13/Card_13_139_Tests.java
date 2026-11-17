package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_139_Tests
{

	// The Fellowship path, but for White Rocks 4_346 (a battleground and nothing else) as site 4 and Deeping Wall
	// 4_348 (a battleground and nothing else) as site 5.  Site 2 Ettenmoors is a plains; site 1 Bag End and site 3
	// Rivendell Valley (a sanctuary) are neither.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_139");

					put("warrior", "12_113");   // Eored Warrior: [rohan] Man companion; strength +3 at a battleground or plains, nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion, cost 2; answers only another [rohan] Man's exertion
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ALLY; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose

					put("host", "5_94");        // Thundering Host: [rohan] support-area condition, cost 1; a skirmish ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "4_346");      // White Rocks: Battleground; its Shadow number is +1 for each mounted companion
					put("site5", "4_348");      // Deeping Wall: Battleground; a Shadow ability that plays Saruman, never used
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Uruk Camp 4_335 as site 2: a battleground AND a plains.
	protected VirtualTableScenario GetBothScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_139");

					put("warrior", "12_113");   // Eored Warrior: [rohan] Man companion; strength +3 at a battleground or plains, nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; answers only another [rohan] Man's exertion
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "4_335");      // Uruk Camp: Battleground, Plains; each Uruk-hai played must exert (none is)
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetMusterScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_139");

					put("scout1", "11_18");     // Elven Scout: [elven] Elf companion; has muster while another Elf can be spotted
					put("scout2", "11_18");
					put("theoden", "11_159");   // Theoden, King of the Eorlingas: [rohan] Man; gains muster while a [gandalf] companion can be spotted
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a companion with no muster; a strength bonus only

					// The Witch-king, Captain of the Nine Riders: a MINION with muster (and fierce, toil 2); the Shadow
					// player's muster offer is declined
					put("witchking", "11_226");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WindsweptHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Wind-swept Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [rohan] companions.<br>When you play this, add a [rohan] token here for each plains site and each battleground site on the adventure path.<br>At the start of each regroup phase, add a [rohan] token here for each of your companions that has muster.
		*/

		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");

		assertEquals("Wind-swept Homestead", homestead.getBlueprint().getTitle());
		assertNull(homestead.getBlueprint().getSubtitle());
		assertTrue(homestead.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, homestead.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, homestead.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, homestead.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(homestead, Keyword.SUPPORT_AREA));
		assertEquals(2, homestead.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondRohanCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var warrior = scn.GetFreepsCard("warrior");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(warrior, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(homestead, merry);

		scn.StartGame();
		assertInHand(homestead, merry);

		// One [rohan] companion; beside him a [rohan] ally and a companion of another culture.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(homestead));

		// A [rohan] Hobbit companion makes the second, beside the [rohan] Man.
		scn.FreepsPlayCard(merry);
		assertInPlay(merry);
		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);

		assertInZone(Zone.SUPPORT, homestead);
		// Site 1, Bag End, is neither a plains nor a battleground.
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSiteFourItTakesATokenForThePlainsAndForTheBattlegroundAndNoneForASiteNotYetOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var warrior = scn.GetFreepsCard("warrior");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(warrior, merry);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertInHand(homestead);
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetSite(4), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetSite(3), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetSite(3), Keyword.BATTLEGROUND));
		// Deeping Wall, a battleground, is still in the adventure deck.
		assertNull(scn.GetSite(5));

		scn.FreepsPlayCard(homestead);

		// Ettenmoors 1 + White Rocks 1; not Bag End, Rivendell Valley or Deeping Wall
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSiteTwoItTakesOneTokenAndAnotherRohanConditionPlayedAfterItAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var warrior = scn.GetFreepsCard("warrior");
		var merry = scn.GetFreepsCard("merry");
		var host = scn.GetFreepsCard("host");
		scn.MoveCompanionsToTable(warrior, merry);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(homestead, host);
		scn.FreepsRefreshDecision();

		assertInHand(homestead, host);
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.PLAINS));

		scn.FreepsPlayCard(homestead);
		// Ettenmoors alone
		assertEquals(1, scn.GetCultureTokensOn(homestead));

		scn.FreepsPlayCard(host);
		assertInZone(Zone.SUPPORT, host);
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AControlledPlainsSiteIsNoLongerOnThePathAndAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var warrior = scn.GetFreepsCard("warrior");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(warrior, merry);

		scn.StartGame();
		scn.SkipToSite(4);
		var ettenmoors = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(ettenmoors);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		// Ettenmoors is still a plains, but a controlled site is in its controller's support area.
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.PLAINS));
		assertInZone(Zone.SUPPORT, ettenmoors);
		assertInHand(homestead);

		scn.FreepsPlayCard(homestead);
		// White Rocks alone
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Q-0383: A (Ketura, 2026-10-28): a site that is both a plains and a battleground adds a token for each word.
	@Test
	public void ASiteThatIsBothAPlainsAndABattlegroundAddsATokenForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBothScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var warrior = scn.GetFreepsCard("warrior");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(warrior, merry);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertInHand(homestead);
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.BATTLEGROUND));

		scn.FreepsPlayCard(homestead);
		// Uruk Camp, a plains site 1 and a battleground site 1
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseItTakesATokenForEachOfYourCompanionsThatHasMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMusterScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var scout1 = scn.GetFreepsCard("scout1");
		var scout2 = scn.GetFreepsCard("scout2");
		var theoden = scn.GetFreepsCard("theoden");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(scout1, scout2, theoden, gandalf);
		scn.MoveCardsToSupportArea(homestead);

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		scn.MoveMinionsToTable(witchking);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.HasKeyword(scout1, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(scout2, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(theoden, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(witchking, Keyword.MUSTER));
		// Nothing at the start of the phases before the regroup phase.
		assertEquals(0, scn.GetCultureTokensOn(homestead));

		scn.SkipToPhase(Phase.REGROUP);

		// Two Elven Scouts and Theoden; not Gandalf, who has no muster, nor the Witch-king, a minion of the
		// other player's.
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInPlay(witchking);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void WithOneCompanionThatHasMusterItTakesOneTokenAndAgainAtTheRegroupPhaseOfYourNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMusterScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var scout1 = scn.GetFreepsCard("scout1");
		var theoden = scn.GetFreepsCard("theoden");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(scout1, theoden, gandalf);
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		// The Elven Scout spots no other Elf.
		assertFalse(scn.HasKeyword(scout1, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(theoden, Keyword.MUSTER));

		// Turn 1's regroup phase adds one; the opponent's regroup phase on turn 2 adds nothing.
		scn.SkipToSite(2);
		assertEquals(1, scn.GetCultureTokensOn(homestead));

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}
}
