package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_220_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "4_220");
					put("soldier2", "4_220");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; his text is about being overwhelmed
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2 of this path
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Desert Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>At the start of each skirmish involving this minion, you may remove (3) to wound a companion or ally he is skirmishing.
		*/

		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");

		assertEquals("Desert Soldier", soldier.getBlueprint().getTitle());
		assertNull(soldier.getBlueprint().getSubtitle());
		assertFalse(soldier.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, soldier.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, soldier.getBlueprint().getCulture());
		assertEquals(CardType.MINION, soldier.getBlueprint().getCardType());
		assertEquals(Race.MAN, soldier.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(soldier, Keyword.SOUTHRON));
		assertEquals(4, soldier.getBlueprint().getTwilightCost());
		assertEquals(9, soldier.getBlueprint().getStrength());
		assertEquals(2, soldier.getBlueprint().getVitality());
		assertEquals(4, soldier.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfItsSkirmishItMayRemoveThreeToWoundTheCompanionItSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowAcceptOptionalTrigger();

		// Boromir, in the other skirmish, is no one this minion is skirmishing.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtTheStartOfAnotherMinionsSkirmishItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{boromir, soldier2});
		scn.SetTwilight(6);

		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// Boromir's skirmish first: only the copy he skirmishes makes an offer.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir (7) loses to the Soldier (9): a wound, and he lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoTwilightInThePoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnAllyItWoundsTheAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, soldier}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(bounder);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnExhaustedCompanionTheWoundKillsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(soldier, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, soldier}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(3);

		// Vitality 3 with 2 wounds: exhausted, so a wound kills him where an exertion could not be made.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetTwilight());

		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowAcceptOptionalTrigger();

		// His skirmish is over with him; Aragorn's is the one left.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AtTheStartOfItsSkirmishInASecondAssignmentPhaseItIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());

		// Aragorn (8) loses to the Soldier (9), and Boromir (7) to the Troop (13) without being overwhelmed: both
		// minions live on into the second assignment phase, and Aragorn (vitality 4) lives through three wounds.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
