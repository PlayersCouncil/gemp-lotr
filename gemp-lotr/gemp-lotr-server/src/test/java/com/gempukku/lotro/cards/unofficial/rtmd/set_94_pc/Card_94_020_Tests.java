package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_020_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("merry", "4_310"); // Merry: Skirmish action exerts the Ring-bearer as its own cost, no assignment needed
		put("resistance", "2_79"); // Resistance Becomes Unbearable: Shadow event, exerts the Ring-bearer as its effect
		put("twigul", "2_82"); // Ulaire Attea: pays Resistance Becomes Unbearable's exert cost
		put("troop", "1_177"); // Goblin Patrol Troop: clean Shadow minion
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: keeps a skirmish active so Merry's own action stays available
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_20", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_20"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_20
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Your Ring-bearer may not be exerted by Free Peoples cards.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_20", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsCardCannotExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		// Merry's own "Skirmish: exert the Ring-bearer" action has no legal cost target
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void WithoutTheModFreepsCardsCanExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertTrue(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void ShadowSourcedExertionStillWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var resistance = scn.GetShadowCard("resistance");
		var twigul = scn.GetShadowCard("twigul");
		var frodo = scn.GetRingBearer();

		scn.MoveMinionsToTable(twigul);
		scn.MoveCardsToHand(resistance);
		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(twigul));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(resistance));
		scn.ShadowPlayCard(resistance);

		// Cost (exert the Nazgul) paid, and the Ring-bearer exert -- sourced from the Shadow
		// event, not a Free Peoples card -- goes through despite the modifier.
		assertEquals(1, scn.GetWoundsOn(twigul));
		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsRingBearerIsUnprotected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		// requires: OwnerIsFreeps is false here, so the block is inactive
		assertTrue(scn.FreepsActionAvailable(merry));
	}
}
