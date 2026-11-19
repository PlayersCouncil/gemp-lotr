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

public class Card_01_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("support", "1_64");

					// Elf allies whose home is site 3 of the Fellowship path
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: strength 4, vitality 3; a skirmish ability against Orcs, never used
					put("erestor", "3_14");     // Erestor, Chief Advisor to Elrond: strength 5, vitality 2; answers [sauron] wounds only

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, no ally; her bonus is against a Nazgul only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally of home 3 who is no Elf; a fellowship ability, never used
					put("dinendal", "3_12");    // Dinendal, Silent Scout: an Elf ally of home 6; a regroup ability, never used
					// Rivendell Scout: an Elf ally of home 3 of the Hobbit path (3H), the only printed Elf ally of a site 3
					// outside the Fellowship block; his trigger answers a companion healed, and nobody is
					put("scout", "33_16");

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
	public void SupportoftheLastHomelyHouseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Support of the Last Homely House
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an Elf ally whose home is site 3. Until the regroup phase, that ally is strength +3 and participates in archery fire and skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("support");

		assertEquals("Support of the Last Homely House", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingAnElfAllyOfSite3MakesHimStrengthPlus3AndSkirmishUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support = scn.GetFreepsCard("support");
		var golradir = scn.GetFreepsCard("golradir");
		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		var grimir = scn.GetFreepsCard("grimir");
		var dinendal = scn.GetFreepsCard("dinendal");
		var scout = scn.GetFreepsCard("scout");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir, erestor, grimir, dinendal, scout);

		scn.StartGame();
		// The maneuver phase is played at site 4 and the second move goes to site 5: neither is any ally's home.
		scn.SkipToSite(3);
		scn.SetFreepsHand(support);
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(4, scn.GetStrength(golradir));
		assertEquals(5, scn.GetStrength(erestor));
		assertTrue(scn.FreepsPlayAvailable(support));

		scn.FreepsPlayCard(support);
		// Elf allies of site 3 only: not an Elf companion, an ally of site 3 who is no Elf, an Elf ally of site 6, or
		// an Elf ally of the Hobbit path's site 3.
		assertTrue(scn.FreepsHasCardChoiceAvailable(golradir, erestor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, grimir, dinendal, scout, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(golradir);

		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(7, scn.GetStrength(golradir));
		assertEquals(5, scn.GetStrength(erestor));
		assertInZone(Zone.DISCARD, support);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// Away from home, only the ally the event chose may be assigned.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(golradir));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(erestor));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(grimir));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{golradir, savage});

		// Arwen's skirmish first: strength 6 against 5, and the Runner dies.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// A skirmish that follows one already resolved: the bonus still holds.
		scn.FreepsResolveSkirmish(golradir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(golradir));
		assertEquals(5, scn.GetStrength(erestor));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		// Strength 7 against 5: Golradir wins.
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(1, scn.GetWoundsOn(savage));

		// The regroup phase: the bonus is over.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(golradir));

		// A second move in the same turn: the participation has ended with the strength.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(golradir));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(arwen));
	}

	@Test
	public void TheAllyChosenMayTakeArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support = scn.GetFreepsCard("support");
		var golradir = scn.GetFreepsCard("golradir");
		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir, erestor, grimir);
		scn.MoveCardsToHand(support);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2: no ally is at home.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(support);
		scn.FreepsChooseCard(golradir);
		assertEquals(1, scn.GetWoundsOn(golradir));

		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(golradir, arwen, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(erestor, grimir));
		scn.FreepsChooseCard(golradir);
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyElfAllyOfSite3IsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support = scn.GetFreepsCard("support");
		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		var grimir = scn.GetFreepsCard("grimir");
		var dinendal = scn.GetFreepsCard("dinendal");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor, grimir, dinendal);
		scn.MoveCardsToHand(support);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Vitality 2 with 1 wound: Erestor is exhausted.  Arwen, Grimir and Dinendal could exert.
		scn.AddWoundsToChar(erestor, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(support));

		// The control: the wound taken off, and he can pay.
		scn.RemoveWoundsFromChar(erestor, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(support));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
