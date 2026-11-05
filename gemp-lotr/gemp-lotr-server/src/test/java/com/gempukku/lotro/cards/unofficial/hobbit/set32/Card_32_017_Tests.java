package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_32_017_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Every [esgaroth]
	// ally here is at home at site 6 (Esgaroth 30_54); the tests are fought at sites 2 and 3, away from it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("volunteers", "32_17");
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, archer; a Regroup ability, not used
					put("percy", "32_19");      // Percy, Bard's Lieutenant: [esgaroth] Man ally; lets [esgaroth] Men bearing a weapon take part (none bears one); a Response, not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, damage +1; no follower, his ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability, not used
					put("marksman", "1_176");   // Goblin Marksman: an archer minion; Archer is its whole text
					put("bats1", "32_30");      // Dark Bats: [moria] minion, strength 4, vitality 2; a Shadow ability, not used
					put("bats2", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void EsgarothVolunteersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Esgaroth Volunteers
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Esgaroth
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Man
		 * Home: 6H
		 * Strength: 6
		 * Vitality: 4
		 * Game Text: <b>Archer</b>.<br><b>Maneuver:</b> Spot Bard and exert Esgaroth Volunteers to allow Esgaroth Volunteers to participate in archery fire and skirmishes until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("volunteers");

		assertEquals("Esgaroth Volunteers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ESGAROTH, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 6)));
	}

	@Test
	public void ManeuverAbilityExertsVolunteersToLetThemTakeArcheryWoundsAndBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var volunteers = scn.GetFreepsCard("volunteers");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		var bats1 = scn.GetShadowCard("bats1");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(volunteers, bard);
		scn.MoveMinionsToTable(marksman, bats1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(volunteers));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(volunteers));
		scn.FreepsUseCardAction(volunteers);
		// Bard is spotted, not exerted.
		assertEquals(1, scn.GetWoundsOn(volunteers));
		assertEquals(0, scn.GetWoundsOn(bard));
		// The Volunteers are an archer: taking part in archery fire, they add 1 to the fellowship archery total
		// (Bard, also an archer, does not).
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// The Marksman's archery wound may go on the Volunteers; not on Bard, another [esgaroth] ally.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin, volunteers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bard));
		scn.FreepsChooseCard(thorin);
		// The Volunteers' archery wound, placed by the Shadow player: the Marksman (vitality 1) dies of it.
		scn.ShadowChooseCard(marksman);

		// Either player may assign them; Bard neither.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(volunteers));
		assertTrue(scn.ShadowCanAssign(volunteers));
		assertFalse(scn.FreepsCanAssign(bard));
		assertFalse(scn.ShadowCanAssign(bard));

		scn.FreepsAssignToMinions(volunteers, bats1);
		assertTrue(scn.IsCharAssignedAgainst(volunteers, bats1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedWithoutBardAndIsOnceBardIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var volunteers = scn.GetFreepsCard("volunteers");
		var bard = scn.GetFreepsCard("bard");
		var percy = scn.GetFreepsCard("percy");
		var bats1 = scn.GetShadowCard("bats1");

		scn.StartGame();
		// Percy, an [esgaroth] Man ally who is not Bard.
		scn.MoveAlliesToTable(volunteers, percy);
		scn.MoveMinionsToTable(bats1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(volunteers));

		scn.MoveAlliesToTable(bard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(volunteers));
		assertEquals(0, scn.GetWoundsOn(volunteers));
	}

	@Test
	public void IsNotOfferedWhenTheVolunteersAreExhaustedAndThenTakeNoPart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var volunteers = scn.GetFreepsCard("volunteers");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(volunteers, bard);
		// Vitality 4 with 3 wounds: they cannot exert.
		scn.AddWoundsToChar(volunteers, 3);
		scn.MoveMinionsToTable(marksman);
		assertEquals(3, scn.GetWoundsOn(volunteers));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetVitality(volunteers));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(volunteers));

		// Away from home and not let take part, they add nothing to the archery total, are offered no archery
		// wound and are not to be assigned.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(volunteers));
		scn.FreepsChooseCard(thorin);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(volunteers));
		assertFalse(scn.ShadowCanAssign(volunteers));
		assertEquals(3, scn.GetWoundsOn(volunteers));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void TheVolunteersTakePartUntilTheRegroupPhaseAndNotAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var volunteers = scn.GetFreepsCard("volunteers");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		var bats1 = scn.GetShadowCard("bats1");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(volunteers, bard);
		scn.MoveMinionsToTable(bats1, bats2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(volunteers));
		scn.FreepsUseCardAction(volunteers);
		assertEquals(1, scn.GetWoundsOn(volunteers));

		// The Volunteers' archery wound goes on the first Bats (vitality 2), who lives through it.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(bats1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thorin, bats1},
				new PhysicalCardImpl[]{balin, bats2});
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		// The second skirmish of the turn, after one already resolved: they may still take part.
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsCanAssign(volunteers));
		assertTrue(scn.ShadowCanAssign(volunteers));
		scn.PassSkirmishActions();

		// The regroup phase: no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(volunteers));
		assertFalse(scn.ShadowCanAssign(volunteers));

		// A second move, to site 3, and archery there: they are offered no wound.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(marksman);
		scn.ShadowRefreshDecision();
		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(volunteers));
		scn.FreepsChooseCard(thorin);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(volunteers));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}
}
