package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_13_080_Tests
{

    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("radagast", "13_80");
                    put("saruman", "12_144");
                    put("gandalf", "6_30");
                    put("fool", "8_14");

                }}
        );
    }


    @Test
    public void RadagastDeceivedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 13
         * Title: *Radagast Deceived
         * Side: Shadow
         * Culture: Isengard
         * Twilight Cost: 3
         * Type: Condition
         * Subtype: Support Area
         * Game Text: Each time a Wizard heals, this condition becomes a fierce Wizard minion until the start of the
         * regroup phase that has 12 strength and 1 vitality, and cannot take wounds or bear other cards. This radagast is still a condition.
         */

        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl radagast = scn.GetFreepsCard("radagast");

        assertTrue(radagast.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, radagast.getBlueprint().getSide());
        assertEquals(Culture.ISENGARD, radagast.getBlueprint().getCulture());
        assertEquals(CardType.CONDITION, radagast.getBlueprint().getCardType());
        assertTrue(scn.HasKeyword(radagast, Keyword.SUPPORT_AREA)); // test for keywords as needed
        assertEquals(3, radagast.getBlueprint().getTwilightCost());
        //assertEquals(, radagast.getBlueprint().getStrength());
        //assertEquals(, radagast.getBlueprint().getVitality());
        //assertEquals(, radagast.getBlueprint().getResistance());
        //assertEquals(Signet., radagast.getBlueprint().getSignet());
        //assertEquals(, radagast.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies
    }

    @Test
    public void RadagastDeceivedTurnsIntoAMinionIfSarumanHeals() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl radagast = scn.GetShadowCard("radagast");
        PhysicalCardImpl saruman = scn.GetShadowCard("saruman");
        scn.ShadowMoveCharToTable(saruman);
        scn.ShadowMoveCardToSupportArea(radagast);

        PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
        scn.FreepsMoveCharToTable(gandalf);

        scn.StartGame();
        scn.AddWoundsToChar(saruman, 1);
        scn.AddWoundsToChar(gandalf, 3);
        scn.FreepsPassCurrentPhaseAction();

        assertEquals(0, scn.GetVitality(radagast));
        assertEquals(0, scn.GetStrength(radagast));
        assertFalse(scn.HasKeyword(radagast, Keyword.FIERCE));
        assertFalse(scn.IsType(radagast, CardType.MINION));
        scn.ShadowAcceptOptionalTrigger(); // mithrandir self-wounding and dying, saruman self-healing as a result

        assertEquals(1, scn.GetVitality(radagast));
        assertEquals(12, scn.GetStrength(radagast));
        assertTrue(scn.HasKeyword(radagast, Keyword.FIERCE));
        assertTrue(scn.IsType(radagast, CardType.MINION));
        assertTrue(scn.IsType(radagast, CardType.CONDITION));

    }






}
