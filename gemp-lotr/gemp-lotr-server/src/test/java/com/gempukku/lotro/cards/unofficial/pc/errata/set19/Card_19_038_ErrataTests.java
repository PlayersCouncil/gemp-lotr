package com.gempukku.lotro.cards.unofficial.pc.errata.set19;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_19_038_ErrataTests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("nertea", "69_38");
                    put("boromir", "1_96");
                    put("aragorn", "1_89");
                    put("arwen", "1_30");
                    put("legolas", "1_50");
                }}
        );
    }

    @Test
    public void NerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 19E
         * Title: Ulaire Nertea
         * Subtitle: Dark Horseman
         * Side: Shadow
         * Culture: Ringwraith
         * Twilight Cost: 4
         * Type: Minion
         * Race: Nazgul
         * Strength: 9
         * Vitality: 2
         * Site: 3
         * Errata Game Text: Fierce.  When you play Ulaire Nertea, name a race.  The Free Peoples player must add a
         * burden to assign Ulaire Nertea to skirmish a companion of the named race.
         */

        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl nertea = scn.GetFreepsCard("nertea");

        assertTrue(nertea.getBlueprint().isUnique());
        assertEquals(4, nertea.getBlueprint().getTwilightCost());
        assertEquals(CardType.MINION, nertea.getBlueprint().getCardType());
        assertEquals(Culture.WRAITH, nertea.getBlueprint().getCulture());
        assertEquals(Race.NAZGUL, nertea.getBlueprint().getRace());
        assertTrue(scn.hasKeyword(nertea, Keyword.FIERCE));
        assertEquals(9, nertea.getBlueprint().getStrength());
        assertEquals(2, nertea.getBlueprint().getVitality());
        assertEquals(3, nertea.getBlueprint().getSiteNumber());
    }

    @Test
    public void FreepsAddsABurdenToSkirmishNamedRace() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl nertea = scn.GetShadowCard("nertea");

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
        PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
        PhysicalCardImpl legolas = scn.GetFreepsCard("legolas");

        scn.ShadowMoveCardToHand(nertea);

        scn.FreepsMoveCharToTable(aragorn, boromir, arwen, legolas);

        scn.StartGame();

        scn.SetTwilight(10);
        scn.RemoveBurdens(1); //To compensate for the bid

        scn.FreepsPassCurrentPhaseAction();

        scn.ShadowPlayCard(nertea);
        scn.ShadowChoose("14"); // Man


        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        assertEquals(0, scn.GetBurdens());
        assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden to be able to assign Ulaire Nertea to a"));
        scn.FreepsChooseNo();
        assertEquals(0, scn.GetBurdens());
        Boolean exc = false;
        try {
            scn.FreepsAssignToMinions(boromir, nertea);
        }
        catch (DecisionResultInvalidException ex) {
            exc = true;
        }
        assertTrue(exc); // If an exception wasn't thrown, then assigning to a Man was permitted.

        scn.FreepsAssignToMinions(legolas, nertea);
        scn.FreepsResolveSkirmish(legolas);
        scn.PassCurrentPhaseActions();

        //fierce skirmish
        scn.PassCurrentPhaseActions();
        assertEquals(0, scn.GetBurdens());
        assertTrue(scn.FreepsDecisionAvailable("Would you like to add a burden to be able to assign Ulaire Nertea to a"));
        scn.FreepsChooseYes();
        assertEquals(1, scn.GetBurdens());
        scn.FreepsAssignToMinions(boromir, nertea);

    }
}
