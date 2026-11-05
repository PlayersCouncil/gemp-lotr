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

public class Card_04_222_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "4_222");
					put("warrior2", "4_222");
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
	public void DesertWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Desert Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>At the start of each skirmish involving this minion, you may remove (3) to wound a companion or ally he is skirmishing.
		*/

		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");

		assertEquals("Desert Warrior", warrior.getBlueprint().getTitle());
		assertNull(warrior.getBlueprint().getSubtitle());
		assertFalse(warrior.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, warrior.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, warrior.getBlueprint().getCulture());
		assertEquals(CardType.MINION, warrior.getBlueprint().getCardType());
		assertEquals(Race.MAN, warrior.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(warrior, Keyword.SOUTHRON));
		assertEquals(2, warrior.getBlueprint().getTwilightCost());
		assertEquals(6, warrior.getBlueprint().getStrength());
		assertEquals(2, warrior.getBlueprint().getVitality());
		assertEquals(4, warrior.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfItsSkirmishItMayRemoveThreeToWoundTheCompanionItSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var warrior = scn.GetShadowCard("warrior");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warrior}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
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

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warrior}, new PhysicalCardImpl[]{boromir, warrior2});
		scn.SetTwilight(6);

		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// Boromir's skirmish first: only the copy he skirmishes makes an offer.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir (7) beats the Warrior (6), who lives through the wound (vitality 2); Boromir takes no more.
		assertEquals(0, scn.GetWoundsOn(warrior2));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(warrior2));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior2));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoTwilightInThePoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
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

		var warrior = scn.GetShadowCard("warrior");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, troop);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, warrior}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(bounder);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
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

		var warrior = scn.GetShadowCard("warrior");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(warrior, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, warrior}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(3);

		// Vitality 3 with 2 wounds: exhausted, so a wound kills him where an exertion could not be made.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetTwilight());

		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
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

		var warrior = scn.GetShadowCard("warrior");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warrior}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());

		// Aragorn (8) beats the Warrior (6), who lives through the wound (vitality 2), and Boromir (7) loses to the
		// Troop (13) without being overwhelmed: both minions live on into the second assignment phase.
		assertEquals(0, scn.GetWoundsOn(warrior));
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warrior}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
