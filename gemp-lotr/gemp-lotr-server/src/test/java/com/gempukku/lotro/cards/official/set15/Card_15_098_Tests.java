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

public class Card_15_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentry", "15_98");
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
	public void BlackGateSentryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Gate Sentry
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to wound a companion it is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sentry");

		assertEquals("Black Gate Sentry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsTheSentryToWoundTheCompanionItIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, troop);
		scn.SkipToAssignments();
		// Both minions on Aragorn: the Troop is the Shadow player's leftover.
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(sentry));

		scn.ShadowUseCardAction(sentry);
		// The Sentry pays, not the other minion of its skirmish.
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
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

		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		// Vitality 4 with 3 wounds: exhausted, so an exertion could not be placed on him, and a wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sentry));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.ShadowActionAvailable(sentry));

		scn.ShadowUseCardAction(sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
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

		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{boromir, sentry});
		// Aragorn fights the Troop first; Boromir waits for the Sentry, assigned to it but not yet skirmishing it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(sentry));

		scn.ShadowUseCardAction(sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
		// A companion skirmishing another minion, and one assigned to the Sentry whose skirmish has not begun.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyItIsSkirmishingIsNoCompanionAndIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, sentry);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, sentry));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.ShadowActionAvailable(sentry));

		scn.ShadowUseCardAction(sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
		// Thrarin keeps the one wound of his own exertion and takes none: he has 2 vitality, so a wound would kill him.
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheSentryIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		// Vitality 3 with 2 wounds: it cannot exert.
		scn.AddWoundsToChar(sentry, 2);
		assertEquals(2, scn.GetWoundsOn(sentry));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, sentry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sentry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
