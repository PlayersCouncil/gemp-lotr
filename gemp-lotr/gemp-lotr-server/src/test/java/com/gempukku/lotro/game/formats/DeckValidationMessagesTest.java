package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.framework.DeckValidationScenario;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * What the deck builder prints when a deck is refused: the wording of the messages, which no card test reads (a
 * card's test asks whether an objection was made and to which cards, not how it is spelt).
 */
public class DeckValidationMessagesTest {

    private static List<String> validate(String mapId, HashMap<String, String> sites) {
        var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
        return dvs.validate(dvs.buildDeckWithMap(mapId, sites));
    }

    @Test
    public void eachSiteThatIsRefusedHasALineOfItsOwn() {
        // Nine Shadows sites, none of them from a set PC Movie plays.  The sites' errors come as one entry of the
        // list, a line for each site.
        var errors = validate("102_74", DeckValidationScenario.ShadowsSites);
        var siteErrors = errors.stream().filter(e -> e.startsWith("Card not from valid set: ")).findFirst().orElseThrow();

        var lines = Arrays.asList(siteErrors.split("\n"));
        assertEquals(siteErrors, 9, lines.size());
        for (String line : lines) {
            assertTrue(line, line.startsWith("Card not from valid set: "));
            assertEquals(line, 1, line.split("Card not from valid set: ", -1).length - 1);
        }
        assertTrue(siteErrors, lines.contains("Card not from valid set: Falls of Rauros"));
        assertTrue(siteErrors, lines.contains("Card not from valid set: Emyn Muil"));
    }

    @Test
    public void aMapsObjectionIsOneSentenceInTheCardsOwnTermsForEachOfTheThreeMaps() {
        // Eight sites of the Map's own path and one stray: Palantír Chamber (Towers) or Emyn Muil (Fellowship).
        var fellowship = new HashMap<>(DeckValidationScenario.FellowshipSites);
        fellowship.put("site9", "4_363");
        assertTrue(validate("102_74", fellowship).contains(
                "Deck does not meet restrictions placed by Path of the Fellowship: "
                        + "Adventure deck must only contain sites from the Fellowship site path.  Found 1 issues: Palantír Chamber"));

        var towers = new HashMap<>(DeckValidationScenario.TowersSites);
        towers.put("site9", "1_360");
        assertTrue(validate("102_75", towers).contains(
                "Deck does not meet restrictions placed by Road to the Towers: "
                        + "Adventure deck must only contain sites from the Towers site path.  Found 1 issues: Emyn Muil"));

        var king = new HashMap<>(DeckValidationScenario.KingSites);
        king.put("site9", "1_360");
        assertTrue(validate("102_76", king).contains(
                "Deck does not meet restrictions placed by Journey of the King: "
                        + "Adventure deck must only contain sites from the King site path.  Found 1 issues: Emyn Muil"));
    }
}
