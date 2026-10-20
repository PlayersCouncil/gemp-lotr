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

public class Card_01_267_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunters1", "1_267");
					put("hunters2", "1_267");   // not unique: "this minion" is read beside other copies
					put("hunters3", "1_267");
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
	public void OrcHuntersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Hunters
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Exert this minion to wound a character he is skirmishing.
		*/

		var scn = GetScenario();

		var hunters = scn.GetFreepsCard("hunters1");

		assertEquals("Orc Hunters", hunters.getBlueprint().getTitle());
		assertNull(hunters.getBlueprint().getSubtitle());
		assertFalse(hunters.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hunters.getBlueprint().getSide());
		assertEquals(Culture.SAURON, hunters.getBlueprint().getCulture());
		assertEquals(CardType.MINION, hunters.getBlueprint().getCardType());
		assertEquals(Race.ORC, hunters.getBlueprint().getRace());
		assertEquals(3, hunters.getBlueprint().getTwilightCost());
		assertEquals(9, hunters.getBlueprint().getStrength());
		assertEquals(2, hunters.getBlueprint().getVitality());
		assertEquals(6, hunters.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishExertsThisMinionToWoundTheCharacterHeIsSkirmishingAndNobodyForOneWhoseSkirmishHasNotBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters1 = scn.GetShadowCard("hunters1");
		var hunters2 = scn.GetShadowCard("hunters2");
		var hunters3 = scn.GetShadowCard("hunters3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(hunters1, hunters2, hunters3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hunters1}, new PhysicalCardImpl[]{boromir, hunters3});
		// A second copy in the same skirmish, put there by the Shadow player.
		scn.ShadowAssignToMinions(aragorn, hunters2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(hunters1));
		assertTrue(scn.ShadowActionAvailable(hunters2));
		// The copy assigned to Boromir is skirmishing nobody yet; the text names no skirmish, and the exertion can be paid.
		assertTrue(scn.ShadowActionAvailable(hunters3));
		assertEquals(0, scn.GetWoundsOn(hunters3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.ShadowUseCardAction(hunters3);

		// Paid for nothing: neither Aragorn, in the skirmish being fought, nor Boromir, whose skirmish is to come.
		assertEquals(1, scn.GetWoundsOn(hunters3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(hunters1));
		assertEquals(0, scn.GetWoundsOn(hunters2));
		scn.ShadowUseCardAction(hunters1);

		// This minion exerts, not the copy beside him, and Aragorn, whom he is skirmishing, is wounded.
		assertEquals(1, scn.GetWoundsOn(hunters1));
		assertEquals(0, scn.GetWoundsOn(hunters2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(hunters3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAnAllyHeIsSkirmishingAndAWoundKillsHimWithOneVitalityLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters1 = scn.GetShadowCard("hunters1");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(hunters1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);

		// Thrarin has exerted to take part in skirmishes: vitality 2, one left.
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, hunters1);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(hunters1));
		assertTrue(scn.ShadowActionAvailable(hunters1));
		scn.ShadowUseCardAction(hunters1);

		// A wound, not an exertion: an exhausted ally takes it and is killed.
		assertEquals(1, scn.GetWoundsOn(hunters1));
		assertInZone(Zone.DEAD, thrarin);
		// His skirmish is over with him gone, and it was the only one.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedHuntersIsNotOfferedTheAbilityWhileTheCopyBesideHimIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunters1 = scn.GetShadowCard("hunters1");
		var hunters2 = scn.GetShadowCard("hunters2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(hunters1, hunters2);
		scn.AddWoundsToChar(hunters1, 1);
		assertEquals(1, scn.GetWoundsOn(hunters1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hunters1);
		scn.ShadowAssignToMinions(aragorn, hunters2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 2 with one wound: he cannot exert.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hunters1));
		assertTrue(scn.ShadowActionAvailable(hunters2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
