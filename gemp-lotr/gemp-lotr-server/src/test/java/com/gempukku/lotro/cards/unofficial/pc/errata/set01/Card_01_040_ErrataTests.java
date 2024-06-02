package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_040_ErrataTests
{

    protected GenericCardTestHelper GetSimpleDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("elrond", "51_40");
                    put("randomcard", "1_3");
                }}
        );
    }

    protected GenericCardTestHelper GetSimpleSpotScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("elrond", "51_40");
                    put("gandalf", "1_72");
                    put("arwen", "1_30");
                }}
        );
    }

    protected GenericCardTestHelper GetHome3AllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("elrond", "51_40");
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
         * At the start of each of your turns, heal up to 3 allies whose home is site 3.
         * Fellowship: Exert Elrond twice (or once if you can spot 2 other [elven] allies) to draw a card.
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
		assertTrue(elrond.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
    }

    @Test
    public void FellowshipActionExertsTwiceToDrawACardIfNoAllies() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetSimpleDeckScenario();
        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");

        scn.FreepsMoveCharToTable(elrond);

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertTrue(scn.FreepsActionAvailable(elrond));

        assertEquals(0, scn.GetWoundsOn(elrond));
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(1, scn.GetFreepsDeckCount());

        scn.FreepsUseCardAction(elrond);

        assertEquals(2, scn.GetWoundsOn(elrond));
        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(0, scn.GetFreepsDeckCount());
    }

    @Test
    public void FellowshipActionExertsOnceToDrawACardIf2Allies() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetHome3AllyScenario();
        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
        scn.FreepsMoveCharToTable("allyHome3_1");
        scn.FreepsMoveCharToTable("allyHome6_1");

        scn.FreepsMoveCharToTable(elrond);

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertTrue(scn.FreepsActionAvailable(elrond));

        assertEquals(0, scn.GetWoundsOn(elrond));
        assertEquals(0, scn.GetFreepsHandCount());
        //assertEquals(2, scn.GetFreepsDeckCount());

        scn.FreepsUseCardAction(elrond);

        assertEquals(1, scn.GetWoundsOn(elrond));
        assertEquals(1, scn.GetFreepsHandCount());
        //assertEquals(1, scn.GetFreepsDeckCount());
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
    public void AllyHealsCappedAt3() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetHome3AllyScenario();
        scn.FreepsMoveCharToTable("elrond");
        scn.FreepsMoveCharToTable("allyHome3_1");
        scn.FreepsMoveCharToTable("allyHome3_2");
        scn.FreepsMoveCharToTable("allyHome6_1");
        scn.FreepsMoveCharToTable("allyHome6_2");

        scn.FreepsAddWoundsToChar("elrond", 1);
        scn.FreepsAddWoundsToChar("allyHome3_1", 1);
        scn.FreepsAddWoundsToChar("allyHome3_2", 1);
        scn.FreepsAddWoundsToChar("allyHome6_1", 1);
        scn.FreepsAddWoundsToChar("allyHome6_2", 1);

        scn.StartGame();

        assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());

        assertEquals(3, scn.FreepsGetADParamAsList("cardId").size());
        assertEquals("0", scn.FreepsGetADParam("min")[0]);
        assertEquals("3", scn.FreepsGetADParam("max")[0]);
    }
}
