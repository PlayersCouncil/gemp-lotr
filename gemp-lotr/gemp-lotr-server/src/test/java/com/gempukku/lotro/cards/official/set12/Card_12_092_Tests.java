package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_092_Tests
{

	// Fellowship path: site 2 is Ettenmoors 1_331 (plains), site 3 Rivendell Valley 1_341 (sanctuary), site 4
	// Balin's Tomb 1_343 (underground).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dreg", "12_92");
					put("dreg2", "12_92");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a second companion; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// King path: site 3 is The Dimholt 8_117, underground and a sanctuary.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dreg", "12_92");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion with a maneuver ability only
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcDregStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Dreg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to a skirmish, if it is not at an underground site, remove (2) or discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dreg");

		assertEquals("Orc Dreg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedAtAPlainsSiteItsOwnerChoosesToRemove2OrDiscardItAndEachCopyAsksForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var dreg = scn.GetShadowCard("dreg");
		var dreg2 = scn.GetShadowCard("dreg2");

		scn.StartGame();
		scn.MoveMinionsToTable(dreg, dreg2);
		scn.SkipToAssignments();

		scn.SetTwilight(5);
		int twilight = 5;
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, dreg);
		assertInZone(Zone.SHADOW_CHARACTERS, dreg2);

		// The Free Peoples player assigns the first copy only; the second is left over for the Shadow player.
		scn.FreepsAssignToMinions(aragorn, dreg);

		assertTrue(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertTrue(scn.ShadowChoiceAvailable("Discard this minion"));
		scn.ShadowChoose("Remove 2 twilight");

		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, dreg));
		assertInZone(Zone.SHADOW_CHARACTERS, dreg2);
		assertFalse(scn.IsCharAssigned(dreg2));

		// The Shadow player assigns the left-over copy himself: it is assigned too, and asks for itself.
		scn.ShadowAssignToMinions(boromir, dreg2);

		assertTrue(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		scn.ShadowChoose("Discard this minion");

		assertInZone(Zone.DISCARD, dreg2);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, dreg));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithOnly1TwilightInThePoolItIsDiscardedWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dreg = scn.GetShadowCard("dreg");

		scn.StartGame();
		scn.MoveMinionsToTable(dreg);
		scn.SkipToAssignments();
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, dreg);

		scn.FreepsAssignToMinions(aragorn, dreg);

		// A choice is of what can be done in full: (2) cannot be removed from a pool of 1.
		assertFalse(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertInZone(Zone.DISCARD, dreg);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.IsCharAssigned(aragorn));
		// With no minion left there is no skirmish, and the game goes on to the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AssignedAtBalinsTombAnUndergroundSiteItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dreg = scn.GetShadowCard("dreg");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(dreg);
		// The fellowship moves on to Balin's Tomb, site 4.
		scn.SkipToAssignments();

		assertEquals(4, scn.GetCurrentSite().getBlueprint().getSiteNumber());
		scn.SetTwilight(5);
		int twilight = 5;
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, dreg);

		scn.FreepsAssignToMinions(aragorn, dreg);

		assertFalse(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, dreg));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAtTheDimholtAnUndergroundSanctuaryItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dreg = scn.GetShadowCard("dreg");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(dreg);
		// The fellowship moves on to The Dimholt, site 3 of the King path.
		scn.SkipToAssignments();

		assertEquals("The Dimholt", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetTwilight(5);
		int twilight = 5;
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, dreg);

		scn.FreepsAssignToMinions(aragorn, dreg);

		assertFalse(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, dreg));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnotherMinionAssignedAsksNothingAndTheShadowPlayersAssignmentOfItAsks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var dreg = scn.GetShadowCard("dreg");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(dreg, troop);
		scn.SkipToAssignments();

		scn.SetTwilight(5);
		int twilight = 5;
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, dreg);

		// The Troop is assigned and the Orc Dreg is left over: nothing is asked of its owner.
		scn.FreepsAssignToMinions(aragorn, troop);

		assertFalse(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertFalse(scn.IsCharAssigned(dreg));
		assertEquals(twilight, scn.GetTwilight());

		scn.ShadowAssignToMinions(boromir, dreg);

		assertTrue(scn.ShadowChoiceAvailable("Discard this minion"));
		scn.ShadowChoose("Remove 2 twilight");

		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(boromir, dreg));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseItAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dreg = scn.GetShadowCard("dreg");

		scn.StartGame();
		scn.MoveMinionsToTable(dreg);
		scn.SkipToAssignments();

		scn.SetTwilight(5);
		int twilight = 5;
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, dreg);

		scn.FreepsAssignToMinions(aragorn, dreg);
		scn.ShadowChoose("Remove 2 twilight");
		assertEquals(twilight - 2, scn.GetTwilight());

		// Strength 10 against Aragorn's 8: the Orc Dreg wins, and lives on into the second move.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, dreg);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		// Site 3, Rivendell Valley, is no underground site.
		scn.SkipToAssignments();

		assertEquals(3, scn.GetCurrentSite().getBlueprint().getSiteNumber());
		scn.SetTwilight(4);
		int twilight2 = 4;
		assertEquals(twilight2, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, dreg);

		assertTrue(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertTrue(scn.ShadowChoiceAvailable("Discard this minion"));
		scn.ShadowChoose("Remove 2 twilight");

		assertEquals(twilight2 - 2, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, dreg));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
