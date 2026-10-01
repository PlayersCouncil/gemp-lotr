package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_026_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("backstabber", "1_174"); // Goblin Backstabber: clean Moria minion
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_26", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
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
		scn.ShadowDeclineOptionalTrigger();

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

		// requires: OwnerIsFreeps is false, so the trigger never appears
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
