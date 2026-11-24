package com.gempukku.lotro.scenario;

import com.gempukku.lotro.framework.LoadedLibraries;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

/**
 * The card search a sandbox's + and the authoring panel share (ScenarioCardSearch): the deck builder's filter over
 * every card once, in pages.
 */
public class ScenarioCardSearchTest extends LoadedLibraries {
    private final ScenarioCardSearch search = new ScenarioCardSearch(_cardLibrary, _formatLibrary);

    @Test
    @SuppressWarnings("unchecked")
    public void FindsWithTheDeckBuildersFilterInPages() {
        Map<String, Object> found = search.search("name:Legolas cardType:COMPANION set:1", 0, 10);
        List<Map<String, Object>> cards = (List<Map<String, Object>>) found.get("cards");
        assertTrue(cards.stream().anyMatch(card -> card.get("blueprintId").equals("1_50")));
        Map<String, Object> legolas = cards.stream().filter(card -> card.get("blueprintId").equals("1_50")).findFirst().orElseThrow();
        assertEquals("Legolas, Greenleaf", legolas.get("name"));
        //(the card type as the state writes it)
        assertEquals("COMPANION", legolas.get("type"));
        assertEquals("free_people", legolas.get("side"));
        assertEquals("elven", legolas.get("culture"));
        assertEquals(false, legolas.get("site"));
        assertEquals(2, legolas.get("twilight"));

        //Sites say their number; a page past the end is empty, with the total all the same
        Map<String, Object> sites = search.search("cardType:SITE set:1", 0, 500);
        int total = (int) sites.get("total");
        assertTrue(total > 9);
        assertTrue(((List<?>) sites.get("cards")).size() <= ScenarioCardSearch.MOST);
        Map<String, Object> site = ((List<Map<String, Object>>) sites.get("cards")).getFirst();
        assertEquals(true, site.get("site"));
        assertTrue((int) site.get("siteNumber") >= 1);
        Map<String, Object> past = search.search("cardType:SITE set:1", total, 10);
        assertEquals(total, past.get("total"));
        assertEquals(List.of(), past.get("cards"));

        //No filter: every card in the library, once
        assertTrue((int) search.search(null, 0, 1).get("total") > 1000);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ABoundIsCheckedAsStrictlyAsSavingItAndCounted() {
        Map<String, Object> allies = search.bound("culture:ROHAN cardtype:ALLY", null);
        assertEquals(true, allies.get("ok"));
        assertNull(allies.get("why"));
        int all = (int) allies.get("total");
        assertTrue(all > 1);
        //With a pool: what both find, each searched with by itself
        Map<String, Object> early = search.bound("culture:ROHAN cardtype:ALLY", "set:1-6");
        int inPool = (int) early.get("total");
        assertTrue(inPool >= 1 && inPool < all);
        //(the same key in both is "both", not "either": set:4 and the pool's set:1-6 find set 4's)
        assertEquals(search.bound("culture:ROHAN cardtype:ALLY set:4", null).get("total"), search.bound("culture:ROHAN cardtype:ALLY set:4", "set:1-6").get("total"));
        //Refused as saving it would refuse it
        Map<String, Object> wrong = search.bound("colour:red", null);
        assertEquals(false, wrong.get("ok"));
        assertTrue((String) wrong.get("why"), ((String) wrong.get("why")).contains("has no 'colour'"));
        assertNull(wrong.get("total"));
        assertEquals(false, search.bound("cardtype:ally", null).get("ok"));
        assertEquals(false, search.bound("format:fotr_block", null).get("ok"));
        assertTrue(((String) search.bound("culture:ROHAN", "set:x").get("why")).startsWith("pool: "));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ASearchInsideABoundNeverGoesOutsideIt() throws Exception {
        //Typed culture:GONDOR inside a Rohan-ally bound: as one text that would be "Rohan or Gondor"; each by itself, nothing
        Map<String, Object> none = search.search("culture:GONDOR", "set:1-6", "culture:ROHAN cardtype:ALLY", false, 0, 50);
        assertEquals(0, none.get("total"));
        //Typed name words narrow it
        Map<String, Object> aldor = search.search("name:Aldor", "set:1-6", "culture:ROHAN cardtype:ALLY", false, 0, 50);
        List<Map<String, Object>> cards = (List<Map<String, Object>>) aldor.get("cards");
        assertTrue(cards.toString(), cards.stream().anyMatch(card -> card.get("blueprintId").equals("4_262")));
        assertTrue(cards.stream().allMatch(card -> card.get("type").equals("ALLY") && card.get("culture").equals("rohan")));
        //Nothing typed: the bound and the pool, as bound counts them
        assertEquals(search.bound("culture:ROHAN cardtype:ALLY", "set:1-6").get("total"),
                search.search(null, "set:1-6", "culture:ROHAN cardtype:ALLY", false, 0, 1).get("total"));
        //Strict: an unknown key is refused, not ignored; leniently it is ignored, as before
        try {
            search.search("colour:red", null, null, true, 0, 10);
            fail("A key the search does not know is refused when strict");
        } catch (ScenarioCardSearch.Refused exp) {
            assertTrue(exp.getMessage(), exp.getMessage().startsWith("filter: ") && exp.getMessage().contains("has no 'colour'"));
        }
        assertEquals(search.search(null, 0, 1).get("total"), search.search("colour:red", null, null, false, 0, 1).get("total"));
        //(a format is something a person may type, strictly or not)
        assertTrue((int) search.search("format:fotr_block", null, null, true, 0, 1).get("total") > 0);
        //A bound that is no bound is refused whatever strict says
        try {
            search.search(null, null, "cardtype:ally", false, 0, 10);
            fail("A mistaken bound is refused");
        } catch (ScenarioCardSearch.Refused exp) {
            assertTrue(exp.getMessage(), exp.getMessage().startsWith("bound: "));
        }
        assertFalse(search.search("name:Aldor", "set:1-3", "culture:ROHAN cardtype:ALLY", false, 0, 50).toString().contains("4_262"));
    }
}
