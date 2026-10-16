package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jarnsmid", "12_30");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("guardian", "5_17");    // Forest Guardian: [gandalf] Ent companion; his ability is an assignment one
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, so no companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion; his ability is a skirmish one
					put("troop1", "1_177");     // Goblin Patrol Troop: minion with no text, strength 13
					put("troop2", "1_177");
					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5; his texts are about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void JarnsmidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Járnsmid, Barding Emissary
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: At the start of each skirmish involving Járnsmid, you may remove a burden (or 2 burdens if you can spot another [gandalf] companion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jarnsmid");

		assertEquals("Járnsmid", card.getBlueprint().getTitle());
		assertEquals("Barding Emissary", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfHisSkirmishRemovesOneBurdenWhenNoOtherGandalfCompanionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var barliman = scn.GetFreepsCard("barliman");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(jarnsmid, boromir);
		scn.MoveAlliesToTable(barliman);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(jarnsmid, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(jarnsmid);

		// A [gandalf] ally and a [gondor] companion are no other [gandalf] companion, and Járnsmid is not "another".
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RemovesTwoBurdensUnaskedWhenGandalfIsAnotherGandalfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(jarnsmid, gandalf);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(jarnsmid, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(jarnsmid);

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsAcceptOptionalTrigger();

		// The branch in parentheses is taken whenever it is available (Q-0035): no question, two burdens.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RemovesTwoBurdensWhenAnEntIsTheOtherGandalfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(jarnsmid, guardian);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(jarnsmid, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(jarnsmid);

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedAtTheStartOfAnotherCompanionsSkirmishButIsAtHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(jarnsmid, boromir);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, troop1}, new PhysicalCardImpl[]{jarnsmid, troop2});
		scn.ShadowDeclineAssignments();

		// Járnsmid is assigned, but his skirmish comes second.
		scn.FreepsResolveSkirmish(boromir);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir's 7 against 13: he loses and is wounded, not overwhelmed.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetBurdens());

		scn.FreepsResolveSkirmish(jarnsmid);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(jarnsmid, gandalf);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(jarnsmid, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(jarnsmid);

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningLeavesTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(jarnsmid, gandalf);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(jarnsmid, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(jarnsmid);

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedAgainAtTheStartOfAFierceSkirmishInvolvingHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		scn.MoveCompanionsToTable(jarnsmid);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(jarnsmid, pursuer);
		scn.FreepsResolveSkirmish(jarnsmid);

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());

		// Járnsmid's 4 against the Pursuer's 5: he loses and takes a wound; the fierce minion fights again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(jarnsmid));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(jarnsmid, pursuer);
		scn.FreepsResolveSkirmish(jarnsmid);

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
