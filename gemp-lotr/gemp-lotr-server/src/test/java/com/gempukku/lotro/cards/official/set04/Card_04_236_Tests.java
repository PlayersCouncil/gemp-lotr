package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_236_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo (strength 3, vitality 4). The Great Ring adds no strength and answers
	// no wound while it is not worn, so a wound Frodo takes is read as it is.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("howl", "4_236");

					put("explorer", "4_250");   // Southron Explorer: [raider] Southron Man, strength 5, vitality 3; a regroup ability only
					put("explorer2", "4_250");
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron, strength 6; his text needs 2 burdens
					// Bladetusk Rearguard: a [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce mounted
					// Southron minion until the end of the turn": a Southron who is no Man, strength 5, vitality 5
					put("rearguard", "103_43");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion, strength 6; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound (CR 4.2 "Ring-bound"), strength 3, vitality 4
					put("bounder", "1_286");    // Bounder: a Hobbit ally, strength 2, vitality 2; a skirmish ability used only on purpose
					// One Whom Men Would Follow: exert Aragorn to make an ally strength +2 and take part in skirmishes
					put("follow", "1_109");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void HowlofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Howl of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time a companion or ally loses a skirmish involving a Southron, you may remove (4) to make the Free Peoples player wound a Ring-bound companion.
		*/

		var scn = GetScenario();

		var howl = scn.GetFreepsCard("howl");

		assertEquals("Howl of Harad", howl.getBlueprint().getTitle());
		assertNull(howl.getBlueprint().getSubtitle());
		assertFalse(howl.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, howl.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, howl.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, howl.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(howl, Keyword.SUPPORT_AREA));
		assertEquals(1, howl.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionLosingToASouthronLetsHimRemoveFourToMakeTheFreePeoplesPlayerWoundARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howl = scn.GetShadowCard("howl");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(sam, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(sam));
		scn.MoveCardsToSupportArea(howl);
		scn.MoveMinionsToTable(explorer);
		assertInZone(Zone.SUPPORT, howl);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, explorer);
		scn.FreepsResolveSkirmish(sam);
		scn.SetTwilight(4);

		// Sam (3) loses to the Southron (5) and takes a wound: exhausted.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howl));
		assertEquals(4, scn.GetTwilight());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		// The Free Peoples player chooses: the Ring-bearer and Sam, exhausted as he is (a wound, not an exertion);
		// not Aragorn, who is unbound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, howl);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyLosingToASouthronSetsItOffToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howl = scn.GetShadowCard("howl");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(follow);

		scn.StartGame();
		scn.MoveCardsToSupportArea(howl);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		assertTrue(scn.FreepsCanAssign(bounder));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, explorer);
		scn.FreepsResolveSkirmish(bounder);
		scn.SetTwilight(4);

		// Bounder (2 + 2) loses to the Southron (5).
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howl));
		scn.ShadowAcceptOptionalTrigger();

		// The Ring-bearer is the only Ring-bound companion: wounded with no choice to make.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionLosingToASouthronWhoIsNoManSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howl = scn.GetShadowCard("howl");
		var rearguard = scn.GetShadowCard("rearguard");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(howl, rearguard);
		// The Easterling is there so that the turn has a maneuver phase.
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertEquals(5, scn.GetStrength(rearguard));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, rearguard}, new PhysicalCardImpl[]{aragorn, axeman});
		scn.FreepsResolveSkirmish(sam);
		scn.SetTwilight(4);

		// Sam (3) loses to the Rearguard (5).
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howl));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseCard(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void IsNotOfferedWhenTheSouthronLosesOrWhenTheCompanionLosesToARaiderManWhoIsNoSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howl = scn.GetShadowCard("howl");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(howl);
		scn.MoveMinionsToTable(explorer, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{gimli, axeman});
		scn.SetTwilight(4);

		// Aragorn (8) beats the Southron (5): the Southron is the one who loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(howl));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli (6) loses to the Easterling (6), a [raider] Man who is no Southron: a tie goes to the Shadow.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(howl));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithThreeTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howl = scn.GetShadowCard("howl");
		var explorer = scn.GetShadowCard("explorer");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(howl);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, explorer);
		scn.FreepsResolveSkirmish(sam);
		scn.SetTwilight(3);

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(howl));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAndTheWoundsAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howl = scn.GetShadowCard("howl");
		var explorer = scn.GetShadowCard("explorer");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(howl);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, explorer);
		scn.FreepsResolveSkirmish(sam);
		scn.SetTwilight(4);

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howl));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachLossToASouthronIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howl = scn.GetShadowCard("howl");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(howl);
		scn.MoveMinionsToTable(explorer, explorer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, explorer}, new PhysicalCardImpl[]{frodo, explorer2});
		scn.SetTwilight(8);

		// Sam loses the first skirmish: the Free Peoples player has Sam take the wound.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howl));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseCard(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Frodo (3) loses the second to the other Southron (5): offered again.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howl));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseCard(frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
