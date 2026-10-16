package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_330_Tests
{

	// Buckleberry Ferry is site 2; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion; a bonus with a weapon only
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					// Goldberry, River-daughter: [shire] ally who is no Hobbit, home site 2; heals herself as the
					// fellowship moves to a river, and has a skirmish ability used only on purpose
					put("goldberry", "9_51");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text
					put("rider", "12_161");     // Black Rider: fierce [wraith] Nazgul; strength +1 for each companion
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc minion, archer and nothing else
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BuckleberryFerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Buckleberry Ferry
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>River</b>. While only Hobbits are in the fellowship, there are no assignment and skirmish phases at Buckleberry Ferry.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Buckleberry Ferry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void WithOnlyHobbitsInTheFellowshipAtBuckleberryFerryTheGameGoesFromArcheryToRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var goldberry = scn.GetFreepsCard("goldberry");
		// The other player's Man, in his own fellowship: not in the one at the Ferry.
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var rider = scn.GetShadowCard("rider");
		// Hobbits of two cultures, the Ring-bearer among them; an ally who is no Hobbit, at her own home site,
		// is no member of the fellowship.
		scn.MoveCompanionsToTable(merry, pippin, shadowAragorn);
		scn.MoveAlliesToTable(goldberry);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A plain minion and a fierce one: there would be an assignment phase and a fierce one.
		scn.MoveMinionsToTable(troop, rider);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, rider);
		assertInZone(Zone.SUPPORT, goldberry);

		scn.ShadowPass();
		// The maneuver and archery phases are still there.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.PassManeuverActions();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.PassArcheryActions();

		// No assignment, no skirmish, no fierce round: the regroup phase, nobody hurt, the minions still there.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(goldberry));
		assertInZone(Zone.SHADOW_CHARACTERS, troop, rider);
	}

	@Test
	public void WithAragornInTheFellowshipAtBuckleberryFerryTheAssignmentPhaseComes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(troop);

		scn.ShadowPass();
		scn.PassManeuverActions();
		scn.PassArcheryActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The skirmish phase comes as well: the bar on skirmish phases is off too.
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSmeagolAShireCompanionWhoIsNoHobbitTheAssignmentPhaseComesAtBuckleberryFerry() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(troop);

		scn.ShadowPass();
		scn.PassManeuverActions();
		scn.PassArcheryActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The skirmish phase comes as well: the bar on skirmish phases is off too.
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(smeagol, troop);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhenTheOnlyCompanionWhoIsNoHobbitDiesToArcheryAtBuckleberryFerryThereIsNoAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(merry, aragorn);
		// Vitality 4 with 3 wounds: one archery wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(marksman);

		scn.SkipToArcheryWounds();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, aragorn));
		scn.FreepsChooseCard(aragorn);

		// The fellowship is now Hobbits alone, and the assignment phase does not come.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromBuckleberryFerryHobbitsAloneHaveTheirAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.FreepsPass();

		// At the Ferry: straight from archery to regroup.
		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(troop);
		scn.ShadowPass();
		scn.PassManeuverActions();
		scn.PassArcheryActions();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		// The regroup move takes the same Hobbits on to site 3, Rivendell Valley.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(rider);
		scn.ShadowPass();
		scn.PassManeuverActions();
		scn.PassArcheryActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
