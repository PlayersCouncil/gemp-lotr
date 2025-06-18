package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_02_121_ErrataTests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("gimli", "52_121");
                }}
        );
    }

    @Test
    public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *Gimli
         * Subtitle: Dwarf of the Mountain-race
         * Side: Free Peoples
         * Culture: Dwarven
         * Twilight Cost: 2
         * Type: Companion
         * Subtype: Dwarf
         * Strength: 6
         * Vitality: 3
         * Signet: Frodo
         * Errata Game Text: Damage +1.  Each underground site's Shadow number is -1.  While the fellowship is at an underground site, Gimli is strength +1.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");

        assertTrue(gimli.getBlueprint().isUnique());
        assertEquals(2, gimli.getBlueprint().getTwilightCost());

        assertEquals(6, gimli.getBlueprint().getStrength());
        assertEquals(3, gimli.getBlueprint().getVitality());
        assertEquals(Signet.FRODO, gimli.getBlueprint().getSignet());

        assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
    }


    @Test
    public void GimliRemovesTwilightWhenUnderground() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(gimli);

        scn.StartGame();

        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.UNDERGROUND));

        scn.FreepsPassCurrentPhaseAction();

        // 2 for Frodo/Gimli, 1 for the site, -1 for Gimli's text
        assertEquals(2, scn.GetTwilight());
    }

    @Test
    public void GimliDoesNotRemoveTwilightWhenNotUnderground() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(gimli);

        scn.StartGame();

        scn.FreepsPassCurrentPhaseAction();

        // 2 for Frodo/Gimli, 1 for the site
        assertEquals(3, scn.GetTwilight());
    }

    @Test
    public void GimliIsStrengthPlus1WhenUnderground() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(gimli);

        scn.StartGame();

        assertEquals(6, scn.GetStrength(gimli));

        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.UNDERGROUND));

        scn.FreepsPassCurrentPhaseAction();

        assertEquals(7, scn.GetStrength(gimli));
    }


}
