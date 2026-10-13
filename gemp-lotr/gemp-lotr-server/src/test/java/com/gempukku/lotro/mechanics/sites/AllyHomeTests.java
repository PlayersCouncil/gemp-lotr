package com.gempukku.lotro.mechanics.sites;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * An ally in the skirmishes at his home site, and not elsewhere.
 *
 * Comprehensive Rules 4.2, "ally": "Ally cards have a home site number indicated just after the card's type ...
 * These home site numbers all refer to adventure paths from the Fellowship block, Tower block, and King block.  ...
 * Block symbol is a factor in determining an ally's home site.  An ally with a home site of 2 [Towers] has a home
 * site only on the Tower block adventure path.  ...  Allies normally do not participate in archery fire and
 * skirmishes."  "Assignment phase": "When the fellowship is at an ally's home site (or if a card effect has allowed
 * an ally to participate in skirmishes), that ally may be assigned to a skirmish in the same way that companions are
 * assigned to skirmishes."  "Fierce": "The Free Peoples player then assigns defenders just as during the regular
 * assignment phase".
 *
 * This class is what lets a card's own tests take an ally's printed home as read (Ketura on Q-0279, 2026-10-12).
 *
 * What is in another class and is not gone over again here:
 *   the archery half (an ally at home in the archery total and taking archery wounds): AlliesTakeArcheryWoundsTests;
 *   a card that bars an ally from taking part, or has him take part away from home: AllyBarredFromTakingPartTests;
 *   an unhasty ally, who may be assigned at home and only there: UnhastyTests;
 *   an ally at home fighting at his own strength: SkirmishStrengthTests;
 *   a minion whose own text assigns it to an ally: MinionThatAssignsItselfToAnAllyTests.
 *
 * Erland, Advisor to Brand 2_21 is an ally whose home is site 3 of the Fellowship path; Aldor, Soldier of Edoras
 * 4_262 one whose homes are sites 3 and 6 of the Towers path.  Site 3 of the Fellowship table is Rivendell Valley
 * 1_341; of the Towers table, Barrows of Edoras 4_337.  Neither site's text is used.
 */
public class AllyHomeTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FellowshipSites);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.TowersSites);
	}

	protected VirtualTableScenario GetScenario(HashMap<String, String> sites) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Erland, Advisor to Brand: ally, home 3 of the Fellowship path, strength 5, vitality 2.  "Shadow
					// players may not look at or reveal cards in your hand."
					put("erland", "2_21");
					// Aldor, Soldier of Edoras: ally, homes 3 and 6 of the Towers path, strength 3, vitality 2.  His
					// archery ability needs a villager, and there is none.
					put("aldor", "4_262");
					put("knight", "5_35");      // Gondorian Knight: companion, strength 6, vitality 3; "Knight." is his whole text

					// Gríma, Wormtongue: minion, strength 4, vitality 3.  His maneuver ability is never used.
					put("grima", "4_154");
					put("troop", "1_177");      // Goblin Patrol Troop: minion, strength 13, vitality 3, no text at all
					// Uruk Pursuer: fierce minion, strength 5, vitality 3.  "Tracker.  Fierce.  Unbound Hobbits may not
					// be discarded."  Its response answers an unbound Hobbit killed.
					put("pursuer", "4_190");
				}},
				sites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AtHisHomeSiteAnAllyMayBeAssignedByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var erland = scn.GetFreepsCard("erland");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(erland);

		var grima = scn.GetShadowCard("grima");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(grima, troop);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// He is on the Free Peoples player's list beside the companions.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(3, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(erland));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(knight));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));

		scn.FreepsAssignToMinions(erland, grima);
		assertTrue(scn.IsCharAssignedAgainst(erland, grima));

		// And on the Shadow player's, for the minion left over.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(erland));
		scn.ShadowAssignToMinions(erland, troop);
		assertTrue(scn.IsCharAssignedAgainst(erland, troop));

		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AwayFromHisHomeSiteAnAllyIsAssignedByNeitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var erland = scn.GetFreepsCard("erland");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(erland);

		var grima = scn.GetShadowCard("grima");

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(2, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(knight));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(erland));

		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(knight));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(erland));
	}

	@Test
	public void ASiteOfTheSameNumberOnAnotherBlocksPathIsNotAnAllysHome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var aldor = scn.GetFreepsCard("aldor");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(erland, aldor);

		var grima = scn.GetShadowCard("grima");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(grima);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// Site 3 of the Fellowship path: Erland's home; Aldor's is site 3 of the Towers path.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(erland));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aldor));

		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(erland));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(aldor));
	}

	@Test
	public void OnTheOtherBlocksPathItIsTheOtherWayRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var erland = scn.GetFreepsCard("erland");
		var aldor = scn.GetFreepsCard("aldor");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(erland, aldor);

		var grima = scn.GetShadowCard("grima");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(grima);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aldor));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(erland));

		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aldor));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(erland));
	}

	@Test
	public void AnAllyWhoMayBeAssignedInTheFirstRoundMayBeAssignedInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var erland = scn.GetFreepsCard("erland");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(erland);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// The first round: the Knight, 6, beats the Pursuer, 5, which lives.
		scn.FreepsAssignToMinions(knight, pursuer);
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(3, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(erland));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(knight));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));

		scn.FreepsAssignToMinions(erland, pursuer);
		scn.FreepsResolveSkirmish(erland);
		scn.PassFierceSkirmishActions();

		// 5 against 5: the tie is the Shadow side's.
		assertEquals(1, scn.GetWoundsOn(erland));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
