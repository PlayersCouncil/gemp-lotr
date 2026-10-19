package com.gempukku.lotro.at;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.CantBeAssignedAgainstModifier;
import com.gempukku.lotro.logic.timing.processes.turn.assign.MaximumAssignment;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The ASSIGN_MINIONS decision carries "maxAssignable", the most minions that can legally be assigned, so the client
 * only warns about unassigned minions when the player has assigned fewer than that.
 *
 * Frodo is the ring-bearer and counts as a companion, so each scenario has Frodo plus the companions named here.
 */
public class MaxAssignableAtTest {

    private static final String MINION = "1_158"; // Uruk-hai Raiding Party; any minion will do

    private static VirtualTableScenario scenario(int extraCompanions, int minions) throws CardNotFoundException, DecisionResultInvalidException {
        String[] companionBlueprints = {"1_89", "1_311", "1_303", "1_306"}; // Aragorn, Sam, Merry, Pippin
        HashMap<String, String> cards = new HashMap<>();
        for (int i = 0; i < extraCompanions; i++)
            cards.put("comp" + i, companionBlueprints[i]);
        for (int i = 0; i < minions; i++)
            cards.put("minion" + i, MINION);

        var scn = new VirtualTableScenario(cards, VirtualTableScenario.FellowshipSites,
                VirtualTableScenario.FOTRFrodo, VirtualTableScenario.RulingRing);
        for (int i = 0; i < extraCompanions; i++)
            scn.MoveCompanionsToTable(scn.GetFreepsCard("comp" + i));
        for (int i = 0; i < minions; i++)
            scn.MoveMinionsToTable(scn.GetShadowCard("minion" + i));
        scn.StartGame();
        return scn;
    }

    private static String maxAssignable(VirtualTableScenario scn) {
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, scn.FreepsGetAwaitingDecision().getDecisionType());
        String[] max = scn.FreepsGetADParam("maxAssignable");
        assertNotNull("the decision should say how many minions can be assigned", max);
        assertEquals(1, max.length);
        return max[0];
    }

    @Test
    public void moreMinionsThanCompanionsCanAssignOnlyAsManyAsThereAreCompanions() throws Exception {
        // Frodo + 3 companions against 6 minions: 4 is the most that can be assigned
        var scn = scenario(3, 6);
        scn.SkipToAssignments();

        assertEquals(6, scn.FreepsGetShadowAssignmentTargetCount());
        assertEquals("4", maxAssignable(scn));
    }

    @Test
    public void defenderAddsCapacity() throws Exception {
        // Frodo + Aragorn, Aragorn defender +1: 3 slots for 4 minions
        var scn = scenario(1, 4);
        var aragorn = scn.GetFreepsCard("comp0");
        scn.ApplyAdHocModifier(new AddKeywordModifier(aragorn, aragorn, null, Keyword.DEFENDER));
        scn.SkipToAssignments();

        assertEquals("3", maxAssignable(scn));
    }

    @Test
    public void defenderCapacityIsNotWastedWhenThereAreFewMinions() throws Exception {
        var scn = scenario(1, 2);
        var aragorn = scn.GetFreepsCard("comp0");
        scn.ApplyAdHocModifier(new AddKeywordModifier(aragorn, aragorn, null, Keyword.DEFENDER, 2));
        scn.SkipToAssignments();

        assertEquals("2", maxAssignable(scn));
    }

    @Test
    public void allMinionsAssignableWhenThereIsRoomForThem() throws Exception {
        var scn = scenario(2, 2);
        scn.SkipToAssignments();

        assertEquals("2", maxAssignable(scn));
    }

    @Test
    public void assignmentRestrictionsLowerTheMaximum() throws Exception {
        // Frodo + Aragorn, 2 minions, but the first minion can't be assigned against anyone: only the other one can go
        var scn = scenario(1, 2);
        var barred = scn.GetShadowCard("minion0");
        scn.ApplyAdHocModifier(new CantBeAssignedAgainstModifier(barred, Side.FREE_PEOPLE, CardType.COMPANION, null, Filters.sameCard(barred)));
        scn.SkipToAssignments();

        assertEquals("1", maxAssignable(scn));
    }

    @Test
    public void restrictionsCanBeWorkedAroundByPairingDifferently() throws Exception {
        // Frodo + Aragorn + Sam, 3 minions; minion0 may not face Frodo or Aragorn, so it must take Sam. Everyone fits.
        var scn = scenario(2, 3);
        var barred = scn.GetShadowCard("minion0");
        scn.ApplyAdHocModifier(new CantBeAssignedAgainstModifier(barred, Side.FREE_PEOPLE,
                Filters.or(scn.GetFreepsCard("comp0"), scn.GetRingBearer()), null, Filters.sameCard(barred)));
        scn.SkipToAssignments();

        assertEquals("3", maxAssignable(scn));
    }

    @Test
    public void shadowPlayerMayStackLeftoverMinions() throws Exception {
        // Free Peoples takes one minion; the Shadow player is not capped by 1 + defender, so both leftovers can go
        var scn = scenario(1, 3);
        scn.SkipToAssignments();
        assertEquals("2", maxAssignable(scn));

        scn.FreepsAssignToMinions(scn.GetFreepsCard("comp0"), scn.GetShadowCard("minion0"));

        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, scn.ShadowGetAwaitingDecision().getDecisionType());
        assertEquals("2", scn.ShadowGetFirstADParam("maxAssignable"));
    }

    @Test
    public void searchGivesUpWithoutAnAnswerWhenOverBudget() throws Exception {
        var scn = scenario(3, 6);
        Collection<PhysicalCard> companions = Filters.filterActive(scn.game(), CardType.COMPANION);
        Collection<PhysicalCard> minions = Filters.filterActive(scn.game(), CardType.MINION);

        assertEquals(Integer.valueOf(4), MaximumAssignment.compute(scn.game(), Side.FREE_PEOPLE, companions, minions));
        assertNull(MaximumAssignment.compute(scn.game(), Side.FREE_PEOPLE, companions, minions, 2));
    }

    @Test
    public void noMinionsAvailableMeansNothingToAssign() throws Exception {
        var scn = scenario(1, 0);
        Collection<PhysicalCard> companions = Filters.filterActive(scn.game(), CardType.COMPANION);

        assertEquals(Integer.valueOf(0), MaximumAssignment.compute(scn.game(), Side.FREE_PEOPLE, companions, java.util.List.of()));
    }
}
