package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_31_008_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Elf Army is at
	// home at site 5 (Mirkwood 30_53); the tests are fought at sites 2 and 3, away from it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "31_8");
					put("legolas", "31_9");     // Legolas, Prince of Mirkwood: [elven] Elf ally, archer, home 5H; an Archery ability, not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, damage +1; no follower, his ability not used
					put("malice", "32_51");     // Dragon's Malice: [smaug] condition, support area: a Shadow card that is no minion; its texts need Smaug
					put("marksman1", "1_176");  // Goblin Marksman: an archer minion; Archer is its whole text
					put("marksman2", "1_176");
					put("bats1", "32_30");      // Dark Bats: [moria] minion, strength 4, vitality 2; a Shadow ability, not used
					put("bats2", "32_30");
					put("bats3", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ElfArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Elf Army
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 5H
		 * Strength: 7
		 * Vitality: 4
		 * Game Text: <b>Archer</b>.<br><b>Maneuver:</b> If you can spot more minions than companions, exert Elf Army to allow it to participate in archery fire and skirmishes until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Elf Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 5)));
	}

	@Test
	public void IsNotOfferedWithAsManyMinionsAsCompanionsAndIsWithOneMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var legolas = scn.GetFreepsCard("legolas");
		var thorin = scn.GetFreepsCard("thorin");
		var malice = scn.GetShadowCard("malice");
		var bats1 = scn.GetShadowCard("bats1");
		var bats2 = scn.GetShadowCard("bats2");
		var bats3 = scn.GetShadowCard("bats3");

		scn.StartGame();
		// Two companions (Bilbo and Thorin) and two allies, the Elf Army among them.
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(army, legolas);
		// Two minions, and a Shadow card that is no minion.
		scn.MoveCardsToSupportArea(malice);
		scn.MoveMinionsToTable(bats1, bats2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Two minions against two companions: not more.  Counting the allies as well, or the condition, would
		// tip the count the other way.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(army));

		// A third minion: three against two.
		scn.MoveMinionsToTable(bats3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(army));
		assertEquals(0, scn.GetWoundsOn(army));
	}

	@Test
	public void ManeuverAbilityExertsElfArmyToLetItTakeArcheryWoundsAndBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var legolas = scn.GetFreepsCard("legolas");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman1");
		var bats1 = scn.GetShadowCard("bats1");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(army, legolas);
		scn.MoveMinionsToTable(marksman, bats1, bats2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(army));
		scn.FreepsUseCardAction(army);
		assertEquals(1, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(legolas));
		// The Elf Army is an archer: taking part in archery fire, it adds 1 to the fellowship archery total
		// (Legolas, also an archer ally, does not).
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// The Marksman's archery wound may go on the Elf Army; not on Legolas, another [elven] ally.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin, army));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas));
		scn.FreepsChooseCard(thorin);
		// The Elf Army's archery wound, placed by the Shadow player: the Marksman (vitality 1) dies of it.
		scn.ShadowChooseCard(marksman);

		// Either player may assign it; Legolas neither.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(army));
		assertTrue(scn.ShadowCanAssign(army));
		assertFalse(scn.FreepsCanAssign(legolas));
		assertFalse(scn.ShadowCanAssign(legolas));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{army, bats1},
				new PhysicalCardImpl[]{thorin, bats2});
		assertTrue(scn.IsCharAssignedAgainst(army, bats1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedWhenTheElfArmyIsExhaustedAndThenTakesNoPart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman1");
		var bats1 = scn.GetShadowCard("bats1");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(army);
		// Vitality 4 with 3 wounds: it cannot exert.
		scn.AddWoundsToChar(army, 3);
		// Three minions against two companions: the requirement is met.
		scn.MoveMinionsToTable(marksman, bats1, bats2);
		assertEquals(3, scn.GetWoundsOn(army));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetVitality(army));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(army));

		// Away from home and not let take part, it adds nothing to the archery total, is offered no archery
		// wound and is not to be assigned.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(army));
		scn.FreepsChooseCard(thorin);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(army));
		assertFalse(scn.ShadowCanAssign(army));
		assertEquals(3, scn.GetWoundsOn(army));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void TheElfArmyTakesPartUntilTheRegroupPhaseAndNotAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var bats1 = scn.GetShadowCard("bats1");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(army);
		scn.MoveMinionsToTable(marksman1, bats1, bats2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(army));
		scn.FreepsUseCardAction(army);
		assertEquals(1, scn.GetWoundsOn(army));

		// The Marksman's wound goes on Thorin; the Elf Army's, placed by the Shadow player, kills the Marksman.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(thorin);
		scn.ShadowChooseCard(marksman1);
		scn.SkipToAssignments();
		// Bilbo (strength 3) loses to a Dark Bats (strength 4) and takes a wound; no more.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thorin, bats1},
				new PhysicalCardImpl[]{bilbo, bats2});
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		// The second skirmish of the turn, after one already resolved: it may still take part.
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsCanAssign(army));
		assertTrue(scn.ShadowCanAssign(army));
		scn.PassSkirmishActions();

		// The regroup phase: no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(army));
		assertFalse(scn.ShadowCanAssign(army));

		// A second move, to site 3, and archery there: it is offered no wound.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(marksman2);
		scn.ShadowRefreshDecision();
		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(army));
		scn.FreepsChooseCard(thorin);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(army));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}
}
