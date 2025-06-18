package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertNotAttachedTo;
import static org.junit.Assert.*;

public class Card_03_042_ErrataTests
{
    protected VirtualTableScenario GetFOTRScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("horn", "53_42");
                    put("elrond", "1_40");
                    put("boromir", "1_97");
                    put("aragorn", "1_89");

                    put("runner1", "1_178");
                }},
                VirtualTableScenario.FellowshipSites,
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.RulingRing
        );
    }

    protected VirtualTableScenario GetMovieScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("horn", "53_42");
                    put("elrond", "1_40");
                    put("boromir", "1_97");
                    put("aragorn", "1_89");

                    put("runner1", "1_178");
                    put("runner2", "1_178");
                }}
        );
    }

    @Test
    public void HornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *Horn of Boromir
         * Side: Free Peoples
         * Culture: Gondor
         * Twilight Cost: 1
         * Type: Possession
         * Errata Game Text: Bearer must be Boromir.
         * Maneuver: Exert Boromir and discard this possession to spot an ally.  Until the regroup phase,
         * that ally is strength +3 and participates in archery fire and skirmishes.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetFOTRScenario();

        PhysicalCardImpl horn = scn.GetFreepsCard("horn");

        assertTrue(horn.getBlueprint().isUnique());
        assertEquals(1, horn.getBlueprint().getTwilightCost());
    }

    @Test
    public void CanBeBorneByBoromir() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetFOTRScenario();

        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
        PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl horn = scn.GetFreepsCard("horn");

        scn.MoveCompanionsToTable(elrond);

        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(horn);
        scn.MoveCardsToHand(boromir);

        scn.StartGame();

        assertFalse(scn.FreepsPlayAvailable(horn));
        scn.FreepsPlayCard(boromir);
        assertTrue(scn.FreepsPlayAvailable(horn));
        scn.FreepsPlayCard(horn);

        assertAttachedTo(horn, boromir);
    }

    @Test
    public void AbilityExertsAndDiscardsToPermitAllyToSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetFOTRScenario();

        PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
        PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
        PhysicalCardImpl horn = scn.GetFreepsCard("horn");
        PhysicalCardImpl runner1 = scn.GetShadowCard("runner1");

        scn.MoveCompanionsToTable(elrond);
        scn.MoveCompanionsToTable(boromir);
        scn.MoveCardsToHand(horn);

        scn.MoveMinionsToTable(runner1);

        scn.StartGame();

        scn.FreepsPlayCard(horn);

        scn.SkipToPhase(Phase.MANEUVER);
        assertTrue(scn.FreepsActionAvailable(horn));
        scn.FreepsUseCardAction(horn);

        //discards
        assertNotAttachedTo(horn, boromir);
        assertEquals(1, scn.GetWoundsOn(boromir));
        assertEquals(11, scn.GetStrength(elrond));

        scn.SkipToPhase(Phase.ASSIGNMENT);

        scn.PassCurrentPhaseActions();

        scn.FreepsAssignToMinions(elrond, runner1);
        assertTrue(scn.IsCharAssigned(elrond));
        assertEquals(11, scn.GetStrength(elrond));
        
    }


}
