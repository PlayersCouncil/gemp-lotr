package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: a table whose sites give the game no site 1 to lay is refused when it is built, in
 * words.  The game lays each site by its printed site number unless the first card of the adventure deck has none
 * (GameState.usesUnorderedSites), so a Shadows-block site, which has no printed number, cannot take the place of
 * site 1 on a numbered path: the game's set-up lays nothing there, and used to fall over two steps later with a
 * NullPointerException before any line of the test ran.  The same site starts a path that is unordered throughout.
 *
 * Also the order the table gives a site map's sites in each adventure deck (those with a printed number first, so
 * that the key a Shadows-block site stands under cannot turn a numbered path into an unordered one), and the
 * names it gives two sites of one blueprint (each its own key).
 */
public class FirstSiteTests
{
	// East Road, a Shadows-block site: "Forest. Each companion is twilight cost +2."
	private static final String EastRoad = "11_236";

	protected VirtualTableScenario GetScenario(HashMap<String, String> sites, String format) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: cost 4
				}},
				sites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	// Fails unless building the table is refused, saying each of the given things.
	private void assertRefused(HashMap<String, String> sites, String format, String... said) throws CardNotFoundException, DecisionResultInvalidException {
		String refusal = null;
		try {
			GetScenario(sites, format);
		}
		catch (IllegalArgumentException refused) {
			refusal = String.valueOf(refused.getMessage());
		}
		assertNotNull("The table was built.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	@Test
	public void ASiteWithNoPrintedNumberInPlaceOfSiteOneOfANumberedPathIsRefusedSayingWhy() throws DecisionResultInvalidException, CardNotFoundException {
		var sites = new HashMap<String, String>() {{
			putAll(VirtualTableScenario.FellowshipSites);
			put("site1", EastRoad);
		}};

		// Whatever the format: the game goes by the adventure deck.
		for (String format : new String[] { VirtualTableScenario.Multipath, VirtualTableScenario.Fellowship, VirtualTableScenario.Shadows }) {
			assertRefused(sites, format,
					"the game would lay no site 1",
					"none of these sites is printed as site 1",
					"The key \"site1\" holds East Road (11_236), which has no printed site number");
		}
	}

	@Test
	public void ANumberedPathWithNoSiteOneAtAllIsRefusedToo() throws DecisionResultInvalidException, CardNotFoundException {
		assertRefused(new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site1");
				}}, VirtualTableScenario.Multipath,
				"the game would lay no site 1", "none of these sites is printed as site 1");

		// Ettenmoors is the Fellowship path's site 2 whatever key it is under.
		assertRefused(new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_331");
				}}, VirtualTableScenario.Multipath,
				"the game would lay no site 1", "The key \"site1\" holds Ettenmoors (1_331), which is printed as site 2.");
	}

	@Test
	public void TheSameSiteElsewhereOnANumberedPathIsNoTrouble() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario(new HashMap<>() {{
			putAll(VirtualTableScenario.FellowshipSites);
			put("site5", EastRoad);
		}}, VirtualTableScenario.Multipath);

		scn.StartGame();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("1_319", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASiteWithNoPrintedNumberLeavesANumberedPathNumberedWhateverKeyItStandsUnder() throws DecisionResultInvalidException, CardNotFoundException {
		// East Road in place of site 9, under a key that a HashMap hands out ahead of every "siteN": it was then
		// the first card of each adventure deck, the game took the whole path for an unordered one, and
		// StartGame() came to rest on "Choose site to play".
		var sites = new HashMap<String, String>() {{
			putAll(VirtualTableScenario.FellowshipSites);
			remove("site9");
			put("homestead", EastRoad);
		}};
		assertEquals("homestead", sites.keySet().iterator().next());

		var scn = GetScenario(sites, VirtualTableScenario.Multipath);
		var homestead = scn.GetFreepsSite("homestead");
		assertEquals(EastRoad, homestead.getBlueprintId());

		scn.StartGame();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("1_319", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// And at the move: the game lays site 2 by its number and asks the Shadow player for no site.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals("1_331", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Zone.ADVENTURE_DECK, homestead.getZone());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANumberedSiteAmongSitesWithNoPrintedNumberMakesThePathNumberedAndWithNoSiteOneItIsRefused() throws DecisionResultInvalidException, CardNotFoundException {
		// The other way about: Ettenmoors, printed as site 2, among Shadows-block sites.  It goes into the
		// adventure deck first whatever its key, so the game would lay by number, and there is no site 1.
		assertRefused(new HashMap<>() {{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site5", "1_331");
				}}, VirtualTableScenario.Shadows,
				"the game would lay no site 1",
				"The first card of the adventure deck (Ettenmoors, 1_331) has a printed site number",
				"The table puts the sites that have a printed number into the adventure deck ahead of those that have none.");
	}

	@Test
	public void TwoKeysThatHoldOneBlueprintNameItsTwoCopies() throws DecisionResultInvalidException, CardNotFoundException {
		// Two keys of the site map with one blueprint used to be given one alias between them: the first of the
		// two keys named one copy, the other key named nothing, and looking it up threw.
		var scn = GetScenario(new HashMap<>() {{
			putAll(VirtualTableScenario.ShadowsSites);
			put("site1", EastRoad);
			put("road", EastRoad);
		}}, VirtualTableScenario.Shadows);

		var site1 = scn.GetFreepsSite("site1");
		var road = scn.GetFreepsSite("road");
		assertEquals(EastRoad, site1.getBlueprintId());
		assertEquals(EastRoad, road.getBlueprintId());
		assertNotSame(site1, road);
		assertEquals(scn.P1(), site1.getOwner());
		assertEquals(scn.P1(), road.getOwner());

		// The second player's two copies have the same two names, and are his own.
		var shadowSite1 = scn.GetShadowSite("site1");
		var shadowRoad = scn.GetShadowSite("road");
		assertNotSame(shadowSite1, shadowRoad);
		assertEquals(scn.P2(), shadowSite1.getOwner());
		assertEquals(scn.P2(), shadowRoad.getOwner());

		// One is laid as the first site and the other is still in the adventure deck.  StartGame(site) chooses
		// the site by its blueprint, so which of two copies the game lays is not the test's to say.
		scn.StartGame(site1);
		var laid = scn.GetCurrentSite();
		assertTrue(laid == site1 || laid == road);
		assertEquals(Zone.ADVENTURE_DECK, (laid == site1 ? road : site1).getZone());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheSameSiteStartsAPathThatIsUnorderedThroughout() throws DecisionResultInvalidException, CardNotFoundException {
		// Every site without a printed number, in the default format: the first player chooses where to start.
		var scn = GetScenario(new HashMap<>() {{
			putAll(VirtualTableScenario.ShadowsSites);
			put("site1", EastRoad);
		}}, VirtualTableScenario.Multipath);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(EastRoad, scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The site's text is in force in the first fellowship phase: 4 + 2.
		assertEquals(6, scn.GetTwilightCost(aragorn));
	}
}
