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

public class Card_01_272_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band1", "1_272");
					put("band2", "1_272");   // not unique: "this minion" is read beside other copies
					put("band3", "1_272");
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
	public void OrcWarBandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc War Band
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Exert this minion to wound a character it is skirmishing.
		*/

		var scn = GetScenario();

		var band = scn.GetFreepsCard("band1");

		assertEquals("Orc War Band", band.getBlueprint().getTitle());
		assertNull(band.getBlueprint().getSubtitle());
		assertFalse(band.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, band.getBlueprint().getSide());
		assertEquals(Culture.SAURON, band.getBlueprint().getCulture());
		assertEquals(CardType.MINION, band.getBlueprint().getCardType());
		assertEquals(Race.ORC, band.getBlueprint().getRace());
		assertEquals(4, band.getBlueprint().getTwilightCost());
		assertEquals(10, band.getBlueprint().getStrength());
		assertEquals(2, band.getBlueprint().getVitality());
		assertEquals(6, band.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishExertsThisMinionToWoundTheCharacterItIsSkirmishingAndNobodyForOneWhoseSkirmishHasNotBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band1 = scn.GetShadowCard("band1");
		var band2 = scn.GetShadowCard("band2");
		var band3 = scn.GetShadowCard("band3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(band1, band2, band3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, band1}, new PhysicalCardImpl[]{boromir, band3});
		// A second copy in the same skirmish, put there by the Shadow player.
		scn.ShadowAssignToMinions(aragorn, band2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(band1));
		assertTrue(scn.ShadowActionAvailable(band2));
		// The copy assigned to Boromir is skirmishing nobody yet; the text names no skirmish, and the exertion can be paid.
		assertTrue(scn.ShadowActionAvailable(band3));
		assertEquals(0, scn.GetWoundsOn(band3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.ShadowUseCardAction(band3);

		// Paid for nothing: neither Aragorn, in the skirmish being fought, nor Boromir, whose skirmish is to come.
		assertEquals(1, scn.GetWoundsOn(band3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(band1));
		assertEquals(0, scn.GetWoundsOn(band2));
		scn.ShadowUseCardAction(band1);

		// This minion exerts, not the copy beside it, and Aragorn, whom it is skirmishing, is wounded once.
		assertEquals(1, scn.GetWoundsOn(band1));
		assertEquals(0, scn.GetWoundsOn(band2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(band3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAnAllyItIsSkirmishingAndAWoundKillsHimWithOneVitalityLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band1 = scn.GetShadowCard("band1");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(band1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);

		// Thrarin has exerted to take part in skirmishes: vitality 2, one left.
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, band1);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(band1));
		assertTrue(scn.ShadowActionAvailable(band1));
		scn.ShadowUseCardAction(band1);

		// A wound, not an exertion: an exhausted ally takes it and is killed.
		assertEquals(1, scn.GetWoundsOn(band1));
		assertInZone(Zone.DEAD, thrarin);
		// His skirmish is over with him gone, and it was the only one.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedWarBandIsNotOfferedTheAbilityWhileTheCopyBesideItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band1 = scn.GetShadowCard("band1");
		var band2 = scn.GetShadowCard("band2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(band1, band2);
		scn.AddWoundsToChar(band1, 1);
		assertEquals(1, scn.GetWoundsOn(band1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band1);
		scn.ShadowAssignToMinions(aragorn, band2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 2 with one wound: he cannot exert.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(band1));
		assertTrue(scn.ShadowActionAvailable(band2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
