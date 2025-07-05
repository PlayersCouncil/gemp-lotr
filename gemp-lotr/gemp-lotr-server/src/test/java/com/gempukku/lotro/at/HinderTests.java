package com.gempukku.lotro.at;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
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

    //TODO: Setting this aside until the HinderEffect has been split into hand/stacked/void versions
    //@Test
    public void HinderingACompanionInHandGrantsItTheHinderedStatus() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCardsToHand(aragorn);

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
        assertEquals(Zone.HAND, aragorn.getZone());

        scn.FreepsUseCardAction(frodo);

        assertTrue(aragorn.isFlipped());
        assertTrue(scn.IsHindered(aragorn));
    }

    //@Test
    public void HinderingACompanionInStackGrantsItTheHinderedStatus() throws DecisionResultInvalidException, CardNotFoundException {

    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByCardType() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("sam", "1_311");

                    put("wall", "13_82");
                    put("dunlending", "13_85");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var sam = scn.GetFreepsCard("sam");
        scn.MoveCompanionsToTable(aragorn, sam);
        scn.AddWoundsToChar(aragorn, 1);
        scn.AddWoundsToChar(frodo, 2);
        scn.AddWoundsToChar(sam, 2);

        var wall = scn.GetShadowCard("wall");
        var dunlending = scn.GetShadowCard("dunlending");
        scn.MoveMinionsToTable(dunlending);
        scn.MoveCardsToHand(wall);

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

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(frodo, dunlending);
        scn.FreepsPassCurrentPhaseAction();

        assertEquals(8, scn.GetStrength(dunlending));
        scn.ShadowPlayCard(wall); //"Spot a companion to make a [Men] minion strength +1 for each wound on that companion."

        //Aragorn should not be available as a choice, but Sam and Frodo are.
        assertTrue(scn.ShadowHasCardChoiceAvailable(frodo));
        assertTrue(scn.ShadowHasCardChoiceAvailable(sam));
        assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.ShadowGetCardChoiceCount());

    }

    @Test
    public void HinderedCompanionsCannotBeTargeted() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_364");
                    put("flame", "2_28");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        var flame = scn.GetFreepsCard("flame");
        scn.MoveCompanionsToTable(aragorn, gandalf);
        scn.MoveCardsToHand(flame);

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

        scn.SkipToPhase(Phase.MANEUVER);
        scn.FreepsPlayCard(flame); //"Spot Gandalf to make a companion defender +1 until the regroup phase"
        scn.ShadowChooseNo(); //"Any Shadow player may remove (3) to prevent this."

        //Aragorn should not be available as a choice, but Gandalf and Frodo are.
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
        assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.FreepsGetCardChoiceCount());
    }

    @Test
    public void HinderedCompanionsCannotBeCounted() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_364");
                    put("sam", "1_311");
                    put("merry", "1_302");
                    put("gimli", "1_13");

                    put("enquea", "1_231");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCompanionsToTable("gandalf", "sam", "merry", "gimli");

        var enquea = scn.GetShadowCard("enquea");
        scn.MoveMinionsToTable(enquea);

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

        scn.SkipToPhase(Phase.MANEUVER);
        scn.FreepsPassCurrentPhaseAction();

        assertEquals(1, scn.GetBurdens());
        //Frodo + Sam + Merry + Gandalf + Gimli = 5.  The hindered Aragorn should not count.
        assertFalse(scn.ShadowActionAvailable(enquea)); //"Spot 6 companions (or 5 burdens) and exert Úlairë Enquëa to wound a companion (except the Ring-bearer). "
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByCulture() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("boromir", "3_122");
                    put("gandalf", "1_364");
                    put("gimli", "1_13");
                    put("beacons", "7_43");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var boromir = scn.GetFreepsCard("boromir");
        var gimli = scn.GetFreepsCard("gimli");
        var gandalf = scn.GetFreepsCard("gandalf");
        var beacons = scn.GetFreepsCard("beacons");
        scn.MoveCompanionsToTable(aragorn, boromir, gimli, gandalf);
        scn.MoveCardsToHand(beacons);

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

        scn.SkipToPhase(Phase.MANEUVER);
        scn.FreepsPlayCard(beacons); //"Exert Gandalf [twice] to make all unbound companions of one culture strength +3 until the regroup phase"

        //Aragorn should not be available as a choice, but Boromir and Gimli are.
        // (Gandalf is disallowed by the card, Frodo is not unbound)
        assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
        assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.FreepsGetCardChoiceCount());

        scn.FreepsChooseCard(boromir);
        //Aragorn should not be pumped but boromir should.
        assertEquals(8, scn.GetStrength(aragorn));
        assertEquals(10, scn.GetStrength(boromir));
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByRace() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("armor", "1_92");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var armor = scn.GetFreepsCard("armor");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(armor);

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

        assertTrue(scn.FreepsPlayAvailable(armor));
        scn.FreepsUseCardAction(frodo);
        assertFalse(scn.FreepsPlayAvailable(armor));
    }

    @Test
    public void HinderedCompanionsCannotBeCountedByRace() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.MoveCompanionsToTable(aragorn, gandalf);

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

        assertEquals(8, scn.GetStrength(gandalf)); //"Gandalf is strength +1 for each:...Hobbit, Dwarf, Elf, and Man
        scn.FreepsUseCardAction(frodo);
        assertEquals(7, scn.GetStrength(gandalf));
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByKeyword() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("pathfinder", "1_110");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var pathfinder = scn.GetFreepsCard("pathfinder");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(pathfinder);

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

        assertTrue(scn.FreepsPlayAvailable(pathfinder)); //"Fellowship: to play, spot a ranger..."
        scn.FreepsUseCardAction(frodo);
        assertFalse(scn.FreepsPlayAvailable(pathfinder));
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("barricade", "15_58");
                    put("guardian", "12_50");

                    put("grond", "8_103");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var barricade = scn.GetFreepsCard("barricade");
        var guardian = scn.GetFreepsCard("guardian");
        scn.MoveCompanionsToTable(aragorn);
        //These are 4-cost decoys that Grond can target instead
        scn.MoveCardsToSupportArea(barricade, guardian);

        var grond = scn.GetShadowCard("grond");
        scn.MoveCardsToSupportArea(grond);
        scn.AddTokensToCard(grond, 4);

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

        scn.SkipToPhase(Phase.REGROUP);
        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowUseCardAction(grond);
        scn.ShadowChoose("4");

        //Aragorn should not be available as a choice, but Barricade and Guardian are.
        assertTrue(scn.ShadowHasCardChoiceAvailable(barricade));
        assertTrue(scn.ShadowHasCardChoiceAvailable(guardian));
        assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.ShadowGetCardChoiceCount());
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedAs0TwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("kingdom", "1_16");
                    put("stairs", "1_24");

                    put("grond", "8_103");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var kingdom = scn.GetFreepsCard("kingdom");
        var stairs = scn.GetFreepsCard("stairs");
        scn.MoveCompanionsToTable(aragorn);
        //These are 0-cost decoys that Grond can target instead
        scn.MoveCardsToSupportArea(kingdom, stairs);

        var grond = scn.GetShadowCard("grond");
        scn.MoveCardsToSupportArea(grond);
        scn.AddTokensToCard(grond, 4);

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

        scn.SkipToPhase(Phase.REGROUP);
        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowUseCardAction(grond);
        scn.ShadowChoose("0");

        //Aragorn should not be available as a choice, but Barricade and Guardian are.
        assertTrue(scn.ShadowHasCardChoiceAvailable(kingdom));
        assertTrue(scn.ShadowHasCardChoiceAvailable(stairs));
        assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.ShadowGetCardChoiceCount());
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByStrength() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.MoveCompanionsToTable(aragorn, gandalf);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, scn.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, scn.P1, "Can't pick aragorn", Filters.minStrength(4)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        var name = card.getCardId();
                    }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertTrue(scn.FreepsActionAvailable(frodo));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(3, scn.FreepsGetCardChoiceCount());
        scn.FreepsChooseCard(frodo);

        assertTrue(scn.IsHindered(aragorn));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.FreepsGetCardChoiceCount());
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByVitality() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.MoveCompanionsToTable(aragorn, gandalf);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, scn.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, scn.P1, "Can't pick aragorn", Filters.minVitality(4)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) { }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertTrue(scn.FreepsActionAvailable(frodo));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(3, scn.FreepsGetCardChoiceCount());
        scn.FreepsChooseCard(frodo);

        assertTrue(scn.IsHindered(aragorn));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.FreepsGetCardChoiceCount());
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedByResistance() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.MoveCompanionsToTable(aragorn, gandalf);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, scn.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, scn.P1, "Can't pick aragorn", Filters.minResistance(4)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        var name = card.getCardId();
                    }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertTrue(scn.FreepsActionAvailable(frodo));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(3, scn.FreepsGetCardChoiceCount());
        scn.FreepsChooseCard(frodo);

        assertTrue(scn.IsHindered(aragorn));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.FreepsGetCardChoiceCount());
    }

    @Test
    public void HinderedCompanionsCannotBeSpottedBySignet() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "7_36");
                    put("gimli", "1_13");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        var gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(aragorn, gandalf, gimli);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, scn.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, scn.P1, "Can't pick aragorn", Signet.GANDALF) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        var name = card.getCardId();
                    }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertTrue(scn.FreepsActionAvailable(frodo));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(3, scn.FreepsGetCardChoiceCount());
        scn.FreepsChooseCard(gandalf);

        assertTrue(scn.IsHindered(aragorn));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
        assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
        assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
        assertEquals(2, scn.FreepsGetCardChoiceCount());
    }

    @Test
    public void HinderedCompanionsCannotBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("lord", "4_219");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        var lord = scn.GetShadowCard("lord");
        scn.MoveMinionsToTable(lord);

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

        scn.SkipToPhase(Phase.ARCHERY);
        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowUseCardAction(lord); //"Archery: Exert Desert Lord to exert a companion (except the Ring-bearer)..."

        assertEquals(0, scn.GetWoundsOn(aragorn));

    }

    //@Test
    public void HinderedCompanionsCannotBeExhausted() throws DecisionResultInvalidException, CardNotFoundException {

    }

    //@Test
    public void HinderedCompanionsCannotBeWounded() throws DecisionResultInvalidException, CardNotFoundException {

    }

    //@Test
    public void HinderedCompanionsDoNotCountAsWounded() throws DecisionResultInvalidException, CardNotFoundException {
        //Corsair champion
    }

    //@Test
    public void HinderedCompanionsDoNotCountAsExhausted() throws DecisionResultInvalidException, CardNotFoundException {

    }

    //@Test
    public void HinderedCompanionsCannotBeHealed() throws DecisionResultInvalidException, CardNotFoundException {

    }

    //@Test
    public void HinderedRingBearerCannotAddBurdens() throws DecisionResultInvalidException, CardNotFoundException {

    }

    //@Test
    public void HinderedRingBearerCannotRemoveBurdens() throws DecisionResultInvalidException, CardNotFoundException {

    }

    //@Test
    public void HinderedRingBearerCanSpotBurdens() throws DecisionResultInvalidException, CardNotFoundException {

    }



    //@Test
    public void HinderedCompanionsCanBeSpottedAsFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
        //Courtesy of my Hall
    }

    @Test
    public void HinderedCompanionsCannotUseSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
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

        scn.SkipToPhase(Phase.MANEUVER);
        assertFalse(scn.FreepsActionAvailable(aragorn)); //"Maneuver: Exert Aragorn to make him defender +1"
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
    public void HinderedCompanionsCannotUseModifiers() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "7_364");
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
        scn.AddThreats(3);

        assertEquals(10, scn.GetStrength(aragorn));
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(aragorn));
        assertEquals(7, scn.GetStrength(aragorn));
    }

    @Test
    public void HinderedCompanionsCannotUseTriggers() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "3_38");
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
        scn.AddThreats(3);

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(aragorn));

        scn.FreepsPassCurrentPhaseAction();
        assertFalse(scn.FreepsHasOptionalTriggerAvailable());
    }

    @Test
    public void HinderedCompanionsLoseKeywordsFromModifiers() throws DecisionResultInvalidException, CardNotFoundException {
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

        scn.SkipToPhase(Phase.MANEUVER);

        assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
        scn.FreepsUseCardAction(aragorn);
        assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
        scn.ShadowPass();
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(aragorn));
        assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
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

    //@Test
    public void HinderedCompanionsDoesNotCountAsHavingOrNotHavingKeyword() throws DecisionResultInvalidException, CardNotFoundException {
        //Remember Your Old Strength
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
        scn.FreepsPassCurrentPhaseAction();
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
        scn.FreepsPassCurrentPhaseAction();

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
        assertFalse(scn.FreepsGetFreepsAssignmentTargets().contains(aragorn.getCardId()));
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
        assertFalse(scn.FreepsGetFreepsAssignmentTargets().contains(aragorn.getCardId()));
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

    //@Test
    public void BearerCannotBeUsedIfBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {

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
        scn.FreepsPassCurrentPhaseAction();

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
        scn.FreepsPassCurrentPhaseAction();

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

    //@Test
    public void HinderingACompanionMidAbilityAlsoCancelsTheAbility() throws DecisionResultInvalidException, CardNotFoundException {

    }


}
