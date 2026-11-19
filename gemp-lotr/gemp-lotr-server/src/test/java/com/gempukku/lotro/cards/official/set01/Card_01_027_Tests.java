package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_027_Tests
{

	// Thrarin's home is site 3 of the Fellowship sites; every test is fought away from it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thrarin", "1_27");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: another Dwarf ally, home 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, strength 8; his maneuver ability is not used
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion, strength 6, damage +1; his skirmish ability is not used
					put("marksman", "1_176");   // Goblin Marksman: an archer minion, strength 7, vitality 1; Archer is its whole text
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThrarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Thrarin, Dwarven Smith
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Dwarf
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Maneuver:</b> Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thrarin");

		assertEquals("Thrarin", card.getBlueprint().getTitle());
		assertEquals("Dwarven Smith", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void ManeuverAbilityExertsThrarinToLetHimTakeArcheryWoundsAndBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin, grimir);

		var marksman = scn.GetShadowCard("marksman");
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, runner1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.FreepsActionAvailable(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// The Marksman's archery wound may go on Thrarin; not on Grimir, another Dwarf ally away from home.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir));
		scn.FreepsChooseCard(aragorn);

		// Either player may assign him; Grimir neither.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(thrarin));
		assertTrue(scn.ShadowCanAssign(thrarin));
		assertFalse(scn.FreepsCanAssign(grimir));
		assertFalse(scn.ShadowCanAssign(grimir));

		scn.FreepsAssignToMinions(thrarin, runner1);
		// The Marksman is left over, and the Shadow player puts it on Thrarin as well.
		scn.ShadowAssignToMinions(thrarin, marksman);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, runner1));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, marksman));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedWhenThrarinIsExhaustedAndHeThenTakesNoPart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(thrarin, 1);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetVitality(thrarin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thrarin));

		// Away from home and not let take part, he is offered no archery wound and is not to be assigned.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin));
		scn.FreepsChooseCard(aragorn);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(thrarin));
		assertFalse(scn.ShadowCanAssign(thrarin));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void ThrarinTakesPartUntilTheRegroupPhaseAndNotAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var marksman = scn.GetShadowCard("marksman");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{gimli, runner2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The second skirmish of the turn, after one already resolved: he may still take part.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsCanAssign(thrarin));
		assertTrue(scn.ShadowCanAssign(thrarin));
		scn.PassSkirmishActions();

		// The regroup phase: no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(thrarin));
		assertFalse(scn.ShadowCanAssign(thrarin));

		// A second move, to site 5, and archery there: he is offered no wound.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(marksman);
		scn.ShadowRefreshDecision();
		scn.SkipToArcheryWounds();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin));
		scn.FreepsChooseCard(aragorn);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(thrarin));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}
}
