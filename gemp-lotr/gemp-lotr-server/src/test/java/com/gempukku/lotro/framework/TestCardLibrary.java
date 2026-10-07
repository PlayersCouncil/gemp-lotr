package com.gempukku.lotro.framework;

import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.common.BlueprintUtils;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;

import java.util.Collections;
import java.util.Map;

/**
 * The card library of the unit test rig: every real card, and beside them the card definitions that belong to the
 * tests.  Those are shapes of definition that no real card has yet and that a mechanic test needs; they are read
 * from one resource in the test sources, are known to every table by their ids (set 0), and are in no format's
 * card pool.
 */
public class TestCardLibrary extends LotroCardBlueprintLibrary {
    /** The resource, in the test sources, that holds the tests' own card definitions. */
    public static final String Resource = "/testCards.hjson";

    private final Map<String, LotroCardBlueprint> _testCards = load();

    private static Map<String, LotroCardBlueprint> load() {
        try (var stream = TestCardLibrary.class.getResourceAsStream(Resource)) {
            if (stream == null)
                return Collections.emptyMap();
            return LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(), stream);
        } catch (Exception exp) {
            throw new RuntimeException("Unable to read the tests' own card definitions from " + Resource, exp);
        }
    }

    @Override
    public LotroCardBlueprint getLotroCardBlueprint(String blueprintId) throws CardNotFoundException {
        final LotroCardBlueprint own = _testCards.get(BlueprintUtils.stripModifiers(blueprintId));
        return own != null ? own : super.getLotroCardBlueprint(blueprintId);
    }
}
