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

public class Card_17_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "17_102");

					put("guard", "7_246");       // Rohirrim Guard: [rohan] Man companion, strength 6; a skirmish ability used only on purpose
					put("merry", "103_85");      // Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3 (a Man only with 3 [rohan] companions)
					put("herdsman", "7_247");    // Rohirrim Herdsman: [rohan] Man ally, strength 4; a regroup ability used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer

					// [rohan] followers of two titles, and their near misses
					put("master", "15_134");     // Rohan Stable Master: [rohan] follower; bearer is mounted
					put("worker", "15_135");     // Rohan Worker: [rohan] follower; its triggers answer a transfer and a win
					put("alatar", "13_28");      // Alatar, Final Envoy: [gandalf] follower; nothing while in the support area

					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo is strength 3 and takes his wounds
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Théoden, Northman, King of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While the Ring-bearer is assigned to a skirmish, each [rohan] companion is strength +1.
		 * 	<b>Regroup:</b> Spot 2 [rohan] followers and exert Théoden to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("Northman, King of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void EachRohanCompanionIsStrengthPlus1WhileTheRingBearerIsAssignedAndNotOnceHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(theoden, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();

		// Nobody is assigned yet.  With two [rohan] companions Merry is no Man.
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner1}, new PhysicalCardImpl[]{aragorn, runner2});

		// Théoden himself, a [rohan] Man, and Merry, a [rohan] Hobbit companion; not the [rohan] ally nor the
		// [gondor] companion.
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(7 + 1, scn.GetStrength(theoden));
		assertEquals(3 + 1, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(8, scn.GetStrength(aragorn));

		// Frodo's skirmish first: he is assigned to it while it is fought.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(7 + 1, scn.GetStrength(theoden));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// His skirmish over, he is assigned to none, with Aragorn's skirmish still waiting.
		assertTrue(scn.IsCharAssigned(aragorn));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerBeingAssignedStrengthensNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(theoden, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);

		assertTrue(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(7, scn.GetStrength(theoden));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void RegroupAbilitySpotsTwoRohanFollowersAndExertsTheodenToLiberateTheHighestSiteTheShadowPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var master = scn.GetFreepsCard("master");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(theoden, guard);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		// One follower in the support area and one borne
		scn.MoveCardsToSupportArea(master);
		scn.AttachCardsTo(guard, worker);
		assertInZone(Zone.SUPPORT, master);
		assertAttachedTo(worker, guard);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(0, scn.GetWoundsOn(theoden));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		// One site, the highest the Shadow player controls, goes back on the path.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithOneRohanFollowerAndIsOfferedForNothingWithASecondAndNoSiteControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var herdsman = scn.GetFreepsCard("herdsman");
		var master = scn.GetFreepsCard("master");
		var worker = scn.GetFreepsCard("worker");
		var alatar = scn.GetFreepsCard("alatar");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(herdsman);
		// One [rohan] follower, a follower of another culture, and a [rohan] card that is no follower
		scn.MoveCardsToSupportArea(master, alatar);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));

		// A second [rohan] follower: offered, with no site in anyone's hands.
		scn.MoveCardsToSupportArea(worker);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		// The exertion is paid, and nothing is liberated.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedOnceTheodenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var master = scn.GetFreepsCard("master");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(theoden);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToSupportArea(master, worker);
		assertTrue(scn.ShadowControls(site1));
		scn.SkipToPhase(Phase.REGROUP);

		// One exertion left: offered.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.AddWoundsToChar(theoden, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetVitality(theoden));
		assertTrue(scn.FreepsActionAvailable(theoden));

		// None left.
		scn.AddWoundsToChar(theoden, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetVitality(theoden));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));
	}
}
