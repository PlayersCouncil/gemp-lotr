package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.RTMDGameInfo;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: a table whose players are seated under account names of the test's choosing (the
 * constructor of VirtualTableScenario that ends with the two names; G-0039).  The names are the players' names to
 * the game, and everything the table keys by player goes by them: each player's cards, his meta-site, his seat's
 * channel, and the Freeps and Shadow methods.  It is for tests of something that reads a player's name
 * (mechanics/PlayerIsRequirementTests).
 */
public class NamedSeatTests
{
	protected VirtualTableScenario GetScenario(String freepsPlayerName, String shadowPlayerName) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: cost 4
					put("troop", "1_177");      // Goblin Patrol Troop: cost 6, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				null, null, null, null,
				freepsPlayerName, shadowPlayerName
		);
	}

	@Test
	public void EachPlayerIsSeatedUnderTheNameGivenAndTheTableActsForHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario("First Seat of the Test Table", "Second Seat of the Test Table");

		assertEquals("First Seat of the Test Table", scn.P1());
		assertEquals("Second Seat of the Test Table", scn.P2());

		// Each player's cards are his under his name.
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		assertEquals(scn.P1(), aragorn.getOwner());
		assertEquals(scn.P2(), troop.getOwner());
		assertEquals(scn.P2(), scn.GetShadowCard("aragorn").getOwner());
		assertEquals(scn.P1(), scn.GetFreepsSite(1).getOwner());
		assertEquals(scn.P2(), scn.GetShadowSite(1).getOwner());

		scn.MoveCardsToHand(aragorn, troop);
		scn.StartGame();

		// The game knows the players by those names, and each seat's channel is kept under its own.
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(scn.P1(), scn.GetRingBearer().getOwner());
		assertTrue(scn.PlayerSeesCardInHand(scn.P1(), aragorn));
		assertFalse(scn.PlayerSeesCardInHand(scn.P2(), aragorn));
		assertTrue(scn.ShadowSeesCardInHand(troop));
		assertFalse(scn.FreepsSeesCardInHand(troop));

		// The Freeps methods act for player 1 and the Shadow methods for player 2, by legal play and by an ad
		// hoc effect.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(4, scn.GetTwilight());

		scn.FreepsExecuteAdHocEffect(new WoundCharactersEffect(troop, aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 4 for Aragorn, 2 for the companions moving and 2 for Ettenmoors; the Troop costs 6, and 2 more for
		// roaming at site 2.
		assertEquals(8, scn.GetTwilight());
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(0, scn.GetTwilight());

		// And on into the second player's turn.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ANameLeftNullIsTheUsualName() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario(null, "Second Seat of the Test Table");
		assertEquals(VirtualTableScenario.P1, scn.P1());
		assertEquals("Second Seat of the Test Table", scn.P2());
		assertEquals(VirtualTableScenario.P1, scn.GetFreepsCard("aragorn").getOwner());

		scn = GetScenario("First Seat of the Test Table", null);
		assertEquals("First Seat of the Test Table", scn.P1());
		assertEquals(VirtualTableScenario.P2, scn.P2());
		assertEquals(VirtualTableScenario.P2, scn.GetShadowCard("aragorn").getOwner());

		scn = GetScenario(null, null);
		assertEquals(VirtualTableScenario.P1, scn.P1());
		assertEquals(VirtualTableScenario.P2, scn.P2());
	}

	@Test
	public void ANamedPlayerHasHisMetaSiteAndHisLeaguePlacementUnderHisName() throws DecisionResultInvalidException, CardNotFoundException {
		// Race Text 91_3 is player 2's: "Each time your fellowship moves, you may draw a card."  Nothing moves here.
		var scn = new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				null, "91_3", null,
				Map.of("Second Seat of the Test Table", new RTMDGameInfo.LeaguePlacement(3, 25)),
				null, "Second Seat of the Test Table"
		);

		var mod = scn.GetShadowCard("mod");
		assertEquals("91_3", mod.getBlueprintId());
		assertEquals("Second Seat of the Test Table", mod.getOwner());

		var league = (RTMDGameInfo) scn.game().getExtraInfo();
		assertEquals(new RTMDGameInfo.LeaguePlacement(3, 25), league.getLeaguePlacement(scn.P2()));
		assertNull(league.getLeaguePlacement(scn.P1()));
		// The usual name is nobody's at this table.
		assertNull(league.getLeaguePlacement(VirtualTableScenario.P2));
	}

	@Test
	public void TwoSeatsUnderOneNameAreRefused() {
		assertThrows(IllegalArgumentException.class,
				() -> GetScenario("First Seat of the Test Table", "First Seat of the Test Table"));
		// A name left null is the usual one, which the other player then cannot have.
		assertThrows(IllegalArgumentException.class, () -> GetScenario(null, VirtualTableScenario.P1));
		assertThrows(IllegalArgumentException.class, () -> GetScenario(VirtualTableScenario.P2, null));
	}
}
