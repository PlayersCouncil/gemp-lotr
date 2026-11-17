package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_149_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "15_149");

					put("sam", "1_311");            // Sam, Son of Hamfast: strength 3; a fellowship ability used only on purpose
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("pipe", "1_292");           // The Gaffer's Pipe: a possession a Hobbit bears that is no follower; a fellowship ability not used
					// Rohan Worker: a follower with no printed number, Aid (1); transferred to a companion who is not
					// [rohan] it exerts him twice; its skirmish trigger is not reached
					put("worker1", "15_135");
					put("worker2", "15_135");

					// Hunting Herdsman: a [men] Man, hunter 1, strength 8; +3 for each follower (none where it is laid)
					put("herdsman", "15_83");
					put("butcher", "17_122");       // White Hand Butcher: an [uruk-hai] Uruk-hai, hunter 6 and nothing else
					put("tracker", "15_171");       // Tracking Uruk: an [uruk-hai] Uruk-hai, hunter 1 and nothing else
					put("savage", "1_151");         // Uruk Savage: a minion who is no hunter; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Merry, The Tall One
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 9
		 * Game Text: While Merry bears a follower, he is strength +2.<br><b>Skirmish:</b> If Merry is skirmishing a hunter minion, add a threat to make Merry strength +2.
		*/

		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");

		assertEquals("Merry", merry.getBlueprint().getTitle());
		assertEquals("The Tall One", merry.getBlueprint().getSubtitle());
		assertTrue(merry.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, merry.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, merry.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, merry.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, merry.getBlueprint().getRace());
		assertEquals(1, merry.getBlueprint().getTwilightCost());
		assertEquals(3, merry.getBlueprint().getStrength());
		assertEquals(4, merry.getBlueprint().getVitality());
		assertEquals(9, merry.getBlueprint().getResistance());
	}

	@Test
	public void WhileMerryBearsAFollowerHeIsStrengthPlus2AndNotWhileHeBearsAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pipe = scn.GetFreepsCard("pipe");
		var worker1 = scn.GetFreepsCard("worker1");
		var worker2 = scn.GetFreepsCard("worker2");
		scn.MoveCompanionsToTable(merry, sam);
		scn.AttachCardsTo(merry, pipe);
		scn.AttachCardsTo(sam, worker1);
		scn.MoveCardsToSupportArea(worker2);

		scn.StartGame();
		assertAttachedTo(pipe, merry);
		assertAttachedTo(worker1, sam);
		assertInZone(Zone.SUPPORT, worker2);
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		// A possession that is no follower; a follower in the support area; another companion's follower.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));

		scn.SkipToPhase(Phase.MANEUVER);
		// Both Workers' Aids are offered, the one Sam bears as well.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker2));
		scn.FreepsChooseOptionalTrigger(worker2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, sam));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(worker2, merry);
		// Rohan Worker's own trigger: a companion who is not [rohan] exerts twice.
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(worker1, sam);

		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsAThreatToMakeMerryStrengthPlus2AgainstAHunterMinionForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetShadowCard("herdsman");
		var tracker = scn.GetShadowCard("tracker");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(herdsman, tracker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, herdsman}, new PhysicalCardImpl[]{aragorn, tracker});
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, herdsman));
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Herdsman, 8 + 1 hunter, beats Merry's 5 without doubling it: one wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, tracker));

		// Each skirmish is a phase of its own: the bonus is gone in the next.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileMerryWaitsForHisSkirmishAgainstAHunterAndOfferedInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var butcher = scn.GetShadowCard("butcher");
		var tracker = scn.GetShadowCard("tracker");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(butcher, tracker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, butcher}, new PhysicalCardImpl[]{aragorn, tracker});

		// Aragorn skirmishes a hunter minion while Merry is only assigned to one.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, tracker));
		assertTrue(scn.IsCharAssignedAgainst(merry, butcher));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));

		// Tracking Uruk, 14 + 1, does not double Aragorn's 8.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, butcher));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileMerrySkirmishesAMinionWhoIsNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);

		// A threat could be added: three companions, none yet.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void NotOfferedWithAsManyThreatsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		// Two companions, Frodo and Merry: two threats is the most there can be.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(herdsman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, herdsman);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, herdsman));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}
}
