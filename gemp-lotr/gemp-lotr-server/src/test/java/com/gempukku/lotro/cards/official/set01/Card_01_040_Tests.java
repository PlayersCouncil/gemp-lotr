package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_040_Tests
{

    protected GenericCardTestHelper GetSimpleDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("elrond", "1_40");
                    put("randomcard", "1_3");
                }}
        );
    }

    protected GenericCardTestHelper GetSimpleSpotScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("elrond", "1_40");
                    put("gandalf", "1_72");
                    put("arwen", "1_30");
                }}
        );
    }

    protected GenericCardTestHelper GetHome3AllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("elrond", "1_40");
                    put("allyHome3_1", "1_60");
                    put("allyHome3_2", "1_27"); // thrarin
                    put("allyHome6_1", "1_56");
                    put("allyHome6_2", "1_57");
                }}
        );
    }

    @Test
    public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *Elrond
         * Subtitle: Lord of Rivendell
         * Side: Free Peoples
         * Culture: Elven
         * Twilight Cost: 4
         * Type: Ally
         * Subtype: Elf
         * Home: 3
         * Strength: 8
         * Vitality: 4
         * Errata Game Text: To play, spot Gandalf or an Elf.
         * At the start of each of your turns, heal every ally whose home is site 3.
         * Fellowship: Exert Elrond to draw a card.
         */

        //Pre-game setup
        GenericCardTestHelper scn = GetSimpleDeckScenario();

        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");

        assertTrue(elrond.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, elrond.getBlueprint().getSide());
        assertEquals(Culture.ELVEN, elrond.getBlueprint().getCulture());
        assertEquals(CardType.ALLY, elrond.getBlueprint().getCardType());
        assertEquals(Race.ELF, elrond.getBlueprint().getRace());
        assertEquals(4, elrond.getBlueprint().getTwilightCost());
        assertEquals(8, elrond.getBlueprint().getStrength());
        assertEquals(4, elrond.getBlueprint().getVitality());
        assertEquals(3, elrond.getBlueprint().getAllyHomeSiteNumbers()[0]);
        assertEquals(SitesBlock.FELLOWSHIP, elrond.getBlueprint().getAllyHomeSiteBlock());
    }

    @Test
    public void FellowshipActionExertsToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetHome3AllyScenario();
        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");

        scn.FreepsMoveCharToTable(elrond);

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertTrue(scn.FreepsActionAvailable(elrond));

        assertEquals(0, scn.GetWoundsOn(elrond));
        assertEquals(0, scn.GetFreepsHandCount());

        scn.FreepsUseCardAction(elrond);

        assertEquals(1, scn.GetWoundsOn(elrond));
        assertEquals(1, scn.GetFreepsHandCount());
    }

    @Test
    public void CardCanPlayIfGandalfInPlay() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetSimpleSpotScenario();
        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
        PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");

        scn.FreepsMoveCardToHand(elrond);
        scn.FreepsMoveCardToHand(gandalf);

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertFalse(scn.FreepsPlayAvailable(elrond));

        scn.FreepsPlayCard(gandalf);
        assertTrue(scn.FreepsPlayAvailable(elrond));
    }

    @Test
    public void CardCanPlayIfElfInPlay() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetSimpleSpotScenario();
        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
        PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");

        scn.FreepsMoveCardToHand(elrond);
        scn.FreepsMoveCardToHand(arwen);

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertFalse(scn.FreepsPlayAvailable(elrond));

        scn.FreepsPlayCard(arwen);
        assertTrue(scn.FreepsPlayAvailable(elrond));
    }

    @Test
    public void Site3AlliesAllHeal() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetHome3AllyScenario();
        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
        PhysicalCardImpl allyHome3_1 = scn.GetFreepsCard("allyHome3_1");
        PhysicalCardImpl allyHome3_2 = scn.GetFreepsCard("allyHome3_2");
        PhysicalCardImpl allyHome6_1 = scn.GetFreepsCard("allyHome6_1");
        PhysicalCardImpl allyHome6_2 = scn.GetFreepsCard("allyHome6_2");

        scn.FreepsMoveCharToTable(elrond, allyHome3_1, allyHome3_2, allyHome6_1, allyHome6_2);

        scn.AddWoundsToChar(elrond, 1);
        scn.AddWoundsToChar(allyHome3_1, 1);
        scn.AddWoundsToChar(allyHome3_2, 1);
        scn.AddWoundsToChar(allyHome6_1, 1);
        scn.AddWoundsToChar(allyHome6_2, 1);

        assertEquals(1, scn.GetWoundsOn(elrond));
        assertEquals(1, scn.GetWoundsOn(allyHome3_1));
        assertEquals(1, scn.GetWoundsOn(allyHome3_2));
        assertEquals(1, scn.GetWoundsOn(allyHome6_1));
        assertEquals(1, scn.GetWoundsOn(allyHome6_2));

        scn.StartGame();

        assertEquals(0, scn.GetWoundsOn(elrond));
        assertEquals(0, scn.GetWoundsOn(allyHome3_1));
        assertEquals(0, scn.GetWoundsOn(allyHome3_2));
        assertEquals(1, scn.GetWoundsOn(allyHome6_1));
        assertEquals(1, scn.GetWoundsOn(allyHome6_2));
    }
}
