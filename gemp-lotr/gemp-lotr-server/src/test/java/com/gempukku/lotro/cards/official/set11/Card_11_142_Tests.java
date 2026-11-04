package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_142_Tests
{

	// Site 2 is Ettenmoors 1_331 (plains, no underground site); site 4 is Balin's Tomb 1_343 (underground).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "11_142");
					put("goblin2", "11_142");
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a maneuver ability used only on purpose
					// Thrarin, Dwarven Smith: a Dwarf ally, home 3F; "Maneuver: Exert Thrarin to allow him to participate
					// in archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 3 is The Dimholt 8_117: underground and a sanctuary.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "11_142");
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnyieldingGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Unyielding Goblin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> If this minion is at an underground site, exert it to exert a companion it is skirmishing.
		*/

		var scn = GetScenario();

		var goblin = scn.GetFreepsCard("goblin");

		assertEquals("Unyielding Goblin", goblin.getBlueprint().getTitle());
		assertNull(goblin.getBlueprint().getSubtitle());
		assertFalse(goblin.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, goblin.getBlueprint().getSide());
		assertEquals(Culture.ORC, goblin.getBlueprint().getCulture());
		assertEquals(CardType.MINION, goblin.getBlueprint().getCardType());
		assertEquals(Race.ORC, goblin.getBlueprint().getRace());
		assertEquals(2, goblin.getBlueprint().getTwilightCost());
		assertEquals(5, goblin.getBlueprint().getStrength());
		assertEquals(2, goblin.getBlueprint().getVitality());
		assertEquals(4, goblin.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAnUndergroundSiteItExertsItselfToExertTheCompanionItIsSkirmishingAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);

		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(goblin, goblin2, troop);
		scn.SkipToAssignments();

		assertEquals(4, scn.GetCurrentSiteNumber());
		// Both Goblins on Boromir, the second by the Shadow player; Aragorn's skirmish, against the Troop, waits.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, goblin},
				new PhysicalCardImpl[]{aragorn, troop}
		);
		scn.ShadowAssignToMinions(boromir, goblin2);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);

		// The Goblin used exerts, and not the other copy beside it; Boromir, the one companion it is skirmishing,
		// is exerted unasked, and Aragorn, assigned to a skirmish still to come, is not.
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExhaustedItIsNotOfferedWhileAnotherCopyInTheSameSkirmishIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(goblin, goblin2);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(goblin, 1);
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, goblin);
		scn.ShadowAssignToMinions(boromir, goblin2);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(goblin));
		assertTrue(scn.ShadowActionAvailable(goblin2));
		assertEquals(0, scn.GetWoundsOn(boromir));
	}

	@Test
	public void AtASiteThatIsNotUndergroundItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.SkipToAssignments();

		// Ettenmoors, site 2: plains.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(boromir, goblin);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(goblin));
		assertEquals(0, scn.GetWoundsOn(boromir));
	}

	@Test
	public void AtAnUndergroundSanctuaryItIsOfferedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(goblin);
		scn.SkipToAssignments();

		// The Dimholt, site 3: underground and a sanctuary.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(boromir, goblin);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);

		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnAllyItIsUsedAndExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(thrarin);

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		// Vitality 4, so that after his own exertion Thrarin could still be exerted (no printed card raises it).
		scn.ApplyAdHocModifier(new VitalityModifier(null, thrarin, null, 2));
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(goblin);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, goblin);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(boromir));
		// The ability has a cost, and is offered with no companion in its skirmish.
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);

		// The ally it is skirmishing is no companion, and Boromir is in no skirmish: nobody is exerted.
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheCompanionItIsSkirmishingExhaustedTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(goblin);
		// Vitality 3 with 2 wounds: Boromir is exhausted, and a third wound would kill him.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, goblin);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);

		// The Goblin exerts; an exhausted companion cannot be exerted, and he is not wounded either.
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
