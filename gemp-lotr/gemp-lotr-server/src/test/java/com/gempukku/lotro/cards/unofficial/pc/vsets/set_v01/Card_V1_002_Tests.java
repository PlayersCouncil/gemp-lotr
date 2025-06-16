package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_002_Tests
{

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("gimli", "1_13");
                    put("axe", "1_9");
                    put("axe2", "1_9");
                    put("deep", "101_2");
                    put("emhr", "4_46");
                    put("strike", "1_3");

                    put("runner", "1_178");
                    put("plunder", "1_193");

                }}
        );
    }


    @Test
    public void DeepestDelvingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: VSet1, VPack1
         * Title: *Deepest Delvings
         * Side: Free Peoples
         * Culture: Dwarven
         * Twilight Cost: 1
         * Type: Condition
         * Subtype: Support Area
         * Game Text: Each time you discard a [dwarven] card from the top of your deck you may add (1) to stack that card here.
         *  Maneuver: Exert a dwarf to take a card stacked here into hand.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl deep = scn.GetFreepsCard("deep");

        assertTrue(scn.HasKeyword(deep, Keyword.SUPPORT_AREA));
        assertEquals(1, deep.getBlueprint().getTwilightCost());
        assertTrue(deep.getBlueprint().isUnique());
    }

    @Test
    public void DiscardingDwarvenCardFromDeckTriggersOptionalStack() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl emhr = scn.GetFreepsCard("emhr");
        PhysicalCardImpl deep = scn.GetFreepsCard("deep");
        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
        PhysicalCardImpl axe = scn.GetFreepsCard("axe2");
        PhysicalCardImpl strike = scn.GetFreepsCard("strike");
        scn.MoveCardsToSupportArea(emhr);
        scn.MoveCardsToSupportArea(deep);
        scn.MoveCompanionToTable(gimli);
        scn.MoveCardsToFreepsDiscard("axe", "runner", "plunder");

        scn.StartGame();

        scn.MoveCardsToTopOfDeck(strike);

        scn.FreepsChooseAction("Discard 1");

        assertTrue(scn.FreepsHasOptionalTriggerAvailable());
        scn.FreepsAcceptOptionalTrigger();
        assertEquals(1, scn.GetTwilight());
        assertEquals(deep, strike.getStackedOn());

    }

    @Test
    public void ManeuverAbilityExertsToTakeStackedCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl emhr = scn.GetFreepsCard("emhr");
        PhysicalCardImpl deep = scn.GetFreepsCard("deep");
        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
        PhysicalCardImpl strike = scn.GetFreepsCard("strike");
        scn.MoveCardsToSupportArea(emhr);
        scn.MoveCardsToHand(deep);
        scn.MoveCompanionToTable(gimli);
        scn.MoveCardsToFreepsDiscard("axe", "runner", "plunder");
        scn.MoveCardsToTopOfDeck(strike);

        scn.MoveMinionsToTable("runner");

        scn.StartGame();

        scn.FreepsPlayCard(deep);
        scn.StackCardsOn(deep, strike);

//        scn.FreepsUseCardAction("Discard 1");
//        assertTrue(scn.FreepsHasOptionalTriggerAvailable());
//        scn.FreepsAcceptOptionalTrigger();

        scn.SkipToPhase(Phase.MANEUVER);

        assertEquals(0, scn.GetWoundsOn(gimli));
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(1, scn.GetStackedCards(deep).size());
        assertEquals(Zone.STACKED, strike.getZone());
        assertEquals(deep, strike.getStackedOn());
        assertTrue(scn.FreepsActionAvailable("Deepest"));

        scn.FreepsUseCardAction(deep);

        assertEquals(1, scn.GetWoundsOn(gimli));
        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(0, scn.GetStackedCards(deep).size());
        assertEquals(Zone.HAND, strike.getZone());

    }




}
