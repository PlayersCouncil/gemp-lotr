package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_001_Tests
{

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("gimli", "1_13");
                    put("farin", "1_11");
                    put("hosp", "101_1");
                    put("handaxe1", "2_10");
                    put("handaxe2", "2_10");
                    put("handaxe3", "2_10");

                    put("troop", "1_143");
                }},
                VirtualTableScenario.FellowshipSites,
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.RulingRing
        );
    }


    @Test
    public void HospitalityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: VSet1, VPack1
         * Title: *Hospitality of the Dwarves
         * Side: Free Peoples
         * Culture: Dwarven
         * Twilight Cost: 1
         * Type: Condition
         * Subtype: Support Area
         * Game Text: Maneuver: If the fellowship is at any sites 4 through 8, make each Dwarf lose all damage bonuses
         * until the regroup phase, and each Dwarf bearing 2 or more items takes no more than 1 wound per phase until the
         * regroup phase.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl hosp = scn.GetFreepsCard("hosp");

        assertFalse(hosp.getBlueprint().isUnique());
        assertEquals(1, hosp.getBlueprint().getTwilightCost());
        assertEquals(CardType.CONDITION, hosp.getBlueprint().getCardType());
        assertEquals(Culture.DWARVEN, hosp.getBlueprint().getCulture());
        assertTrue(scn.HasKeyword(hosp, Keyword.SUPPORT_AREA));
    }

    @Test
    public void AbilityOnlyActivatesAtSites4To8() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
        PhysicalCardImpl hosp = scn.GetFreepsCard("hosp");

        PhysicalCardImpl handaxe1 = scn.GetFreepsCard("handaxe1");
        PhysicalCardImpl handaxe2 = scn.GetFreepsCard("handaxe2");

        scn.MoveCompanionToTable(gimli);
        scn.MoveCardsToSupportArea(hosp);
        scn.MoveCardsToHand(handaxe1);
        scn.MoveCardsToHand(handaxe2);

        PhysicalCardImpl troop = scn.GetShadowCard("troop");
        scn.MoveMinionsToTable(troop);

        //Max out the move limit so we don't have to juggle play back and forth
        scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

        scn.StartGame();

        // Putting two hand axes on Gimli
        scn.FreepsPlayCard(handaxe1);
        scn.FreepsPlayCard(handaxe2);

        //Site 2
        scn.SkipToPhase(Phase.MANEUVER);
        assertFalse(scn.FreepsActionAvailable(hosp));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.PassCurrentPhaseActions(); // skips the assignment on both sides

        scn.PassCurrentPhaseActions(); //in regroup
        scn.FreepsChooseToMove();

        //Site 3
        scn.SkipToPhase(Phase.MANEUVER);
        assertFalse(scn.FreepsActionAvailable(hosp));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.PassCurrentPhaseActions(); // skips the assignment on both sides

        scn.PassCurrentPhaseActions(); //in regroup
        scn.ShadowPassCurrentPhaseAction(); //reconcile
        scn.FreepsChooseToMove();

        //Site 4
        scn.SkipToPhase(Phase.MANEUVER);
        assertTrue(scn.FreepsActionAvailable(hosp));
        scn.FreepsUseCardAction(hosp);
        assertFalse(scn.HasKeyword(gimli, Keyword.DAMAGE));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(gimli, troop);
        scn.FreepsResolveSkirmish(gimli);
        scn.PassCurrentPhaseActions(); // passes skirmish actions

        assertEquals(1, scn.GetWoundsOn(gimli));

        scn.PassCurrentPhaseActions(); //in regroup
        scn.ShadowPassCurrentPhaseAction(); //reconcile
        scn.FreepsChooseToMove();

        //Site 5
        scn.SkipToPhase(Phase.MANEUVER);
        assertTrue(scn.FreepsActionAvailable(hosp));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(gimli, troop);
        scn.FreepsResolveSkirmish(gimli);
        scn.PassCurrentPhaseActions(); // passes skirmish actions

        //Without the wound protection, gimli dies with 2 wounds this round
        assertEquals(Zone.DEAD, gimli.getZone());

        scn.PassCurrentPhaseActions(); //in regroup
        scn.ShadowPassCurrentPhaseAction(); //reconcile
        scn.FreepsChooseToMove();

        //Site 6
        scn.SkipToPhase(Phase.MANEUVER);
        assertTrue(scn.FreepsActionAvailable(hosp));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.PassCurrentPhaseActions(); // skips the assignment on both sides

        scn.PassCurrentPhaseActions(); //in regroup
        scn.ShadowPassCurrentPhaseAction(); //reconcile
        scn.FreepsChooseToMove();

        //Site 7
        scn.SkipToPhase(Phase.MANEUVER);
        assertTrue(scn.FreepsActionAvailable(hosp));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.PassCurrentPhaseActions(); // skips the assignment on both sides

        scn.PassCurrentPhaseActions(); //in regroup
        scn.ShadowPassCurrentPhaseAction(); //reconcile
        scn.FreepsChooseToMove();

        //Site 8
        scn.SkipToPhase(Phase.MANEUVER);
        assertTrue(scn.FreepsActionAvailable(hosp));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.PassCurrentPhaseActions(); // skips the assignment on both sides

        scn.PassCurrentPhaseActions(); //in regroup
        scn.ShadowPassCurrentPhaseAction(); //reconcile
        scn.FreepsChooseToMove();

        //Site 5
        scn.SkipToPhase(Phase.MANEUVER);
        assertFalse(scn.FreepsActionAvailable(hosp));
    }
}
