package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("depths", "3_77");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text
					put("wallcrawler", "1_184");  // Goblin Wallcrawler: [moria] Orc, cost 2; standing texts underground and on the archery total
					put("troll", "1_165");        // Cave Troll of Moria: [moria] Troll, damage +1 and fierce; its other texts are about playing it: a [moria] minion that is no Orc
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rivendell Waterfall as site 3, with Aragorn, a ranger: the move limit is 3 there, so a turn has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("depths", "3_77");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a ranger; a maneuver ability only
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_342");   // Rivendell Waterfall
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DepthsofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Depths of Moria
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [moria] Orc. Plays to your support area.<br>Each time the fellowship moves during the regroup phase, add (2).
		*/

		var scn = GetScenario();

		var depths = scn.GetFreepsCard("depths");

		assertEquals("Depths of Moria", depths.getBlueprint().getTitle());
		assertNull(depths.getBlueprint().getSubtitle());
		assertFalse(depths.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, depths.getBlueprint().getSide());
		assertEquals(Culture.MORIA, depths.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, depths.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(depths, Keyword.SUPPORT_AREA));
		assertEquals(1, depths.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMoriaOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depths = scn.GetShadowCard("depths");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(depths, wallcrawler);
		assertInHand(depths, wallcrawler);

		scn.StartGame();
		// A [moria] minion who is no Orc (the Cave Troll, cheated in: the Orc he was played beside is gone) and an
		// Orc who is not [moria].
		scn.MoveMinionsToTable(troll, soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(depths));

		scn.ShadowPlayCard(wallcrawler);
		assertInZone(Zone.SHADOW_CHARACTERS, wallcrawler);
		assertTrue(scn.ShadowPlayAvailable(depths));

		scn.ShadowPlayCard(depths);
		assertInZone(Zone.SUPPORT, depths);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAGoblinPatrolTroop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depths = scn.GetShadowCard("depths");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(depths);
		assertInHand(depths);

		scn.StartGame();
		// A [moria] Orc of another title than the Wallcrawler.
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(depths));
		scn.ShadowPlayCard(depths);
		assertInZone(Zone.SUPPORT, depths);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARegroupMoveAddsTwoAndTheFellowshipPhaseMoveDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depths = scn.GetShadowCard("depths");

		scn.StartGame();
		scn.MoveCardsToSupportArea(depths);
		assertInZone(Zone.SUPPORT, depths);
		assertEquals(0, scn.GetTwilight());

		// The move at the end of the fellowship phase is no regroup move:
		// 2 for Ettenmoors' Shadow number and 1 for Frodo, and nothing more.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilight());

		// No minion: the Shadow phase is followed by the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// 0 for Rivendell Valley's Shadow number, 1 for Frodo, and 2 for Depths of Moria.
		assertEquals(pool + 0 + 1 + 2, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoRegroupMovesInOneTurnAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var depths = scn.GetShadowCard("depths");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(depths);
		assertInZone(Zone.SUPPORT, depths);

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		int pool = scn.GetTwilight();

		// The first regroup move, to Rivendell Waterfall: 0 for its Shadow number, 2 for Frodo and Aragorn, and 2
		// for Depths of Moria.
		scn.FreepsChooseToMove();
		assertEquals(pool + 0 + 2 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		pool = scn.GetTwilight();

		// The second regroup move of the same turn, to Balin's Tomb: 3 for its Shadow number, 2 for the
		// companions, and 2 for Depths of Moria again.
		scn.FreepsChooseToMove();
		assertEquals(pool + 3 + 2 + 2, scn.GetTwilight());
		assertEquals(3, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
