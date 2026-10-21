package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("worker1", "3_62");
					put("worker2", "3_62");   // not unique: "this minion" is read beside another copy

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					// Barliman Butterbur: a [gandalf] Man ally, vitality 2; a fellowship ability only
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardWorkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Isengard Worker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Exert this minion to make the Free Peoples player wound a companion.
		*/

		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker1");

		assertEquals("Isengard Worker", worker.getBlueprint().getTitle());
		assertNull(worker.getBlueprint().getSubtitle());
		assertFalse(worker.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, worker.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, worker.getBlueprint().getCulture());
		assertEquals(CardType.MINION, worker.getBlueprint().getCardType());
		assertEquals(Race.ORC, worker.getBlueprint().getRace());
		assertEquals(2, worker.getBlueprint().getTwilightCost());
		assertEquals(5, worker.getBlueprint().getStrength());
		assertEquals(3, worker.getBlueprint().getVitality());
		assertEquals(4, worker.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsThisMinionToMakeTheFreePeoplesPlayerWoundACompanionUntilItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		// Gimli, vitality 3, is exhausted: a wound kills him.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		var worker1 = scn.GetShadowCard("worker1");
		var worker2 = scn.GetShadowCard("worker2");

		scn.StartGame();
		scn.MoveMinionsToTable(worker1, worker2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(worker1));
		assertEquals(0, scn.GetWoundsOn(worker2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(worker1));
		assertTrue(scn.ShadowActionAvailable(worker2));

		scn.ShadowUseCardAction(worker1);
		// The Worker used exerts, and the copy beside it does not.
		assertEquals(1, scn.GetWoundsOn(worker1));
		assertEquals(0, scn.GetWoundsOn(worker2));

		// The Free Peoples player chooses the companion: any companion, exhausted or not, and no ally or minion.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(worker2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// A wound and not an exertion: the exhausted Gimli is killed.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		scn.ShadowUseCardAction(worker1);
		assertEquals(2, scn.GetWoundsOn(worker1));
		assertEquals(0, scn.GetWoundsOn(worker2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: exhausted, so it can no longer exert; the copy still can.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(worker1));
		assertTrue(scn.ShadowActionAvailable(worker2));
	}
}
