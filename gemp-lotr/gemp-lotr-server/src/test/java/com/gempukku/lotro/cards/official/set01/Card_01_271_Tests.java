package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_271_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier1", "1_271");
					put("soldier2", "1_271");   // not unique: "this minion" is read beside other copies
					put("soldier3", "1_271");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Exert this minion to wound a character he is skirmishing.
		*/

		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier1");

		assertEquals("Orc Soldier", soldier.getBlueprint().getTitle());
		assertNull(soldier.getBlueprint().getSubtitle());
		assertFalse(soldier.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, soldier.getBlueprint().getSide());
		assertEquals(Culture.SAURON, soldier.getBlueprint().getCulture());
		assertEquals(CardType.MINION, soldier.getBlueprint().getCardType());
		assertEquals(Race.ORC, soldier.getBlueprint().getRace());
		assertEquals(2, soldier.getBlueprint().getTwilightCost());
		assertEquals(7, soldier.getBlueprint().getStrength());
		assertEquals(2, soldier.getBlueprint().getVitality());
		assertEquals(6, soldier.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishExertsThisMinionToWoundTheCharacterHeIsSkirmishingAndNobodyForOneWhoseSkirmishHasNotBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2, soldier3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier1}, new PhysicalCardImpl[]{boromir, soldier3});
		// A second copy in the same skirmish, put there by the Shadow player.
		scn.ShadowAssignToMinions(aragorn, soldier2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(soldier1));
		assertTrue(scn.ShadowActionAvailable(soldier2));
		// The copy assigned to Boromir is skirmishing nobody yet; the text names no skirmish, and the exertion can be paid.
		assertTrue(scn.ShadowActionAvailable(soldier3));
		assertEquals(0, scn.GetWoundsOn(soldier3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.ShadowUseCardAction(soldier3);

		// Paid for nothing: neither Aragorn, in the skirmish being fought, nor Boromir, whose skirmish is to come.
		assertEquals(1, scn.GetWoundsOn(soldier3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(soldier1));
		assertEquals(0, scn.GetWoundsOn(soldier2));
		scn.ShadowUseCardAction(soldier1);

		// This minion exerts, not the copy beside him, and Aragorn, whom he is skirmishing, is wounded.
		assertEquals(1, scn.GetWoundsOn(soldier1));
		assertEquals(0, scn.GetWoundsOn(soldier2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(soldier3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAnAllyHeIsSkirmishingAndAWoundKillsHimWithOneVitalityLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier1 = scn.GetShadowCard("soldier1");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);

		// Thrarin has exerted to take part in skirmishes: vitality 2, one left.
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, soldier1);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier1));
		assertTrue(scn.ShadowActionAvailable(soldier1));
		scn.ShadowUseCardAction(soldier1);

		// A wound, not an exertion: an exhausted ally takes it and is killed.
		assertEquals(1, scn.GetWoundsOn(soldier1));
		assertInZone(Zone.DEAD, thrarin);
		// His skirmish is over with him gone, and it was the only one.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedSoldierIsNotOfferedTheAbilityWhileTheCopyBesideHimIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.AddWoundsToChar(soldier1, 1);
		assertEquals(1, scn.GetWoundsOn(soldier1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier1);
		scn.ShadowAssignToMinions(aragorn, soldier2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 2 with one wound: he cannot exert.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(soldier1));
		assertTrue(scn.ShadowActionAvailable(soldier2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
