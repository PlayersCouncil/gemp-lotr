package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertNotAttachedTo;
import static org.junit.Assert.*;

public class Card_03_106_ErrataTests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("bill", "53_106");
                    put("sam", "1_311");
                }}
        );
    }

    @Test
    public void BillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *Bill the Pony
         * Side: Free Peoples
         * Culture: Shire
         * Twilight Cost: 0
         * Type: Possession
         * Subtype: Pony
         * Errata Game Text: Stealth.  Bearer must be Sam.
         * Each site's Shadow number is -1. Discard this possession when at an underground site.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl bill = scn.GetFreepsCard("bill");

        assertTrue(bill.getBlueprint().isUnique());
        assertEquals(0, bill.getBlueprint().getTwilightCost());

        assertTrue(scn.HasKeyword(bill, Keyword.STEALTH));
    }

    @Test
    public void BillCanBeBorneBySam() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl bill = scn.GetFreepsCard("bill");
        PhysicalCardImpl sam = scn.GetFreepsCard("sam");

        scn.MoveCardsToHand(bill, sam);

        scn.StartGame();


        assertFalse(scn.FreepsPlayAvailable(bill));
        scn.FreepsPlayCard(sam);
        assertTrue(scn.FreepsPlayAvailable(bill));
        scn.FreepsPlayCard(bill);

        assertAttachedTo(bill, sam);
    }


    @Test
    public void BillReducesTwilight() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl bill = scn.GetFreepsCard("bill");
        PhysicalCardImpl sam = scn.GetFreepsCard("sam");

        scn.MoveCardsToHand(bill, sam);

        scn.StartGame();

        scn.FreepsPlayCard(sam);
        scn.FreepsPlayCard(bill);

        // 2 for Frodo/Sam, 1 for the site, -1 for Bill
        assertEquals(2, scn.GetTwilight());
    }


    @Test
    public void BillDiscardedWhenMovingToUnderground() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl bill = scn.GetFreepsCard("bill");
        PhysicalCardImpl sam = scn.GetFreepsCard("sam");

        scn.MoveCardsToHand(bill, sam);

        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.UNDERGROUND));

        scn.StartGame();

        scn.FreepsPlayCard(sam);
        scn.FreepsPlayCard(bill);

        scn.FreepsPassCurrentPhaseAction();

        assertNotAttachedTo(bill, sam);
        assertEquals(Zone.DISCARD, bill.getZone());
    }

}
