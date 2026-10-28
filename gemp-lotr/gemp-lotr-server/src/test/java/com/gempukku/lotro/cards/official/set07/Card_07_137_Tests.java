package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spearman", "7_137");
					put("spearman2", "7_137");
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
	public void DesertSpearmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Desert Spearman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>At the start of each skirmish involving this minion, you may remove (3) to wound a companion or ally he is skirmishing.
		*/

		var scn = GetScenario();

		var spearman = scn.GetFreepsCard("spearman");

		assertEquals("Desert Spearman", spearman.getBlueprint().getTitle());
		assertNull(spearman.getBlueprint().getSubtitle());
		assertFalse(spearman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spearman.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, spearman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, spearman.getBlueprint().getCardType());
		assertEquals(Race.MAN, spearman.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(spearman, Keyword.SOUTHRON));
		assertEquals(3, spearman.getBlueprint().getTwilightCost());
		assertEquals(8, spearman.getBlueprint().getStrength());
		assertEquals(2, spearman.getBlueprint().getVitality());
		assertEquals(4, spearman.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfItsSkirmishItMayRemoveThreeToWoundTheCompanionItSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var spearman = scn.GetShadowCard("spearman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman));
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

		var spearman = scn.GetShadowCard("spearman");
		var spearman2 = scn.GetShadowCard("spearman2");

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, spearman2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman}, new PhysicalCardImpl[]{boromir, spearman2});
		scn.SetTwilight(6);

		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// Boromir's skirmish first: only the copy he skirmishes makes an offer.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spearman));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir (7) loses to the Spearman (8): a wound, and he lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spearman2));
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

		var spearman = scn.GetShadowCard("spearman");

		scn.StartGame();
		scn.MoveMinionsToTable(spearman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, spearman);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spearman));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var spearman = scn.GetShadowCard("spearman");

		scn.StartGame();
		scn.MoveMinionsToTable(spearman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, spearman);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman));
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

		var spearman = scn.GetShadowCard("spearman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, troop);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, spearman}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(bounder);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman));
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

		var spearman = scn.GetShadowCard("spearman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(spearman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, spearman}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(3);

		// Vitality 3 with 2 wounds: exhausted, so a wound kills him where an exertion could not be made.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetTwilight());

		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman));
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

		var spearman = scn.GetShadowCard("spearman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());

		// Aragorn (8) ties the Spearman (8) and so loses, and Boromir (7) to the Troop (13) without being overwhelmed: both
		// minions live on into the second assignment phase, and Aragorn (vitality 4) lives through three wounds.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, spearman);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spearman));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
