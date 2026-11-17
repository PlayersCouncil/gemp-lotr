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

public class Card_16_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faces", "16_4");
					put("faces2", "16_4");
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

					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition on a [gondor] companion; a Skirmish ability only
					put("stairs", "1_24");      // Stairs of Khazad-dûm: [dwarven] support-area condition; a Dwarf skirmishing a [moria] minion only
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] condition on Aragorn; an extra cost to play only
					put("fear", "1_278");       // Strength Born of Fear: [sauron] support-area condition; a Skirmish ability only
					put("sword", "1_112");      // Ranger's Sword: [gondor] possession on Aragorn, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeadFacesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 16
		 * Name: Dead Faces
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>. Each time the Free Peoples player assigns this minion to a companion, you may spot another [wraith] Wraith and remove (2) to discard a condition.
		*/

		var scn = GetScenario();

		var faces = scn.GetFreepsCard("faces");

		assertEquals("Dead Faces", faces.getBlueprint().getTitle());
		assertNull(faces.getBlueprint().getSubtitle());
		assertFalse(faces.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, faces.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, faces.getBlueprint().getCulture());
		assertEquals(CardType.MINION, faces.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, faces.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(faces, Keyword.ENDURING));
		assertEquals(2, faces.getBlueprint().getTwilightCost());
		assertEquals(4, faces.getBlueprint().getStrength());
		assertEquals(2, faces.getBlueprint().getVitality());
		assertEquals(3, faces.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerToACompanionItMayRemoveTwoToDiscardAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(aragorn, saga, sword);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.ATTACHED, saga);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");
		var legacy = scn.GetShadowCard("legacy");
		var fear = scn.GetShadowCard("fear");
		scn.AttachCardsTo(aragorn, legacy);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertSame(aragorn, saga.getAttachedTo());
		assertSame(aragorn, sword.getAttachedTo());
		assertSame(aragorn, legacy.getAttachedTo());
		assertInZone(Zone.SUPPORT, stairs, fear);

		// The Undead goes to Boromir in the same answer: only the Faces' assignment sets anything off.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, faces}, new PhysicalCardImpl[]{boromir, undead});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());

		// "A condition" of either side, borne or in a support area; a possession is no condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, stairs, legacy, fear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saga);

		assertInDiscard(saga);
		assertSame(aragorn, sword.getAttachedTo());
		assertSame(aragorn, legacy.getAttachedTo());
		assertInZone(Zone.SUPPORT, stairs, fear);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, faces));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, faces}, new PhysicalCardImpl[]{boromir, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, faces));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithOneTwilightInThePoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, faces}, new PhysicalCardImpl[]{boromir, undead});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(faces));
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithNoOtherWraithWraithToSpotItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var faces = scn.GetShadowCard("faces");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		// The Faces is itself a [wraith] Wraith, whom "another" leaves out; Lemenya is a [wraith] Nazgûl and
		// Corpse Lights a [sauron] Wraith.
		scn.MoveMinionsToTable(faces, lemenya, lights);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, faces}, new PhysicalCardImpl[]{boromir, lemenya}, new PhysicalCardImpl[]{gimli, lights});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(faces));
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToAnAllyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveMinionsToTable(faces, undead);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, faces}, new PhysicalCardImpl[]{aragorn, undead});

		assertTrue(scn.IsCharAssignedAgainst(bounder, faces));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(faces));
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToACompanionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);

		// The Free Peoples player leaves the Faces unassigned, and the Shadow player assigns it himself.
		scn.FreepsAssignToMinions(boromir, undead);
		scn.ShadowAssignToMinions(aragorn, faces);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, faces));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(faces));
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayersOwnAssignmentActionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertSame(aragorn, visitor.getAttachedTo());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);

		// Unexpected Visitor gives the minion an assignment action, and the one who uses it is the Shadow player.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(faces));
		scn.ShadowUseCardAction(faces);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, faces));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(faces));
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayersOwnCardToTheRingBearerItDiscardsACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(0, scn.GetWoundsOn(eowyn));

		// Éowyn exerts and the Free Peoples player assigns a minion of his choice to the Ring-bearer.
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(faces, undead));
		scn.FreepsChooseCard(faces);

		assertTrue(scn.IsCharAssignedAgainst(frodo, faces));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertInDiscard(stairs);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MadeByAShadowCardToAssignTheRingBearerTheFreePeoplesPlayerWhoPicksTheFacesDiscardsACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var faces = scn.GetShadowCard("faces");
		var dread = scn.GetShadowCard("dread");
		var troop = scn.GetShadowCard("troop");
		var endcomes = scn.GetShadowCard("endcomes");

		scn.StartGame();
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(endcomes);
		// Five [moria] minions for The End Comes to spot: the Troop and four Runners.  Spirit of Dread is the other
		// [wraith] Wraith.
		scn.MoveMinionsToTable(faces, dread, troop, scn.GetShadowCard("runner"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("runner3"), scn.GetShadowCard("runner4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stairs);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's, but the one who assigns is the Free Peoples player: he picks the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(faces, troop));
		scn.FreepsChooseCard(faces);

		assertTrue(scn.IsCharAssignedAgainst(frodo, faces));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertInDiscard(stairs);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayTheTwilightIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, sword);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertSame(aragorn, sword.getAttachedTo());

		// The trigger has a cost, so with nothing to discard it is still offered, and paid for nothing.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, faces}, new PhysicalCardImpl[]{boromir, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertSame(aragorn, sword.getAttachedTo());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopyAssignedToACompanionDiscardsACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.ATTACHED, saga);

		var faces = scn.GetShadowCard("faces");
		var faces2 = scn.GetShadowCard("faces2");

		scn.StartGame();
		// Each copy is the other's "another [wraith] Wraith".
		scn.MoveMinionsToTable(faces, faces2);
		scn.SkipToAssignments();
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertSame(aragorn, saga.getAttachedTo());
		assertInZone(Zone.SUPPORT, stairs);

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, faces}, new PhysicalCardImpl[]{boromir, faces2});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces2));
		scn.ShadowChooseOptionalTrigger(faces2);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, stairs));
		scn.ShadowChooseCard(stairs);
		assertInDiscard(stairs);
		assertSame(aragorn, saga.getAttachedTo());

		// The other copy's own offer, and the one condition left goes unasked.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowChooseOptionalTrigger(faces);
		assertEquals(1, scn.GetTwilight());
		assertInDiscard(saga, stairs);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseItDiscardsASecondCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.ATTACHED, saga);

		var faces = scn.GetShadowCard("faces");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(faces, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertSame(aragorn, saga.getAttachedTo());
		assertInZone(Zone.SUPPORT, stairs);

		// Boromir (7) beats the Faces (4) without overwhelming it, and the Undead (9) beats Aragorn (8): both
		// minions live on into the second assignment phase.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, faces}, new PhysicalCardImpl[]{aragorn, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(stairs);
		assertInDiscard(stairs);
		assertEquals(1, scn.GetTwilight());

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(faces));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, faces);
		assertInZone(Zone.SHADOW_CHARACTERS, undead);
		scn.SkipToAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertSame(aragorn, saga.getAttachedTo());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, faces}, new PhysicalCardImpl[]{aragorn, undead});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(faces));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(saga);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
