package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tidings", "7_130");
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man; stronger and fierce at 2 burdens
					put("axeman2", "4_224");
					put("aragorn", "1_89");     // with Frodo, two companions: room for two threats
					put("gimli", "1_13");       // Gimli, Son of Gloin: a third companion, room for a third threat
					put("soldier", "4_254");    // Southron Soldier: a [raider] Southron Man; ambush (2) and nothing else
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul who is no Man; his texts read burdens only
					put("attea", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkTidingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Dark Tidings
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Regroup:</b> Spot 2 [raider] Men and remove (3) to add a threat (limit 1).
		*/

		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");

		assertEquals("Dark Tidings", tidings.getBlueprint().getTitle());
		assertNull(tidings.getBlueprint().getSubtitle());
		assertFalse(tidings.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, tidings.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, tidings.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, tidings.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(tidings, Keyword.SUPPORT_AREA));
		assertEquals(1, tidings.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotOfferedWithOneRaiderManBesideARaiderNazgulAndADunlandManThenRemovesThreeToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetShadowCard("tidings");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		var attea = scn.GetShadowCard("attea");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(tidings);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// The minions stand in play at the regroup phase, as minions that lived through the skirmishes do.
		scn.MoveMinionsToTable(axeman, attea, brigand);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// One [raider] Man: a [raider] card that is no Man and a Man of another culture do not make up the two.
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tidings));

		// A second [raider] Man, of another title.
		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(tidings));

		scn.ShadowUseCardAction(tidings);
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetShadowCard("tidings");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(tidings);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The phase before the regroup phase: two [raider] Men and (5) in the pool.
		assertInPlay(axeman, soldier);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tidings));
	}

	@Test
	public void IsNotOfferedWithTwoTwilightThenIsOfferedWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetShadowCard("tidings");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(tidings);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertInPlay(axeman, soldier);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tidings));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(tidings));

		scn.ShadowUseCardAction(tidings);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLimitRenewsInTheNextRegroupPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetShadowCard("tidings");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(tidings);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowUseCardAction(tidings);
		assertEquals(7, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		// At its limit in this regroup phase: the cost is paid and no threat is added.
		scn.FreepsPass();
		scn.ShadowUseCardAction(tidings);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		// A second move: the next regroup phase of the same turn, at site 3, with the same two Men.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInPlay(axeman, soldier);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(tidings));
		scn.ShadowUseCardAction(tidings);
		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityAtItsLimitIsStillOfferedAndPaidForAndAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("tidings");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("axeman"), scn.GetShadowCard("axeman2"));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals("false", scn.ShadowActionNoEffect(card));
		scn.ShadowUseCardAction(card);
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		// Limit 1: (3) can still be removed, so the ability is still offered, flagged as doing nothing
		// (Ketura on Q-0041).
		assertTrue(scn.ShadowActionAvailable(card));
		assertEquals("limit", scn.ShadowActionNoEffect(card));

		scn.ShadowUseCardAction(card);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
