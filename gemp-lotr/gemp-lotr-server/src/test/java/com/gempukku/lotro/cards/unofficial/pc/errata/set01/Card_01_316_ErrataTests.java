package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_01_316_ErrataTests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("talent", "51_316");
                    put("sam", "1_311");
                    put("merry", "1_302");
                    put("pippin", "1_307");
                    put("boromir", "1_97");
                }}
        );
    }

    @Test
    public void TalentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: A Talent for Not Being Seen
         * Side: Free Peoples
         * Culture: Shire
         * Twilight Cost: 0
         * Type: Condition
         * Errata Game Text: Stealth.  Bearer must be Merry or Pippin.  Limit 1 per character.
         * Each site's Shadow number is -1.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var talent = scn.GetFreepsCard("talent");

        assertFalse(talent.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, talent.getBlueprint().getSide());
        assertEquals(Culture.SHIRE, talent.getBlueprint().getCulture());
        assertEquals(CardType.CONDITION, talent.getBlueprint().getCardType());
        assertEquals(0, talent.getBlueprint().getTwilightCost());

        assertTrue(scn.HasKeyword(talent, Keyword.STEALTH));
        assertFalse(scn.HasKeyword(talent, Keyword.SUPPORT_AREA));
    }

    @Test
    public void TalentOnlyPlaysOnMerryOrPippin() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl frodo = scn.GetRingBearer();
        PhysicalCardImpl sam = scn.GetFreepsCard("sam");
        PhysicalCardImpl merry = scn.GetFreepsCard("merry");
        PhysicalCardImpl pippin = scn.GetFreepsCard("pippin");
        PhysicalCardImpl talent = scn.GetFreepsCard("talent");

        scn.MoveCompanionToTable(sam);
        scn.MoveCompanionToTable(merry);
        scn.MoveCompanionToTable(pippin);
        scn.MoveCardsToHand(talent);

        scn.StartGame();

        scn.FreepsPlayCard(talent);

        //There are 4 companions in play, but only 2 valid targets
        assertEquals(2, scn.FreepsGetADParamAsList("cardId").size());
    }


    @Test
    public void TalentReducesTwilightIfOnlyHobbits() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl frodo = scn.GetRingBearer();
        PhysicalCardImpl merry = scn.GetFreepsCard("merry");
        PhysicalCardImpl talent = scn.GetFreepsCard("talent");

        scn.MoveCompanionToTable(merry);
        scn.MoveCardsToHand(talent);

        scn.StartGame();

        scn.FreepsPlayCard(talent);
        scn.FreepsPassCurrentPhaseAction();

        // 2 for Frodo/Merry, 1 for the site, -1 for Talent
        assertEquals(2, scn.GetTwilight());

    }



}
