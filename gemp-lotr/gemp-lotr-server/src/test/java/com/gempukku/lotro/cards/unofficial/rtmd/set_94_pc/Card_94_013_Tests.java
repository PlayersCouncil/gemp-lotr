package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_013_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("legolas", "1_50"); // Legolas, Greenleaf: Elven archer companion
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: non-archer companion
		put("troop", "1_177"); // Goblin Patrol Troop: clean Shadow minion
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_13", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_13
		 * Type: MetaSite
		 * Game Text: Archery: Make each of your archers strength +3 until the regroup phase. Your
		 * archers do not contribute to your archery total and you cannot make any more Archery
		 * actions.
		 */

		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_13", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ArcherGetsStrengthAndStopsContributingAndLocksOutFurtherArcheryActions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// Pre-check: base strengths and archery total (Legolas is the only archer)
		int legolasBase = scn.GetStrength(legolas);
		int aragornBase = scn.GetStrength(aragorn);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Effect: Legolas +3 strength, Aragorn untouched, Legolas no longer counted in archery total
		assertEquals(legolasBase + 3, scn.GetStrength(legolas));
		assertEquals(aragornBase, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Self-lockout: no more archery actions this phase (including Legolas's own archery ability)
		assertFalse(scn.FreepsActionAvailable(mod));
		assertFalse(scn.FreepsAnyActionsAvailable());

		// Duration: bonus gone at regroup
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(legolasBase, scn.GetStrength(legolas));
	}

	@Test
	public void ActionAvailableWithoutAnArcherButEffectFizzlesAndCostIsStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// No archer in play, but there is no CanSpot requirement: costs/requirements gate,
		// effects merely fizzle. The action is still available and still locks out the phase.
		int aragornBase = scn.GetStrength(aragorn);
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		assertEquals(aragornBase, scn.GetStrength(aragorn));
		assertFalse(scn.FreepsActionAvailable(mod));
		assertFalse(scn.FreepsAnyActionsAvailable());
	}

	@Test
	public void ShadowsActivationOfTheSameMetaSiteAffectsOnlyShadowsOwnArchersNotFreepsSince() throws DecisionResultInvalidException, CardNotFoundException {
		// A MetaSite Activated ability during a back-and-forth phase (archery) is offered to
		// whichever player is currently acting, regardless of which physical player owns the mod -
		// "your archers" scopes to the activator, exactly as with a real Site. This is the Shadow
		// scenario for this "your" card: it must confirm the Shadow player's own activation cannot
		// reach into the Free Peoples player's archers.
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(legolas);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		int legolasBase = scn.GetStrength(legolas);

		// Freeps passes their archery action first (back-and-forth), then Shadow can act.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// "Your archers" resolves to the activating player's own archers - Shadow has none (the
		// troop is not an archer), so this fizzles, and Legolas (Freeps' archer) is unaffected.
		assertEquals(legolasBase, scn.GetStrength(legolas));
	}
}
