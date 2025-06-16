package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_045_ErrataTests
{
    protected VirtualTableScenario GetSimplePlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("galadriel", "51_45");
                    put("elrond", "1_40");
                }}
        );
    }

    protected VirtualTableScenario GetHome6AllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("galadriel", "51_45");
                    put("allyHome3_1", "1_60");
                    put("allyHome6_1", "1_56");
                    put("allyHome6_2", "1_57");
                    put("allyHome6_3", "1_34");
                }}
        );
    }

    @Test
    public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *Galadriel
         * Subtitle: Lady of Light
         * Side: Free Peoples
         * Culture: Elven
         * Twilight Cost: 3
         * Type: Ally
         * Subtype: Elf
         * Home: 6
         * Strength: 3
         * Vitality: 3
         * Errata Game Text: At the start of each of your turns, heal up to 3 allies whose home is site 6.
         * Fellowship: Exert Galadriel to play an Elf; that Elf's twilight cost is -1.
         */

        //Pre-game setup

        VirtualTableScenario scn = GetSimplePlayScenario();

        PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");

        assertTrue(galadriel.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, galadriel.getBlueprint().getSide());
        assertEquals(Culture.ELVEN, galadriel.getBlueprint().getCulture());
        assertEquals(CardType.ALLY, galadriel.getBlueprint().getCardType());
        assertEquals(Race.ELF, galadriel.getBlueprint().getRace());
        assertEquals(3, galadriel.getBlueprint().getTwilightCost());
        assertEquals(3, galadriel.getBlueprint().getStrength());
        assertEquals(3, galadriel.getBlueprint().getVitality());
		assertTrue(galadriel.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
    }

    @Test
    public void FellowshipActionExertsToDiscountAnElf() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetSimplePlayScenario();

        PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
        scn.MoveCompanionToTable(galadriel);
        scn.MoveCardsToFreepsHand("elrond");

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertTrue(scn.FreepsActionAvailable(galadriel));

        assertEquals(0, scn.GetWoundsOn(galadriel));
        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(0, scn.GetTwilight());

        scn.FreepsUseCardAction(galadriel);

        assertEquals(1, scn.GetWoundsOn(galadriel));
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(3, scn.GetTwilight());
    }


    @Test
    public void AllyHealsCappedAt3() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetHome6AllyScenario();
        var galadriel = scn.GetFreepsCard("galadriel");
        var allyHome3_1 = scn.GetFreepsCard("allyHome3_1");
        var allyHome6_1 = scn.GetFreepsCard("allyHome6_1");
        var allyHome6_2 = scn.GetFreepsCard("allyHome6_2");
        var allyHome6_3 = scn.GetFreepsCard("allyHome6_3");
        scn.MoveCompanionToTable(galadriel, allyHome3_1, allyHome6_1, allyHome6_2, allyHome6_3);

        scn.AddWoundsToChar(galadriel, 1);
        scn.AddWoundsToChar(allyHome3_1, 1);
        scn.AddWoundsToChar(allyHome6_1, 1);
        scn.AddWoundsToChar(allyHome6_2, 1);
        scn.AddWoundsToChar(allyHome6_3, 1);

        scn.StartGame();

        assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());

        //There are 5 total elf allies in play: 1 is home 3.  The rest should be eligible.
        assertEquals(4, scn.FreepsGetADParamAsList("cardId").size());
        assertEquals("0", scn.FreepsGetADParam("min")[0]);
        assertEquals("3", scn.FreepsGetADParam("max")[0]);
    }
}
