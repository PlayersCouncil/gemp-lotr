package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_108_Tests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("arwen", "1_30");
                    put("boromir", "1_97");
                    put("nostranger", "1_108");
                    put("nostranger2", "1_108");
                }}
        );
    }


    @Test
    public void NoStrangerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1
         * Name: No Stranger to the Shadows
         * Unique: True
         * Side: Free Peoples
         * Culture: Gondor
         * Twilight Cost: 0
         * Type: Condition
         * Game Text: Bearer must be a ranger. Limit 1 per ranger.<br>Each site's Shadow number is -1.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsCard("nostranger");

        assertEquals("No Stranger to the Shadows", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
        assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
        assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
        assertEquals(0, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void NoStrangerCanOnlyPlayOnRanger() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var arwen = scn.GetFreepsCard("arwen");
        var boromir = scn.GetFreepsCard("boromir");
        var nostranger = scn.GetFreepsCard("nostranger");

        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCharToTable(arwen);
        scn.FreepsMoveCharToTable(boromir);
        scn.FreepsMoveCardToHand(nostranger);

        scn.StartGame();

        assertTrue(scn.FreepsPlayAvailable(nostranger));

        scn.FreepsPlayCard(nostranger);

        //There are 3 companions in play, but only 2 rangers, so we should only see 2 options
        assertEquals(2, scn.FreepsGetADParamAsList("cardId").size());
    }


    @Test
    public void NoStrangerReducesTwilight() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var nostranger = scn.GetFreepsCard("nostranger");

        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(nostranger);

        scn.StartGame();

        scn.FreepsPlayCard(nostranger);

        scn.FreepsPassCurrentPhaseAction();

        // 2 for Frodo/Aragorn, 1 for the site, -1 for No Stranger
        assertEquals(2, scn.GetTwilight());
    }
}
