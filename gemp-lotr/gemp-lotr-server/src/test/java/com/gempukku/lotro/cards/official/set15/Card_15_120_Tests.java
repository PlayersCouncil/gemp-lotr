package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chief", "15_120");
					put("chief2", "15_120");     // a second copy: the card is not unique
					put("troop", "1_177");       // Goblin Patrol Troop: a minion with no text, strength 13
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: companion, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");      // Boromir, Son of Denethor: companion; a skirmish ability used only on purpose
					put("thrarin", "1_27");      // Thrarin, Dwarven Smith: an ally who exerts in the maneuver phase to take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VeteranWarChiefStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Veteran War Chief
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to wound a companion it is skirmishing.
		*/

		var scn = GetScenario();

		var chief = scn.GetFreepsCard("chief");

		assertEquals("Veteran War Chief", chief.getBlueprint().getTitle());
		assertNull(chief.getBlueprint().getSubtitle());
		assertFalse(chief.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, chief.getBlueprint().getSide());
		assertEquals(Culture.ORC, chief.getBlueprint().getCulture());
		assertEquals(CardType.MINION, chief.getBlueprint().getCardType());
		assertEquals(Race.ORC, chief.getBlueprint().getRace());
		assertEquals(4, chief.getBlueprint().getTwilightCost());
		assertEquals(10, chief.getBlueprint().getStrength());
		assertEquals(2, chief.getBlueprint().getVitality());
		assertEquals(4, chief.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsTheChiefToWoundTheCompanionItIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir);

		var chief = scn.GetShadowCard("chief");
		var chief2 = scn.GetShadowCard("chief2");

		scn.StartGame();
		scn.MoveMinionsToTable(chief, chief2);
		scn.SkipToAssignments();
		// Both copies on Aragorn: the second is the Shadow player's leftover.
		scn.FreepsAssignToMinions(aragorn, chief);
		scn.ShadowAssignToMinions(aragorn, chief2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(chief2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(chief));
		assertTrue(scn.ShadowActionAvailable(chief2));

		scn.ShadowUseCardAction(chief);
		// The copy used pays, not the other copy in the same skirmish.
		assertEquals(1, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(chief2));
		// One wound, on the one companion it is skirmishing (chosen unasked); the companions in no skirmish are untouched.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheWoundKillsAnExhaustedCompanionItIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		scn.MoveMinionsToTable(chief);
		// Vitality 4 with 3 wounds: exhausted, so an exertion could not be placed on him, and a wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chief);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chief));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.ShadowActionAvailable(chief));

		scn.ShadowUseCardAction(chief);
		assertEquals(1, scn.GetWoundsOn(chief));
		assertInZone(Zone.DEAD, aragorn);
		// His was the only skirmish, and it is over with him gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InAnotherSkirmishTheAbilityIsPaidForAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var chief = scn.GetShadowCard("chief");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chief, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{boromir, chief});
		// Aragorn fights the Troop first; Boromir waits for the Chief, assigned to it but not yet skirmishing it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(chief));

		scn.ShadowUseCardAction(chief);
		assertEquals(1, scn.GetWoundsOn(chief));
		// A companion skirmishing another minion, and one assigned to the Chief whose skirmish has not begun.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyItIsSkirmishingIsNoCompanionAndIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		scn.MoveMinionsToTable(chief);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, chief);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, chief));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertTrue(scn.ShadowActionAvailable(chief));

		scn.ShadowUseCardAction(chief);
		assertEquals(1, scn.GetWoundsOn(chief));
		// Thrarin keeps the one wound of his own exertion and takes none: he has 2 vitality, so a wound would kill him.
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheChiefIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		scn.MoveMinionsToTable(chief);
		// Vitality 2 with 1 wound: it cannot exert.
		scn.AddWoundsToChar(chief, 1);
		assertEquals(1, scn.GetWoundsOn(chief));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chief);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chief));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(chief));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
