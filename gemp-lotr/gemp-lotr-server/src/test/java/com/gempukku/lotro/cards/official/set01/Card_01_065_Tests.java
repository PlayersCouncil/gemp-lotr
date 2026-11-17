package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swanship", "1_65");

					// Elf allies whose home is site 6 of the Fellowship path
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: strength 6, no archer; a fellowship ability, never used
					put("dinendal", "3_12");    // Dinendal, Silent Scout: strength 3, archer; a regroup ability, never used

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, no ally; her bonus is against a Nazgul only
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: an Elf ally of home 3; a skirmish ability, never used
					// Aldor, Soldier of Edoras: a [rohan] Man ally of homes 3 and 6 of the Tower path; his archery ability
					// wants a villager, and there is none
					put("aldor", "4_262");

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and no other text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so it adds nothing
					put("marksman", "1_176");   // Goblin Marksman: archer, strength 7, vitality 1, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwanshipoftheGaladhrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Swan-ship of the Galadhrim
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an Elf ally whose home is site 6. Until the regroup phase, that ally is strength +3 and participates in archery fire and skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swanship");

		assertEquals("Swan-ship of the Galadhrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingAnElfAllyOfSite6MakesHimStrengthPlus3AndSkirmishUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swanship = scn.GetFreepsCard("swanship");
		var celeborn = scn.GetFreepsCard("celeborn");
		var dinendal = scn.GetFreepsCard("dinendal");
		var arwen = scn.GetFreepsCard("arwen");
		var golradir = scn.GetFreepsCard("golradir");
		var aldor = scn.GetFreepsCard("aldor");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(celeborn, dinendal, golradir, aldor);
		scn.MoveCardsToHand(swanship);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2, and the second move goes to site 3: neither is home to an ally of site 6.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(dinendal));
		assertEquals(6, scn.GetStrength(celeborn));
		assertEquals(3, scn.GetStrength(dinendal));
		assertTrue(scn.FreepsPlayAvailable(swanship));

		scn.FreepsPlayCard(swanship);
		// Elf allies of site 6 only: not an Elf companion, an Elf ally of site 3, or a Man ally of the Tower path's site 6.
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, dinendal));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, golradir, aldor, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(celeborn);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(dinendal));
		assertEquals(9, scn.GetStrength(celeborn));
		assertEquals(3, scn.GetStrength(dinendal));
		assertInZone(Zone.DISCARD, swanship);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// Away from home, only the ally the event chose may be assigned.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(celeborn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(dinendal));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(golradir));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{celeborn, savage});

		// Arwen's skirmish first: strength 6 against 5, and the Runner dies.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// A skirmish that follows one already resolved: the bonus still holds.
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(celeborn));
		assertEquals(3, scn.GetStrength(dinendal));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		// Strength 9 against 5: Celeborn wins.
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(savage));

		// The regroup phase: the bonus is over.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(celeborn));

		// A second move in the same turn: the participation has ended with the strength.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(celeborn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(arwen));
	}

	@Test
	public void TheArcherAllyChosenAddsToTheFellowshipArcheryTotalAndMayTakeArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swanship = scn.GetFreepsCard("swanship");
		var celeborn = scn.GetFreepsCard("celeborn");
		var dinendal = scn.GetFreepsCard("dinendal");
		var arwen = scn.GetFreepsCard("arwen");
		var golradir = scn.GetFreepsCard("golradir");
		var frodo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(celeborn, dinendal, golradir);
		scn.MoveCardsToHand(swanship);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		// No companion is an archer, and Dinendal away from home does not count.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(swanship);
		scn.FreepsChooseCard(dinendal);
		assertEquals(1, scn.GetWoundsOn(dinendal));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(dinendal, arwen, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, golradir));
		scn.FreepsChooseCard(dinendal);
		assertEquals(2, scn.GetWoundsOn(dinendal));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		// Dinendal's arrow killed the Marksman, so there is nothing to assign and the game goes to regroup.
		assertInZone(Zone.DISCARD, marksman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyElfAllyOfSite6IsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swanship = scn.GetFreepsCard("swanship");
		var dinendal = scn.GetFreepsCard("dinendal");
		var arwen = scn.GetFreepsCard("arwen");
		var golradir = scn.GetFreepsCard("golradir");
		var aldor = scn.GetFreepsCard("aldor");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(dinendal, golradir, aldor);
		scn.MoveCardsToHand(swanship);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Vitality 3 with 2 wounds: Dinendal is exhausted.  Arwen, Golradir and Aldor could exert.
		scn.AddWoundsToChar(dinendal, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(dinendal));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swanship));

		// The control: the wounds taken off, and he can pay.
		scn.RemoveWoundsFromChar(dinendal, 2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(swanship));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
