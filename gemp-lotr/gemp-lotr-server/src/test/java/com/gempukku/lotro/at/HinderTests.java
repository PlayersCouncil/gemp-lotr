package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Keyword;
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

    @Test
    public void HinderedRingBearerCannotAddBurdens() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("smeagol", "5_29"); //To play, add a burden
                }}
        );

        var frodo = scn.GetRingBearer();

        var smeagol = scn.GetFreepsCard("smeagol");
        scn.MoveCardsToHand(smeagol);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, frodo));
                action.setText("Hinder Frodo");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertEquals(0, scn.GetBurdens());
        assertTrue(scn.FreepsPlayAvailable(smeagol));

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(frodo));

        assertFalse(scn.FreepsPlayAvailable(smeagol));
    }

    @Test
    public void HinderedRingBearerCannotRemoveBurdens() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("sam", "1_311"); //Fellowship ability to remove burdens
                }}
        );

        var frodo = scn.GetRingBearer();

        var sam = scn.GetFreepsCard("sam");
        scn.MoveCompanionsToTable(sam);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, frodo));
                action.setText("Hinder Frodo");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.AddBurdens(2);
        assertEquals(2, scn.GetBurdens());
        assertTrue(scn.FreepsActionAvailable(sam));

        scn.FreepsUseCardAction(sam);

        //ability works and is still available
        assertEquals(1, scn.GetBurdens());
        assertTrue(scn.FreepsActionAvailable(sam));

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(frodo));

        assertEquals(1, scn.GetBurdens());
        assertTrue(scn.FreepsActionAvailable(sam));
        scn.FreepsUseCardAction(sam);
        //Now that the RB is hindered, burdens cannot be removed even after using ability
        assertEquals(1, scn.GetBurdens());
    }

    @Test
    public void HinderedRingBearerCanSpotBurdens() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("stinker", "5_25"); //Strength +1 per burden
                }}
        );

        var frodo = scn.GetRingBearer();

        var stinker = scn.GetShadowCard("stinker");
        scn.MoveMinionsToTable(stinker);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, frodo));
                action.setText("Hinder Frodo");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.AddBurdens(3);
        assertEquals(3, scn.GetBurdens());

        //5 base, +3 for the burdens
        assertEquals(8, scn.GetStrength(stinker));

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(frodo));

        //Number of burdens hasn't changed
        assertEquals(3, scn.GetBurdens());

        //Gollum is still able to see the burdens in spite of the Ring-bearer being hindered
        assertEquals(8, scn.GetStrength(stinker));
    }

    @Test
    public void HinderedRBSuspendsCorruptionCheckWhileHindered() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("elf", "1_53"); //elf
                }},
                null,
                VirtualTableScenario.GaladrielRB,
                null
        );

        var galadriel = scn.GetRingBearer();

        var elf = scn.GetFreepsCard("elf");
        scn.MoveCompanionsToTable(elf);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(galadriel);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, galadriel));
                action.setText("Hinder Galadriel");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.AddBurdens(4);
        assertEquals(4, scn.GetBurdens()); //3 from above

        //Galadriel normally starts with 3 base resistance, and gets +1 for each elf comp you can spot
        // (+1 from herself, +1 from lorien elf)
        //The only reason she is not corrupted right now is because of her own game text
        assertEquals(1, scn.GetResistance(galadriel));

        scn.FreepsUseCardAction(galadriel);
        assertTrue(scn.IsHindered(galadriel));

        assertFalse(scn.GameIsFinished());
    }

    //@Test
    public void CannotBeHinderedModifierPreventsTargetFromBeingHindered() throws DecisionResultInvalidException, CardNotFoundException {

    }



    //@Test
    public void HinderedCompanionsCanBeSpottedAsFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
        //Courtesy of my Hall
    }

    @Test
    public void HinderedCompanionsPermitsUseOfAttachedSpecialAbilitie() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("visitor", "7_126"); //Regroup: Discard this condition to discard a minion and remove (4)
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var visitor = scn.GetFreepsCard("visitor");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn, visitor);

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

        scn.SkipToPhase(Phase.REGROUP);
        assertTrue(scn.FreepsActionAvailable(visitor));
    }

    @Test
    public void HinderedItemGrantsNoModifiersToAttachedCompanion() throws DecisionResultInvalidException, CardNotFoundException {

        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("anduril", "7_79");
                    put("bow", "1_90");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var anduril = scn.GetFreepsCard("anduril");
        var bow = scn.GetFreepsCard("bow");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(bow);
        scn.AttachCardsTo(aragorn, anduril);

        scn.MoveMinionsToTable("runner");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, anduril));
                action.setText("Hinder Anduril");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertFalse(scn.IsHindered(anduril));
        assertTrue(scn.HasKeyword(aragorn, Keyword.DAMAGE));
        //"Aragorn...cannot bear other weapons"
        assertFalse(scn.FreepsPlayAvailable(bow));
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(anduril));
        assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
        assertTrue(scn.FreepsPlayAvailable(bow));
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
    public void HinderedCompanionStillCountsTowardsRuleOf9() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_364");
                    put("legolas", "1_50");
                    put("gimli", "1_13");
                    put("boromir", "3_122");
                    put("sam", "1_311");
                    put("merry", "1_302");
                    put("pippin", "1_306");
                    put("arwen", "1_30");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        var arwen = scn.GetFreepsCard("arwen");

        scn.MoveCompanionsToTable("legolas", "gimli", "boromir", "sam", "merry", "pippin");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(gandalf, arwen);

        scn.HinderCard(aragorn);

        scn.StartGame();

        assertTrue(scn.IsHindered(aragorn));
        assertTrue(scn.FreepsPlayAvailable(gandalf));
        assertTrue(scn.FreepsPlayAvailable(arwen));

        scn.FreepsPlayCard(gandalf);

        //Full 9 companions on the table, can no longer fit arwen, even with Aragorn hindered
        assertFalse(scn.FreepsPlayAvailable(arwen));

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
    public void HinderedCompanionStillCountsTowardsThreatLimit() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("deadman", "10_27");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var deadman = scn.GetFreepsCard("deadman");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(deadman);

        scn.AddThreats(1);

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

        //2 companions on the table, 1 threat, this hasn't hit the threat limit
        assertTrue(scn.FreepsPlayAvailable(deadman)); //"To play, add a threat"
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(aragorn));
        //2 companions still on the table even with 1 hindered, threat limit shouldn't be reached
        assertTrue(scn.FreepsPlayAvailable(deadman));
    }

    @Test
    public void HinderedItemsStillEnforceItemClass() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("anduril", "7_79");
                    put("sword", "1_112");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var anduril = scn.GetFreepsCard("anduril");
        var sword = scn.GetFreepsCard("sword");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn, anduril);
        scn.MoveCardsToHand(sword);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, anduril));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        //Anduril already on Aragorn, Ranger's Sword cannot be played on him
        assertFalse(scn.FreepsPlayAvailable(sword));
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(anduril));
        assertFalse(scn.IsHindered(aragorn));
        //Ranger's Sword still cannot be played even with Anduril hindered
        assertFalse(scn.FreepsPlayAvailable(sword));
    }

    @Test
    public void HinderedItemsDoNotGrantPassiveStatBonuses() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "7_81");
                    put("anduril", "7_79");
                    put("steed", "12_49");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var anduril = scn.GetFreepsCard("anduril");
        var steed = scn.GetFreepsCard("steed");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn, anduril, steed);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, anduril, steed));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();
        scn.RemoveBurdens(1);
        scn.FreepsDeclineOptionalTrigger(); //Knight Aragorn's start of phase action

        assertEquals(11, scn.GetStrength(aragorn)); //base 8 + 2 from anduril +1 from steed
        assertEquals(5, scn.GetVitality(aragorn)); //base 4 + 1 from anduril
        assertEquals(7, scn.GetResistance(aragorn)); //base 7 + 1 from steed

        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(anduril));
        assertTrue(scn.IsHindered(steed));
        assertFalse(scn.IsHindered(aragorn));

        assertEquals(8, scn.GetStrength(aragorn));
        assertEquals(4, scn.GetVitality(aragorn));
        assertEquals(6, scn.GetResistance(aragorn));
    }

    @Test
    public void ItemsCannotBePlayedOntoHinderedCompanions() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("anduril", "7_79");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var anduril = scn.GetFreepsCard("anduril");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(anduril);

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

        assertTrue(scn.FreepsPlayAvailable(anduril));
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(aragorn));
        assertFalse(scn.FreepsPlayAvailable(anduril));
    }

    @Test
    public void ItemsCannotBeTransferredToHinderedCompanions() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("anduril", "7_79");
                    put("arwen", "1_30");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var anduril = scn.GetFreepsCard("anduril");
        var arwen = scn.GetFreepsCard("arwen");
        //Cheating and putting it on arwen for the transfer effect
        scn.MoveCompanionsToTable(arwen, aragorn);
        scn.AttachCardsTo(arwen, anduril);

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

        assertTrue(scn.FreepsActionAvailable("Transfer Andúril"));
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(aragorn));
        assertFalse(scn.FreepsActionAvailable("Transfer Andúril"));
    }

    @Test
    public void BearerCannotBeUsedIfBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("bow", "1_90");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var bow = scn.GetFreepsCard("bow");
        scn.MoveCompanionsToTable(aragorn);
        scn.AttachCardsTo(aragorn, bow);

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
        scn.SkipToPhase(Phase.ARCHERY);

        assertTrue(scn.IsHindered(aragorn));

        //Aragorn is hindered and thus can't be exerted, so his Bow should fizzle
        assertFalse(scn.FreepsActionAvailable(bow));
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
