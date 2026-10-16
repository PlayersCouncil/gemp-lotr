package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_336_Tests
{

	// The Towers path, Wold of Rohan its site 2, reached by the first move of the game.  Site 1 is Eastemnet
	// Gullies 4_325, whose fellowship ability is used only on purpose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion; an ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion; an ability used only on purpose
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: defender +1 and nothing else
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who may skirmish by his own maneuver ability
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("uruk1", "12_135");     // Barbaric Uruk: "Assignment: Make the Free Peoples player assign this minion to an unwounded companion."
					put("uruk2", "12_135");
				}},
				new HashMap<>() {{
					put("site1", "4_325");
					put("site2", "4_336");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WoldofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wold of Rohan
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Plains</b>. Each time the Free Peoples player assigns a companion or ally to a skirmish, add (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Wold of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void TwoCompanionsTheFreePeoplesPlayerAssignsAddTwoAndTheShadowPlayersAssignmentAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wold = scn.GetShadowSite(2);
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();
		scn.FreepsPass();
		assertSame(wold, scn.GetCurrentSite());
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToAssignments();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{boromir, troop2});
		// One for each companion assigned.
		assertEquals(2, scn.GetTwilight());

		// The leftover minion is the Shadow player's to assign.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(frodo, troop3);
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ADefenderTheFreePeoplesPlayerAssignsToTwoMinionsAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wold = scn.GetShadowSite(2);
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		assertSame(wold, scn.GetCurrentSite());
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, troop1, troop2);
		// One companion, assigned to one skirmish: one, not one for each minion.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllyTheFreePeoplesPlayerAssignsAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wold = scn.GetShadowSite(2);
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.FreepsPass();
		assertSame(wold, scn.GetCurrentSite());
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAssignToMinions(thrarin, troop1);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheFreePeoplesPlayerMadeToAssignByAShadowCardAddsOneWhereHeChoosesTheCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wold = scn.GetShadowSite(2);
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var uruk = scn.GetShadowCard("uruk1");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();
		scn.FreepsPass();
		assertSame(wold, scn.GetCurrentSite());
		scn.MoveMinionsToTable(uruk);
		// Frodo wounded: Gimli and Boromir are the unwounded companions he chooses between.
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsPass();
		scn.ShadowUseCardAction(uruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		// He has a choice in it, so the assignment is his (Q-0171).
		scn.FreepsChooseCard(gimli);

		assertTrue(scn.IsCharAssigned(gimli));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMadeToAssignByAShadowCardWithNoChoiceAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wold = scn.GetShadowSite(2);
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var uruk = scn.GetShadowCard("uruk1");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();
		scn.FreepsPass();
		assertSame(wold, scn.GetCurrentSite());
		scn.MoveMinionsToTable(uruk);
		// Frodo and Boromir wounded: Gimli is the one unwounded companion.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPass();
		scn.ShadowUseCardAction(uruk);

		assertTrue(scn.IsCharAssigned(gimli));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
