package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_026_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: [moria] Orc, strength 5, vitality 1; its text needs another [moria] Orc in its skirmish
					put("brigand", "4_10");      // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a companion who is not Ring-bound; a maneuver ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_26", null
		);
	}

	// Gimli, Bearer of Grudges is the Ring-bearer at this table: "the Ring-bearer" is whoever bears the Ring.
	// His own text adds burdens or threats only as a skirmish involving him begins.
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: [moria] Orc, strength 5, vitality 1; its text needs another [moria] Orc in its skirmish
					put("brigand", "4_10");      // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a companion who is not Ring-bound; a maneuver ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing,
				null, "94_26"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_26
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: At the start of the assignment phase, your opponent may assign a minion to
		 * skirmish the Ring-bearer. You may add a burden to prevent this.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_26", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(4, mod.getBlueprint().getIntensity());
	}

	@Test
	public void PreventingWithABurdenStopsTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var ringBearer = scn.GetRingBearer();

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int burdensBefore = scn.GetBurdens();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.IsCharAssignedAgainst(ringBearer, backstabber));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("burden"));
		scn.FreepsChooseYes();

		assertEquals(burdensBefore + 1, scn.GetBurdens());
		assertFalse(scn.IsCharAssignedAgainst(ringBearer, backstabber));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningPreventionLetsTheAssignmentHappen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var ringBearer = scn.GetRingBearer();

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int burdensBefore = scn.GetBurdens();
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("burden"));
		scn.FreepsChooseNo();

		assertEquals(burdensBefore, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(ringBearer, backstabber));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ShadowDecliningTheOptionalTriggerLeavesEverythingAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var ringBearer = scn.GetRingBearer();

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int burdensBefore = scn.GetBurdens();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowDeclineOptionalTrigger();

		// The choice was the opponent's alone: the owner is not offered it in his place, and has nothing to prevent.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsDecisionAvailable("burden"));
		assertEquals(burdensBefore, scn.GetBurdens());
		assertFalse(scn.IsCharAssignedAgainst(ringBearer, backstabber));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OwnerGatingShadowIsNeverOfferedWhenItOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The modifier is player 2's and this is player 1's fellowship: its owner's Ring-bearer is not the one here.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OpponentChoosesAnyMinionAndItIsAssignedToTheRingBearerAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var backstabber = scn.GetShadowCard("backstabber");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, sam);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber, brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// A [moria] Orc and a [dunland] Man: any minion, exactly one, and the opponent's to pick.
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber, brigand));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(brigand);

		assertTrue(scn.FreepsDecisionAvailable("burden"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(frodo, brigand));
		assertFalse(scn.IsCharAssigned(backstabber));
		// Sam is Ring-bound but bears no Ring.
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedAsTheManeuverOrArcheryPhaseBeginsAndOfferedAsTheAssignmentPhaseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
	}

	@Test
	public void OnTheOwnersTurnInPlayerTwosSeatPlayerOneMayAssignHisMinionToTheOwnersRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetFreepsCard("backstabber");
		var gimli = scn.GetShadowRingBearer();

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass(); // player 2's fellowship moves
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhaseInverted(Phase.ASSIGNMENT);

		// Player 1 is the Shadow player of this turn, and the owner's opponent.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsCharAssignedAgainst(gimli, backstabber));
		scn.FreepsAcceptOptionalTrigger();

		// The owner is the one asked whether to prevent it.
		assertTrue(scn.ShadowDecisionAvailable("burden"));
		assertFalse(scn.FreepsDecisionAvailable("burden"));
		scn.ShadowChooseNo();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(gimli, backstabber));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheRingBearerIsHinderedAndCannotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.ARCHERY);
		// A hindered character cannot be assigned to a skirmish; Aragorn is there to be assigned as usual.
		scn.HinderCard(frodo);
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.IsHindered(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsCharAssigned(backstabber));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
