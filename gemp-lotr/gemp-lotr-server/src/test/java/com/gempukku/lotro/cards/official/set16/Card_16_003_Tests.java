package com.gempukku.lotro.cards.official.set16;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_16_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wisp", "16_3");
					put("undead", "16_6");      // Undead of Angmar: [wraith] Wraith; wounded companions resistance -2, a Shadow ability
					put("dread", "16_5");       // Spirit of Dread: [wraith] Wraith of another title; no heals, a Skirmish ability
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] minion who is no Wraith (a Nazgûl)
					put("lights", "6_99");      // Corpse Lights: a Wraith who is not [wraith] ([sauron]); a Skirmish ability
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("runner", "1_178");     // Goblin Runner: [moria] minion; adds (2) only when played, and is cheated in here
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					// The End Comes: "Assignment: Spot 5 [moria] minions to make the Free Peoples player assign the
					// Ring-bearer to a skirmish"
					put("endcomes", "1_169");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; his text is about being overwhelmed
					put("eowyn", "17_96");      // Éowyn, Northwoman: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer"
					put("gimli", "1_13");       // Gimli, Son of Glóin: a third companion; a Skirmish ability only
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2 of this path
					// Unexpected Visitor: on a [gondor] companion, gives each minion "Assignment: Assign this minion to bearer"
					put("visitor", "7_126");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CovetousWispStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 16
		 * Name: Covetous Wisp
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>. Each time the Free Peoples player assigns Covetous Wisp to a companion, you may spot another [wraith] Wraith and remove (2) to add a burden.
		*/

		var scn = GetScenario();

		var wisp = scn.GetFreepsCard("wisp");

		assertEquals("Covetous Wisp", wisp.getBlueprint().getTitle());
		assertNull(wisp.getBlueprint().getSubtitle());
		assertTrue(wisp.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, wisp.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, wisp.getBlueprint().getCulture());
		assertEquals(CardType.MINION, wisp.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, wisp.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(wisp, Keyword.ENDURING));
		assertEquals(2, wisp.getBlueprint().getTwilightCost());
		assertEquals(4, wisp.getBlueprint().getStrength());
		assertEquals(2, wisp.getBlueprint().getVitality());
		assertEquals(3, wisp.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerToACompanionItMayRemoveTwoToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		// The Undead goes to Boromir in the same answer: only the Wisp's assignment sets anything off.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, wisp}, new PhysicalCardImpl[]{boromir, undead});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wisp));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, wisp));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, wisp}, new PhysicalCardImpl[]{boromir, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wisp));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, wisp));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithOneTwilightInThePoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, wisp}, new PhysicalCardImpl[]{boromir, undead});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wisp));
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithNoOtherWraithWraithToSpotItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var wisp = scn.GetShadowCard("wisp");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		// The Wisp is itself a [wraith] Wraith, whom "another" leaves out; Lemenya is a [wraith] Nazgûl and
		// Corpse Lights a [sauron] Wraith.
		scn.MoveMinionsToTable(wisp, lemenya, lights);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, wisp}, new PhysicalCardImpl[]{boromir, lemenya}, new PhysicalCardImpl[]{gimli, lights});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wisp));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToAnAllyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, wisp}, new PhysicalCardImpl[]{aragorn, undead});

		assertTrue(scn.IsCharAssignedAgainst(bounder, wisp));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wisp));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToACompanionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		// The Free Peoples player leaves the Wisp unassigned, and the Shadow player assigns it himself.
		scn.FreepsAssignToMinions(boromir, undead);
		scn.ShadowAssignToMinions(aragorn, wisp);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, wisp));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wisp));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayersOwnAssignmentActionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertSame(aragorn, visitor.getAttachedTo());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		// Unexpected Visitor gives the minion an assignment action, and the one who uses it is the Shadow player.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(wisp));
		scn.ShadowUseCardAction(wisp);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, wisp));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wisp));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayersOwnCardToTheRingBearerItAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(eowyn));

		// Éowyn exerts and the Free Peoples player assigns a minion of his choice to the Ring-bearer.
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(wisp, undead));
		scn.FreepsChooseCard(wisp);

		assertTrue(scn.IsCharAssignedAgainst(frodo, wisp));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wisp));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MadeByAShadowCardToAssignTheRingBearerTheFreePeoplesPlayerWhoPicksTheWispAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var wisp = scn.GetShadowCard("wisp");
		var dread = scn.GetShadowCard("dread");
		var troop = scn.GetShadowCard("troop");
		var endcomes = scn.GetShadowCard("endcomes");

		scn.StartGame();
		scn.MoveCardsToHand(endcomes);
		// Five [moria] minions for The End Comes to spot: the Troop and four Runners.  Spirit of Dread is the other
		// [wraith] Wraith.
		scn.MoveMinionsToTable(wisp, dread, troop, scn.GetShadowCard("runner"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("runner3"), scn.GetShadowCard("runner4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's, but the one who assigns is the Free Peoples player: he picks the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wisp, troop));
		scn.FreepsChooseCard(wisp);

		assertTrue(scn.IsCharAssignedAgainst(frodo, wisp));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wisp));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseItAddsASecondBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var wisp = scn.GetShadowCard("wisp");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		// Boromir (7) beats the Wisp (4) without overwhelming it, and the Undead (9) beats Aragorn (8): both
		// minions live on into the second assignment phase.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, wisp}, new PhysicalCardImpl[]{aragorn, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wisp));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetTwilight());

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(wisp));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, wisp);
		assertInZone(Zone.SHADOW_CHARACTERS, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, wisp}, new PhysicalCardImpl[]{aragorn, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wisp));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
