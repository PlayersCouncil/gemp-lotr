package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_246_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo (strength 3, vitality 4). The Great Ring adds no strength and answers
	// no wound while it is not worn, so a wound Frodo takes is read as it is. The Assassin (9) overwhelms a companion
	// of strength 4 or less, so the companions it beats are Gimli (6) and Aragorn (8).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "4_246");

					put("explorer", "4_250");   // Southron Explorer: [raider] Southron Man, strength 5, vitality 3; a regroup ability only
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron, strength 6; his text needs 2 burdens
					// Bladetusk Rearguard: a [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce mounted
					// Southron minion until the end of the turn": a Southron who is no Man, strength 5, vitality 5
					put("rearguard", "103_43");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, strength 8, vitality 4
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
	public void SouthronAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>Each time a companion or ally loses a skirmish involving a Southron, you may remove (1) to make the Free Peoples player wound a Ring-bound companion.
		*/

		var scn = GetScenario();

		var assassin = scn.GetFreepsCard("assassin");

		assertEquals("Southron Assassin", assassin.getBlueprint().getTitle());
		assertNull(assassin.getBlueprint().getSubtitle());
		assertFalse(assassin.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, assassin.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, assassin.getBlueprint().getCulture());
		assertEquals(CardType.MINION, assassin.getBlueprint().getCardType());
		assertEquals(Race.MAN, assassin.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(assassin, Keyword.SOUTHRON));
		assertEquals(4, assassin.getBlueprint().getTwilightCost());
		assertEquals(9, assassin.getBlueprint().getStrength());
		assertEquals(2, assassin.getBlueprint().getVitality());
		assertEquals(4, assassin.getBlueprint().getSiteNumber());
	}


	@Test
	public void ACompanionLosingToTheAssassinLetsHimRemoveOneToMakeTheFreePeoplesPlayerWoundARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(assassin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, assassin);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(1);

		// Gimli (6) loses to the Assassin (9), itself a Southron.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(1, scn.GetTwilight());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		// The Free Peoples player chooses: the Ring-bearer and Sam, exhausted as he is (a wound, not an exertion);
		// not Aragorn or Gimli, who are unbound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyLosingToAnotherSouthronSetsItOffToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(follow);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, explorer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsCanAssign(bounder));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, explorer}, new PhysicalCardImpl[]{aragorn, assassin});
		scn.FreepsResolveSkirmish(bounder);
		scn.SetTwilight(1);

		// Bounder (2 + 2) loses to the Explorer (5), a Southron other than the Assassin.
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowAcceptOptionalTrigger();

		// The Ring-bearer is the only Ring-bound companion: wounded with no choice to make.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ACompanionLosingToASouthronWhoIsNoManSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var rearguard = scn.GetShadowCard("rearguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(rearguard);
		assertInZone(Zone.SUPPORT, rearguard);
		// The Assassin is the minion that gives the turn a maneuver phase.
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.IsRace(rearguard, Race.MAN));
		assertEquals(5, scn.GetStrength(rearguard));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, rearguard}, new PhysicalCardImpl[]{aragorn, assassin});
		scn.FreepsResolveSkirmish(sam);
		scn.SetTwilight(1);

		// Sam (3) loses to the Rearguard (5).
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void IsNotOfferedWhenTheSouthronLosesOrWhenTheCompanionLosesToARaiderManWhoIsNoSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, axeman);
		// A strength that only arranges the board: Aragorn (8 + 2) is to beat the Assassin (9).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		assertEquals(10, scn.GetStrength(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, assassin}, new PhysicalCardImpl[]{gimli, axeman});
		scn.SetTwilight(1);

		// Aragorn beats the Assassin: the Southron is the one who loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli (6) loses to the Easterling (6), a [raider] Man who is no Southron: a tie goes to the Shadow.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, assassin);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(0);

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAndTheWoundsAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, assassin);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(1);

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachLossToASouthronIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var explorer = scn.GetShadowCard("explorer");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, assassin}, new PhysicalCardImpl[]{frodo, explorer});
		scn.SetTwilight(2);

		// Gimli loses the first skirmish: the Free Peoples player has Sam take the wound.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Frodo (3) loses the second to the Explorer (5): offered again.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseCard(frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
