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
 * Tests of the rig itself: MoveCardToAdventurePath, which lays a site as the one after the fellowship's.  The site
 * is given its number before it reaches the path, as a site played is, so a site whose text reads its own number
 * as it comes into force is laid like any other (F-0484: Mount Doom 15_193 threw a NullPointerException when the
 * number came after).
 */
public class AdventurePathLayingTests
{
	// A Shadows-block path: each site after the first is laid by the Shadow player as the fellowship moves.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Úlairë Nelya, Third of the Nine Riders: "Shadow: Exert Úlairë Nelya to replace the fellowship's
					// current site with a site from your adventure deck."
					put("nelya", "11_222");
				}},
				new HashMap<>() {{
					// Mount Doom: "Until the end of the game, sites in this region cannot be replaced."
					put("mountdoom", "15_193");
					put("eastroad", "11_236");      // East Road: each companion is twilight cost +2
					put("ruins", "18_138");         // Sirannon Ruins: River, no text
					put("nurn", "17_148");          // Nurn: its text is about controlling it
					put("glade", "11_239");         // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void MountDoomIsLaidAheadOfTheFellowshipWithItsNumberAndItsTextInForce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var eastRoad = scn.GetFreepsSite("eastroad");
		var ruins = scn.GetShadowSite("ruins");
		var nurn = scn.GetShadowSite("nurn");
		var mountDoom = scn.GetFreepsSite("mountdoom");

		scn.StartGame(eastRoad);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(ruins, scn.GetCurrentSite());

		// Laid by the cheat in the Shadow phase at site 2: it is site 3, in region 1 with the fellowship's site.
		scn.MoveCardToAdventurePath(mountDoom);
		assertInZone(Zone.ADVENTURE_PATH, mountDoom);
		assertSame(mountDoom, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Its bar is in force as it lies there, with nobody moved to it (Legacy Ruling 3): Nelya exerts, and the
		// fellowship's site 2 is not replaced, nor is the Shadow player asked for a site.
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(ruins, scn.GetCurrentSite());
		assertSame(ruins, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, nurn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
