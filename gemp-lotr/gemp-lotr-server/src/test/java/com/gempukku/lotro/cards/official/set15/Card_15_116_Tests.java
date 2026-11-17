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

public class Card_15_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "15_116");
					put("scout2", "15_116");    // a second copy, in the same skirmish
					put("troop", "1_177");       // Goblin Patrol Troop: a minion with no text, strength 13, for the other skirmish
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
	public void ScoutingOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Scouting Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to wound a companion it is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout");

		assertEquals("Scouting Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsTheScoutingOrcToWoundTheCompanionItIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir);

		var scout = scn.GetShadowCard("scout");
		var scout2 = scn.GetShadowCard("scout2");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, scout2);
		scn.SkipToAssignments();
		// Both copies on Aragorn: the second is the Shadow player's leftover.
		scn.FreepsAssignToMinions(aragorn, scout);
		scn.ShadowAssignToMinions(aragorn, scout2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scout));

		scn.ShadowUseCardAction(scout);
		// The copy used pays, not the other copy in the same skirmish.
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
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

		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		// Vitality 4 with 3 wounds: exhausted, so an exertion could not be placed on him, and a wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(scout));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.ShadowActionAvailable(scout));

		scn.ShadowUseCardAction(scout);
		assertEquals(1, scn.GetWoundsOn(scout));
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

		var scout = scn.GetShadowCard("scout");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{boromir, scout});
		// Aragorn fights the Troop first; Boromir waits for the Scouting Orc, assigned to it but not yet skirmishing it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scout));

		scn.ShadowUseCardAction(scout);
		assertEquals(1, scn.GetWoundsOn(scout));
		// A companion skirmishing another minion, and one assigned to the Scouting Orc whose skirmish has not begun.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyItIsSkirmishingIsNoCompanionAndIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, scout);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, scout));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowActionAvailable(scout));

		scn.ShadowUseCardAction(scout);
		assertEquals(1, scn.GetWoundsOn(scout));
		// Thrarin keeps the one wound of his own exertion and takes none: he has 2 vitality, so a wound would kill him.
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheScoutingOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		// Vitality 2 with 1 wound: it cannot exert.
		scn.AddWoundsToChar(scout, 1);
		assertEquals(1, scn.GetWoundsOn(scout));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scout));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
