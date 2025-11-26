package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class RelentlessTests {
	@Test
	public void HinderingACompanionInPlayFlipsItOver() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = new VirtualTableScenario(
				new HashMap<>() {{
					put("aragorn", "1_89");

					put("runner", "1_178");
					put("enquea", "103_74");
				}}
		);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(enquea, runner);

		scn.StartGame();

		scn.SkipToAssignments();

		//Regular assignments
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		assertTrue(scn.FreepsCanAssign(runner));
		assertTrue(scn.FreepsCanAssign(enquea));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   frodo, runner },
				new PhysicalCardImpl[] { aragorn, enquea }
		);

		scn.FreepsResolveSkirmish(frodo);
		scn.BothPass();
		scn.FreepsDeclineOptionalTrigger(); //the one ring

		scn.FreepsResolveSkirmish(aragorn);
		scn.BothPass();

		//Fierce Assignments
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.BothPass();
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		//Runner is not fierce
		assertFalse(scn.FreepsCanAssign(runner));
		assertTrue(scn.FreepsCanAssign(enquea));

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.ShadowPass(); //Enquea's own game text
		scn.BothPass();

		//Relentless Assignments
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.BothPass();
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		//Runner is not relentless
		assertFalse(scn.FreepsCanAssign(runner));
		assertTrue(scn.FreepsCanAssign(enquea));

		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.ShadowPass(); //Enquea's own game text
		scn.BothPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

}
