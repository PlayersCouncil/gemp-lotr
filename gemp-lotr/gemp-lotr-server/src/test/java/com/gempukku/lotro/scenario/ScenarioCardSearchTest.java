package com.gempukku.lotro.scenario;

import com.gempukku.lotro.framework.LoadedLibraries;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
        assertEquals("companion", legolas.get("type"));
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
}
