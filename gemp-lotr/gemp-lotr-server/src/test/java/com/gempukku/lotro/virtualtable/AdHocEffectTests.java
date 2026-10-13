package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: running an ad hoc effect in place of a player's answer to a choice of actions, with the
 * check that it was carried out (ExecuteAdHocEffect) and without it (ExecuteAdHocEffectUnchecked), for an effect
 * whose being stopped is what a test is about.
 */
public class AdHocEffectTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: vitality 4
					put("gimli", "1_13");           // Gimli, Son of Gloin: vitality 3
					put("troop", "1_177");          // Goblin Patrol Troop: no text; the source the wounds are given
				}},
				// Site 1 is Bag End: "Fellowship: Exert a Hobbit to play The Gaffer from your draw deck."  With
				// Frodo there it is an action on offer in the fellowship phase, ahead of anything added.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Runs the call and returns what the rig said in refusing it, or null if it did not refuse.
	private static String Refusal(Runnable call) {
		try {
			call.run();
		}
		catch (AssertionError refusal) {
			return String.valueOf(refusal.getMessage());
		}
		return null;
	}

	@Test
	public void AnAdHocEffectIsTheThingRunThoughTheChoiceOffersAnActionOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// Bag End's action is on offer, and it would exert Frodo.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.FreepsGetAvailableActions().size());

		scn.FreepsExecuteAdHocEffect(new WoundCharactersEffect(troop, aragorn));

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The choice was used up by it and, this being the fellowship phase, is put again.
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A player who is not at a choice of actions cannot be made to run one.
		String refusal = Refusal(() -> scn.ShadowExecuteAdHocEffect(new WoundCharactersEffect(troop, aragorn)));
		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains("Shadow Player is not at a choice of actions"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEffectThatIsStoppedFailsTheCheckedRunAndNotTheUncheckedOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.ApplyAdHocModifier(new CantTakeWoundsModifier(null, null, gimli));

		// Checked: a wound that comes to nothing fails the test, and says which method does not mind.
		String refusal = Refusal(() -> scn.FreepsExecuteAdHocEffect(new WoundCharactersEffect(troop, gimli)));
		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains("was not carried out"));
		assertTrue(refusal, refusal.contains("ExecuteAdHocEffectUnchecked"));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Unchecked: the same wound is run without complaint, and the board says what became of it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.FreepsExecuteAdHocEffectUnchecked(new WoundCharactersEffect(troop, gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// It is no empty gesture: aimed at a character who can take wounds, it wounds him.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsExecuteAdHocEffectUnchecked(new WoundCharactersEffect(troop, aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Shadow player's, at that player's own choice of actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowExecuteAdHocEffectUnchecked(new WoundCharactersEffect(troop, gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowExecuteAdHocEffectUnchecked(new WoundCharactersEffect(troop, aragorn));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
