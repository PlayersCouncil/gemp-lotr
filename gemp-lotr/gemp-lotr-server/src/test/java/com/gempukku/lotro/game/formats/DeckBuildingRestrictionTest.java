package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The DeckBuildingRestriction effect of a card definition, at the level of its processor: the "min" and "max" of
 * how many cards of a pile may match its filter.  No card can show "min" (the three Maps that use the effect all
 * say "max: 0"), so the Maps here are built from definitions written for the test: "between min and max sites
 * of the Fellowship path".
 */
public class DeckBuildingRestrictionTest extends LoadedLibraries {

    // Sites 1 to 9 of the Fellowship path, and of the King path.
    private static final String[] FellowshipPath = {"1_319", "1_331", "1_341", "1_343", "1_349", "1_351", "1_353", "1_356", "1_360"};
    private static final String[] KingPath = {"7_330", "7_335", "8_117", "7_342", "7_345", "7_350", "8_120", "10_120", "7_360"};

    private static LotroCardBlueprint mapAskingForFellowshipSites(Integer min, Integer max) throws InvalidCardDefinitionException {
        var restriction = new JSONObject();
        restriction.put("type", "DeckBuildingRestriction");
        restriction.put("pile", "sites");
        restriction.put("filter", "siteblock(fellowship)");
        if (min != null)
            restriction.put("min", min);
        if (max != null)
            restriction.put("max", max);
        restriction.put("text", "Adventure deck must contain the right number of Fellowship sites");

        var card = new JSONObject();
        card.put("title", "Map of the Test");
        card.put("type", "Map");
        card.put("twilight", 0);
        card.put("effects", restriction);
        return new LotroCardBlueprintBuilder().buildFromJson("0_1", card);
    }

    /** An adventure deck of nine sites, the first so many of them of the Fellowship path and the rest of the King path. */
    private static boolean accepts(LotroCardBlueprint map, int fellowshipSites) throws CardNotFoundException {
        List<PhysicalCardImpl> sites = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            String id = i < fellowshipSites ? FellowshipPath[i] : KingPath[i];
            sites.add(new PhysicalCardImpl(i, id, "", _cardLibrary.getLotroCardBlueprint(id)));
        }
        return map.validatePreGameDeckCheck(new ArrayList<>(), new ArrayList<>(), sites, null, null, null).success();
    }

    @Test
    public void aDeckIsAcceptedFromMinToMaxMatchingCardsAndRefusedOnEitherSide() throws Exception {
        var map = mapAskingForFellowshipSites(2, 4);

        assertFalse(accepts(map, 1));
        assertTrue(accepts(map, 2));
        assertTrue(accepts(map, 3));
        assertTrue(accepts(map, 4));
        assertFalse(accepts(map, 5));
    }

    @Test
    public void minStandsByItselfWhereNoMaxIsGiven() throws Exception {
        var map = mapAskingForFellowshipSites(2, null);

        assertFalse(accepts(map, 0));
        assertFalse(accepts(map, 1));
        assertTrue(accepts(map, 2));
        assertTrue(accepts(map, 9));
    }

    @Test
    public void maxStandsByItselfWhereNoMinIsGiven() throws Exception {
        var map = mapAskingForFellowshipSites(null, 3);

        assertTrue(accepts(map, 0));
        assertTrue(accepts(map, 3));
        assertFalse(accepts(map, 4));
    }

    @Test
    public void aMinAboveTheMaxIsRefusedWhenTheDefinitionIsRead() {
        assertThrows(InvalidCardDefinitionException.class, () -> mapAskingForFellowshipSites(4, 2));
    }
}
