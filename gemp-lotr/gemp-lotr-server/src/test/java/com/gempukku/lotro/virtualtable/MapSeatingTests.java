package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: a table in a format that uses Maps (PC Movie).  Such a format cannot seat a deck that has
 * no Map, so the rig gives each deck the Map of the site path its sites are on unless the test names the Maps, and
 * hands the Maps to the test with GetFreepsMap() and GetShadowMap().
 */
public class MapSeatingTests
{
	protected VirtualTableScenario GetScenario(HashMap<String, String> sites, String format) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
				}},
				sites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	@Test
	public void WithNoMapNamedEachDeckHasTheMapOfItsSitePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario(VirtualTableScenario.FellowshipSites, VirtualTableScenario.PCMovie);
		assertEquals(VirtualTableScenario.FellowshipMap, scn.GetFreepsMap().getBlueprintId());
		assertEquals(VirtualTableScenario.FellowshipMap, scn.GetShadowMap().getBlueprintId());
		// Two cards, one for each player.
		assertNotSame(scn.GetFreepsMap(), scn.GetShadowMap());
		assertEquals(scn.P1(), scn.GetFreepsMap().getOwner());
		assertEquals(scn.P2(), scn.GetShadowMap().getOwner());
		assertInZone(Zone.SUPPORT, scn.GetFreepsMap(), scn.GetShadowMap());

		scn = GetScenario(VirtualTableScenario.TowersSites, VirtualTableScenario.PCMovie);
		assertEquals(VirtualTableScenario.TowersMap, scn.GetFreepsMap().getBlueprintId());
		assertEquals(VirtualTableScenario.TowersMap, scn.GetShadowMap().getBlueprintId());

		scn = GetScenario(VirtualTableScenario.KingSites, VirtualTableScenario.PCMovie);
		assertEquals(VirtualTableScenario.KingMap, scn.GetFreepsMap().getBlueprintId());
		assertEquals(VirtualTableScenario.KingMap, scn.GetShadowMap().getBlueprintId());

		// The table plays on as any other does.
		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, scn.GetFreepsMap(), scn.GetShadowMap());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFormatThatDoesNotUseMapsHasNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario(VirtualTableScenario.FellowshipSites, VirtualTableScenario.Multipath);
		assertNull(scn.GetFreepsMap());
		assertNull(scn.GetShadowMap());
	}

	@Test
	public void SitesOfAPathWithNoMapNeedTheMapsNamed() throws DecisionResultInvalidException, CardNotFoundException {
		// There is no Map for the Shadows path.
		assertThrows(IllegalArgumentException.class, () -> GetScenario(VirtualTableScenario.ShadowsSites, VirtualTableScenario.PCMovie));

		var scn = new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie,
				VirtualTableScenario.KingMap,
				VirtualTableScenario.TowersMap
		);
		assertEquals(VirtualTableScenario.KingMap, scn.GetFreepsMap().getBlueprintId());
		assertEquals(VirtualTableScenario.TowersMap, scn.GetShadowMap().getBlueprintId());
		assertInZone(Zone.SUPPORT, scn.GetFreepsMap(), scn.GetShadowMap());
	}
}
