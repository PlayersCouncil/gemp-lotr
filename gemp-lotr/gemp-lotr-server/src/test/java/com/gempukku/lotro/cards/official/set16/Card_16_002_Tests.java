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

public class Card_16_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("corpses", "16_2");
					put("corpses2", "16_2");
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
	public void CandleCorpsesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 16
		 * Name: Candle Corpses
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>. Each time the Free Peoples player assigns this minion to a companion, you may spot another [wraith] Wraith and remove (2) to wound that companion.
		*/

		var scn = GetScenario();

		var corpses = scn.GetFreepsCard("corpses");

		assertEquals("Candle Corpses", corpses.getBlueprint().getTitle());
		assertNull(corpses.getBlueprint().getSubtitle());
		assertFalse(corpses.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, corpses.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, corpses.getBlueprint().getCulture());
		assertEquals(CardType.MINION, corpses.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, corpses.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(corpses, Keyword.ENDURING));
		assertEquals(3, corpses.getBlueprint().getTwilightCost());
		assertEquals(8, corpses.getBlueprint().getStrength());
		assertEquals(2, corpses.getBlueprint().getVitality());
		assertEquals(3, corpses.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerToACompanionItMayRemoveTwoToWoundThatCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// The Undead goes to Boromir in the same answer: only the Corpses' assignment sets anything off.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpses}, new PhysicalCardImpl[]{boromir, undead});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, corpses));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpses}, new PhysicalCardImpl[]{boromir, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, corpses));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithOneTwilightInThePoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpses}, new PhysicalCardImpl[]{boromir, undead});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(corpses));
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithNoOtherWraithWraithToSpotItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var corpses = scn.GetShadowCard("corpses");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		// The Corpses is itself a [wraith] Wraith, whom "another" leaves out; Lemenya is a [wraith] Nazgûl and
		// Corpse Lights a [sauron] Wraith.
		scn.MoveMinionsToTable(corpses, lemenya, lights);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpses}, new PhysicalCardImpl[]{boromir, lemenya}, new PhysicalCardImpl[]{gimli, lights});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(corpses));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToAnAllyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, corpses}, new PhysicalCardImpl[]{aragorn, undead});

		assertTrue(scn.IsCharAssignedAgainst(bounder, corpses));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(corpses));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToACompanionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Free Peoples player leaves the Corpses unassigned, and the Shadow player assigns it himself.
		scn.FreepsAssignToMinions(boromir, undead);
		scn.ShadowAssignToMinions(aragorn, corpses);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, corpses));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(corpses));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayersOwnAssignmentActionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertSame(aragorn, visitor.getAttachedTo());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Unexpected Visitor gives the minion an assignment action, and the one who uses it is the Shadow player.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(corpses));
		scn.ShadowUseCardAction(corpses);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, corpses));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(corpses));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayersOwnCardToTheRingBearerItWoundsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(eowyn));

		// Éowyn exerts and the Free Peoples player assigns a minion of his choice to the Ring-bearer.
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(corpses, undead));
		scn.FreepsChooseCard(corpses);

		assertTrue(scn.IsCharAssignedAgainst(frodo, corpses));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MadeByAShadowCardToAssignTheRingBearerTheFreePeoplesPlayerWhoPicksTheCorpsesIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var corpses = scn.GetShadowCard("corpses");
		var dread = scn.GetShadowCard("dread");
		var troop = scn.GetShadowCard("troop");
		var endcomes = scn.GetShadowCard("endcomes");

		scn.StartGame();
		scn.MoveCardsToHand(endcomes);
		// Five [moria] minions for The End Comes to spot: the Troop and four Runners.  Spirit of Dread is the other
		// [wraith] Wraith.
		scn.MoveMinionsToTable(corpses, dread, troop, scn.GetShadowCard("runner"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("runner3"), scn.GetShadowCard("runner4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's, but the one who assigns is the Free Peoples player: he picks the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(corpses, troop));
		scn.FreepsChooseCard(corpses);

		assertTrue(scn.IsCharAssignedAgainst(frodo, corpses));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedToAnExhaustedCompanionTheWoundKillsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		// Vitality 3 with 2 wounds: exhausted, so a wound kills him where an exertion could not be made.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetTwilight());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, corpses}, new PhysicalCardImpl[]{aragorn, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopyAssignedToACompanionWoundsItsOwnCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var corpses = scn.GetShadowCard("corpses");
		var corpses2 = scn.GetShadowCard("corpses2");

		scn.StartGame();
		// Each copy is the other's "another [wraith] Wraith".
		scn.MoveMinionsToTable(corpses, corpses2);
		scn.SkipToAssignments();
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpses}, new PhysicalCardImpl[]{boromir, corpses2});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses2));
		scn.ShadowChooseOptionalTrigger(corpses2);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowChooseOptionalTrigger(corpses);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseItWoundsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var corpses = scn.GetShadowCard("corpses");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(corpses, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Aragorn (8) does not beat the Corpses (8), nor Boromir (7) the Undead (9): both minions live on into
		// the second assignment phase, and Aragorn (vitality 4) lives through three wounds.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpses}, new PhysicalCardImpl[]{boromir, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, corpses);
		assertInZone(Zone.SHADOW_CHARACTERS, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpses}, new PhysicalCardImpl[]{boromir, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(corpses));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
