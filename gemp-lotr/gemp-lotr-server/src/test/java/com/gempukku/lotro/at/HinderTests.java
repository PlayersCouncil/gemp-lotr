package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.HinderCardsInPlayEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class HinderTests
{
    /**
     * Each of these functions follows the same basic pattern: hack in a special ability on Frodo
     * (the 12-resistance once with no other abilities) which hinders Aragorn by name, then activate
     * the ability and ensure that Aragorn is unable to perform some function that hindered companions
     * should be barred from.
     */
    @Test
    public void HinderingACompanionInPlayFlipsItOver() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertFalse(aragorn.isFlipped());

        scn.FreepsUseCardAction(frodo);

        assertTrue(aragorn.isFlipped());
    }

    @Test
    public void HinderingACompanionInPlayGrantsItTheHinderedStatus() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertFalse(aragorn.isFlipped());
        assertFalse(scn.IsHindered(aragorn));
        assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());

        scn.FreepsUseCardAction(frodo);

        assertTrue(aragorn.isFlipped());
        assertTrue(scn.IsHindered(aragorn));
    }

    //@Test
    public void CannotBeHinderedModifierPreventsTargetFromBeingHindered() throws DecisionResultInvalidException, CardNotFoundException {

    }



    //@Test
    public void HinderedCompanionsCanBeSpottedAsFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
        //Courtesy of my Hall
    }

    @Test
    public void HinderedCompanionsCannotBeAssignedToSkirmishByAssignmentAbilities() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("troll", "15_117"); // Tower Troll
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        var troll = scn.GetShadowCard("troll");
        scn.MoveMinionsToTable(troll);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(aragorn));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.FreepsPass();
        scn.ShadowUseCardAction(troll); // Assignment: exert this minion twice to assign it to a companion (except the Ring-bearer).
        assertEquals(2, scn.GetWoundsOn(troll));
        assertFalse(scn.IsCharAssigned(troll));
        assertFalse(scn.IsCharAssigned(aragorn));

        assertTrue(scn.FreepsDecisionAvailable("Assignment action"));
    }

    @Test
    public void HinderedCompanionsCannotBeAssignedAsBearerByAttachedCards() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("visitor", "7_126"); //Each minion gains this ability: "Assignment: Assign this minion to bearer of Unexpected Visitor"

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var visitor = scn.GetFreepsCard("visitor");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn, visitor);

        var runner = scn.GetShadowCard("runner");
        scn.MoveMinionsToTable(runner);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(aragorn));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.FreepsPass();

        assertTrue(scn.ShadowActionAvailable("Assign this minion to bearer of Unexpected Visitor"));

        scn.ShadowUseCardAction(runner);
        assertFalse(scn.IsCharAssigned(runner));
        assertFalse(scn.IsCharAssigned(aragorn));

        assertTrue(scn.FreepsDecisionAvailable("Assignment action"));
    }

    @Test
    public void HinderedCompanionsCannotBeAssignedToSkirmishesNormally() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        scn.MoveMinionsToTable("runner");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(aragorn));

        scn.SkipToAssignments();
        assertEquals(1, scn.FreepsGetFreepsAssignmentTargetCount());
        assertFalse(scn.FreepsGetFreepsAssignmentTargets().contains(String.valueOf(aragorn.getCardId())));
    }

    //@Test
    public void HinderedCompanionsCannotBeAssignedToFierceSkirmishByAssignmentAbilities() throws DecisionResultInvalidException, CardNotFoundException {
        //Undisciplined
    }

    @Test
    public void HinderedCompanionsCannotBeAssignedToFierceSkirmishesNormally() throws DecisionResultInvalidException, CardNotFoundException {

        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("enquea", "1_231");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        scn.MoveMinionsToTable("enquea");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(aragorn));

        scn.SkipToAssignments();
        scn.FreepsDeclineAssignments();
        scn.ShadowDeclineAssignments();
        scn.PassFierceSkirmishActions();

        assertEquals(1, scn.FreepsGetFreepsAssignmentTargetCount());
        assertFalse(scn.FreepsGetFreepsAssignmentTargets().contains(String.valueOf(aragorn.getCardId())));
    }

    @Test
    public void HinderedCompanionStillCountsTowardsUniqueness() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("httwc", "3_38");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var httwc = scn.GetFreepsCard("httwc");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(httwc);

        scn.HinderCard(aragorn);

        scn.StartGame();

        assertTrue(scn.IsHindered(aragorn));
        assertFalse(scn.FreepsPlayAvailable(httwc));
    }

    @Test
    public void HinderingAnAssignedMinionCancelsThatAssignment() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("visitor", "7_126"); //Each minion gains this ability: "Assignment: Assign this minion to bearer of Unexpected Visitor"

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var visitor = scn.GetFreepsCard("visitor");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn, visitor);

        var runner = scn.GetShadowCard("runner");
        scn.MoveMinionsToTable(runner);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, runner));
                action.setText("Hinder Runner");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.FreepsPass();

        assertTrue(scn.ShadowActionAvailable("Assign this minion to bearer of Unexpected Visitor"));

        scn.ShadowUseCardAction(runner);
        assertTrue(scn.IsCharAssigned(runner));
        assertTrue(scn.IsCharAssigned(aragorn));

        assertTrue(scn.FreepsDecisionAvailable("Assignment action"));
        assertFalse(scn.IsHindered(runner));
        scn.FreepsUseCardAction(frodo); //Hindering Runner
        assertTrue(scn.IsHindered(runner));

        assertFalse(scn.IsCharAssigned(runner));
        assertFalse(scn.IsCharAssigned(aragorn));

        scn.ShadowPass();
        scn.FreepsPass();

        assertTrue(scn.FreepsCanAssign(frodo));
        assertFalse(scn.FreepsCanAssign(runner));
        assertTrue(scn.FreepsCanAssign(aragorn));
    }

    @Test
    public void HinderingAnAssignedCompanionCancelsThatAssignment() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("visitor", "7_126"); //Each minion gains this ability: "Assignment: Assign this minion to bearer of Unexpected Visitor"

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var visitor = scn.GetFreepsCard("visitor");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn, visitor);

        var runner = scn.GetShadowCard("runner");
        scn.MoveMinionsToTable(runner);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.FreepsPass();

        assertTrue(scn.ShadowActionAvailable("Assign this minion to bearer of Unexpected Visitor"));

        scn.ShadowUseCardAction(runner);
        assertTrue(scn.IsCharAssigned(runner));
        assertTrue(scn.IsCharAssigned(aragorn));

        assertTrue(scn.FreepsDecisionAvailable("Assignment action"));
        assertFalse(scn.IsHindered(aragorn));
        scn.FreepsUseCardAction(frodo); //Hindering Aragorn
        assertTrue(scn.IsHindered(aragorn));

        assertFalse(scn.IsCharAssigned(runner));
        assertFalse(scn.IsCharAssigned(aragorn));

        scn.ShadowPass();
        scn.FreepsPass();

        assertTrue(scn.FreepsCanAssign(frodo));
        assertTrue(scn.FreepsCanAssign(runner));
        assertFalse(scn.FreepsCanAssign(aragorn));
    }

    @Test
    public void HinderingASkirmishingMinionEndsThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn);

        var runner = scn.GetShadowCard("runner");
        scn.MoveMinionsToTable(runner);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, runner));
                action.setText("Hinder Runner");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(aragorn, runner);

        assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
        assertFalse(scn.IsCharAssignedAgainst(aragorn, runner));
        assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
        assertFalse(scn.IsHindered(runner));
        scn.FreepsUseCardAction(frodo); //Hindering Runner
        assertTrue(scn.IsHindered(runner));

        assertFalse(scn.IsCharAssignedAgainst(aragorn, runner));
        assertFalse(scn.IsCharSkirmishingAgainst(aragorn, runner));

        assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
    }

    @Test
    public void HinderingASkirmishingCompanionEndsThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn);

        var runner = scn.GetShadowCard("runner");
        scn.MoveMinionsToTable(runner);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(aragorn, runner);

        assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
        assertFalse(scn.IsCharAssignedAgainst(aragorn, runner));
        assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
        assertFalse(scn.IsHindered(aragorn));
        scn.FreepsUseCardAction(frodo); //Hindering Aragorn
        assertTrue(scn.IsHindered(aragorn));

        assertFalse(scn.IsCharAssignedAgainst(aragorn, runner));
        assertFalse(scn.IsCharSkirmishingAgainst(aragorn, runner));

        assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
    }
}
