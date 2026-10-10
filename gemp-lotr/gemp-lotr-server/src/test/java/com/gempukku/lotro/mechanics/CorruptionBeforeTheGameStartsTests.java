package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A Ring-bearer corrupted by her own player's bid, before the game starts.
 *
 * Comprehensive Rules 4.2, "bid": "If you bid a number of burdens equal to or higher than your Ring-bearer's
 * resistance, your Ring-bearer becomes corrupted before the game starts and you lose the game."
 *
 * The resistance the bid is held against is the resistance in force, with The One Ring's bonus and the
 * Ring-bearer's own text counted.  Ketura, 2026-10-09: "the game text on the ring and Ring-bearer are active
 * enough for resistance calculations during the bidding process, otherwise Galadriel would never get to bid much
 * at all."
 *
 * Galadriel, Bearer of Wisdom is the Ring-bearer whose printed resistance, 3, is below a bid a player might
 * make: "While Galadriel bears an artifact or The One Ring, she is resistance +1 for each [elven] companion you
 * can spot", and she is one herself.  With The One Ring, The Ring of Rings (resistance +2) she stands at 6 before
 * a burden is placed.  The game once corrupted her at the start on the printed number.
 *
 * Each table here makes its own bids, so no test removes the burdens bid, and the game is left where the bids
 * leave it.  Who has lost is read from the game (FreepsHasLost, ShadowHasLost); why, from the game log's line.
 */
public class CorruptionBeforeTheGameStartsTests
{
	// Player 1 bids 5 and player 2 nothing.
	protected VirtualTableScenario GetBidOfFiveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Son of Glóin: a companion in the draw deck who is no Elf, so that the game has a
					// starting fellowship to ask about
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				"11_1",     // The One Ring, The Ring of Rings: resistance +2
				null, null,
				scn -> {
					scn.MarkGameLog();
					scn.FreepsChoose("5");
					scn.ShadowChoose("0");
					scn.FreepsChoose("Go first");
				}
		);
	}

	// Player 1 bids 6, the whole of her resistance, and player 2 nothing.
	protected VirtualTableScenario GetBidOfSixScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: as above
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				"11_1",     // The One Ring, The Ring of Rings: resistance +2
				null, null,
				scn -> {
					scn.MarkGameLog();
					scn.FreepsChoose("6");
					scn.ShadowChoose("0");
					scn.FreepsChoose("Go first");
				}
		);
	}

	// Player 1 bids 5 and player 2 bids 6: player 2 has the higher bid and the choice of seat.
	protected VirtualTableScenario GetSecondPlayerBidsSixScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: as above
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				"11_1",     // The One Ring, The Ring of Rings: resistance +2
				null, null,
				scn -> {
					scn.MarkGameLog();
					scn.FreepsChoose("5");
					scn.ShadowChoose("6");
					scn.ShadowChoose("Go second");
				}
		);
	}

	@Test
	public void ARingBearerWhoseTextAndRingLiftHerResistanceAboveHerBidIsNotCorruptedBeforeTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBidOfFiveScenario();

		var galadriel = scn.GetRingBearer();

		// The bid has become her burdens, and the game has gone on to her player's starting fellowship
		assertFalse(scn.GameIsFinished());
		assertInZone(Zone.FREE_CHARACTERS, galadriel);
		assertAttachedTo(scn.GetRing(), galadriel);
		assertEquals(5, scn.GetBurdens());
		// 3 printed + 2 for The Ring of Rings + 1 for herself, an [elven] companion, while she bears The One
		// Ring: 6, less the 5 burdens.  On the printed 3, or with either bonus left out, 5 burdens corrupt her.
		assertEquals(1, scn.GetResistance(galadriel));
		assertFalse(scn.FreepsHasLost());
		assertFalse(scn.ShadowHasLost());
		assertFalse(scn.GameLogSinceMarkSays("lost"));
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void ABidEqualToTheRingBearersResistanceInForceCorruptsHerAndHerPlayerLosesBeforeTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBidOfSixScenario();

		// "Equal to or higher": 6 burdens against 3 + 2 + 1.  The game is over before a companion is played.
		assertTrue(scn.GameIsFinished());
		assertEquals(0, scn.GetResistance(scn.GetRingBearer()));
		assertTrue(scn.FreepsHasLost());
		assertFalse(scn.ShadowHasLost());
		assertTrue(scn.ShadowHasWon());
		// The game's own words for why
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: Corrupted before game started"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void EachPlayersBidIsHeldAgainstHisOwnRingBearerAndTheOtherPlayersHigherBidDoesNotCorruptHer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSecondPlayerBidsSixScenario();

		// Both Ring-bearers are Galadriel at 6.  Player 2's 6 burdens corrupt his; player 1's 5 leave hers at 1,
		// though the higher bid at the table was 6.
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.ShadowHasLost());
		assertFalse(scn.FreepsHasLost());
		assertTrue(scn.FreepsHasWon());
		assertEquals(5, scn.FreepsGetBurdens());
		assertEquals(6, scn.ShadowGetBurdens());
		assertTrue(scn.GameLogSinceMarkSays(scn.P2() + " lost due to: Corrupted before game started"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P1() + " lost"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}
}
