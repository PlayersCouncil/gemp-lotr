package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_045_Tests
{
    protected GenericCardTestHelper GetSimplePlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("galadriel", "1_45");
                    put("elrond", "1_40");
                }}
        );
    }

    protected GenericCardTestHelper GetHome6AllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("galadriel", "1_45");
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
         * Set: 1
         * Name: Galadriel, Lady of Light
         * Unique: True
         * Side: Free Peoples
         * Culture: Elven
         * Twilight Cost: 3
         * Type: Ally
         * Subtype: Elf
         * Home: 6
         * Strength: 3
         * Vitality: 3
         * Errata Game Text: At the start of each of your turns, heal every ally whose home is site 6.
         * Fellowship: Exert Galadriel to play an Elf for free.
         */

        var scn = GetSimplePlayScenario();

        var card = scn.GetFreepsCard("galadriel");

        assertEquals("Galadriel", card.getBlueprint().getTitle());
        assertEquals("Lady of Light", card.getBlueprint().getSubtitle());
        assertTrue(card.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
        assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
        assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
        assertEquals(Race.ELF, card.getBlueprint().getRace());
        assertEquals(3, card.getBlueprint().getTwilightCost());
        assertEquals(3, card.getBlueprint().getStrength());
        assertEquals(3, card.getBlueprint().getVitality());
        assertEquals(6, card.getBlueprint().getAllyHomeSiteNumbers()[0]);
        assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getAllyHomeSiteBlock());
    }

    @Test
    public void FellowshipActionExertsToDiscountAnElf() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetSimplePlayScenario();

        var galadriel = scn.GetFreepsCard("galadriel");
        scn.FreepsMoveCharToTable(galadriel);
        scn.FreepsMoveCardToHand("elrond");

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertTrue(scn.FreepsActionAvailable(galadriel));

        assertEquals(0, scn.GetWoundsOn(galadriel));
        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(0, scn.GetTwilight());

        scn.FreepsUseCardAction(galadriel);

        assertEquals(1, scn.GetWoundsOn(galadriel));
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(0, scn.GetTwilight());
    }


    @Test
    public void Site6AlliesAllHeal() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetHome6AllyScenario();
        var galadriel = scn.GetFreepsCard("galadriel");
        var allyHome3_1 = scn.GetFreepsCard("allyHome3_1");

        var allyHome6_1 = scn.GetFreepsCard("allyHome6_1");
        var allyHome6_2 = scn.GetFreepsCard("allyHome6_2");
        var allyHome6_3 = scn.GetFreepsCard("allyHome6_3");

        scn.FreepsMoveCharToTable(galadriel, allyHome3_1, allyHome6_3, allyHome6_1, allyHome6_2);

        scn.AddWoundsToChar(galadriel, 1);
        scn.AddWoundsToChar(allyHome3_1, 1);
        scn.AddWoundsToChar(allyHome6_3, 1);
        scn.AddWoundsToChar(allyHome6_1, 1);
        scn.AddWoundsToChar(allyHome6_2, 1);

        assertEquals(1, scn.GetWoundsOn(galadriel));
        assertEquals(1, scn.GetWoundsOn(allyHome3_1));
        assertEquals(1, scn.GetWoundsOn(allyHome6_3));
        assertEquals(1, scn.GetWoundsOn(allyHome6_1));
        assertEquals(1, scn.GetWoundsOn(allyHome6_2));

        scn.StartGame();

        assertEquals(0, scn.GetWoundsOn(galadriel));
        assertEquals(1, scn.GetWoundsOn(allyHome3_1));
        assertEquals(0, scn.GetWoundsOn(allyHome6_3));
        assertEquals(0, scn.GetWoundsOn(allyHome6_1));
        assertEquals(0, scn.GetWoundsOn(allyHome6_2));
    }
}
