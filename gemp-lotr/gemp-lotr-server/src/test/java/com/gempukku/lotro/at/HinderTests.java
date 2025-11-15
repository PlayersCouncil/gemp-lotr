package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
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

    @Test
    public void CannotSpotWoundsOnHinderedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                     put("whelp", "7_202");
                }}
        );

        var frodo = scn.GetRingBearer();

        var whelp = scn.GetShadowCard("whelp");
        scn.MoveMinionsToTable(whelp);

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

        //Whelp is base 3 strength when there is no wound on the RB
        assertEquals(3, scn.GetStrength(whelp));

        //Whelp jumps to 9 strength when there is a wound on the RB
        scn.AddWoundsToChar(frodo, 1);
        assertEquals(9, scn.GetStrength(whelp));

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(frodo));

        //A hindered character cannot have its wounds spotted, so Whelp goes back down to 3
        assertEquals(3, scn.GetStrength(whelp));
    }

    //@Test
    public void CannotHealHinderedCompanion() throws DecisionResultInvalidException, CardNotFoundException {

    }

    @Test
    public void CannotSpotCultureTokenOnHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("ships", "8_65");
                    put("wind", "13_86"); //While you can spot 5 or more culture tokens, the minion archery total is +2
                }}
        );

        var frodo = scn.GetRingBearer();

        var ships = scn.GetShadowCard("ships");
        var wind = scn.GetShadowCard("wind");
        scn.MoveCardsToSupportArea(ships);
        scn.MoveMinionsToTable(wind);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, ships));
                action.setText("Hinder Ships of Great Draught");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertEquals(0, scn.GetShadowArcheryTotal());

        //With 5 spottable culture tokens, Desert Wind makes the archery total +2
        scn.AddTokensToCard(ships, 5);
        assertEquals(2, scn.GetShadowArcheryTotal());

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(ships));

        //A hindered condition cannot have its culture tokens spotted, so the archery total goes back down
        assertEquals(0, scn.GetShadowArcheryTotal());
    }

    @Test
    public void CannotReinforceCultureTokenOnHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("brego", "13_63"); //reinforces a [gondor] token on play
                    put("leaders", "7_112");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var brego = scn.GetFreepsCard("brego");
        var leaders = scn.GetFreepsCard("leaders");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToSupportArea(leaders);
        scn.MoveCardsToHand(brego);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, leaders));
                action.setText("Hinder Noble Leaders");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.AddTokensToCard(leaders, 1);
        assertEquals(1, scn.GetCultureTokensOn(leaders));

        assertTrue(scn.FreepsPlayAvailable(brego));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(leaders));

        scn.FreepsPlayCard(brego);

        //Accept the optional trigger
        scn.FreepsAcceptOptionalTrigger();
        assertTrue(scn.AwaitingFellowshipPhaseActions());
        //Brego failed to reinforce anything because the only token was on a hindered card
        assertEquals(1, scn.GetCultureTokensOn(leaders));
    }

    @Test
    public void CannotAddCultureTokenOnHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("knowledge", "7_104"); //Adds a [gondor] token (not reinforce)
                    put("leaders", "7_112");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var knowledge = scn.GetFreepsCard("knowledge");
        var leaders = scn.GetFreepsCard("leaders");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToSupportArea(leaders);
        scn.MoveCardsToHand(knowledge);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, leaders));
                action.setText("Hinder Noble Leaders");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.AddTokensToCard(leaders, 1);
        assertEquals(1, scn.GetCultureTokensOn(leaders));

        assertTrue(scn.FreepsPlayAvailable(knowledge));
        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(leaders));

        scn.FreepsPlayCard(knowledge);

        scn.DismissRevealedCards();

        assertTrue(scn.AwaitingFellowshipPhaseActions());
        //Hidden Knowledge failed to add tokens to anything because the only token was on a hindered card
        assertEquals(1, scn.GetCultureTokensOn(leaders));
    }

    @Test
    public void CannotRemoveCultureTokenOnHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("brego", "13_63"); //maneuver action removes 2 [gondor] tokens to wound a minion
                    put("leaders", "7_112");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var brego = scn.GetFreepsCard("brego");
        var leaders = scn.GetFreepsCard("leaders");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToSupportArea(leaders);
        scn.AttachCardsTo(aragorn, brego);

        var runner = scn.GetShadowCard("runner");
        scn.MoveMinionsToTable(runner);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, leaders));
                action.setText("Hinder Noble Leaders");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.AddTokensToCard(leaders, 2);
        assertEquals(2, scn.GetCultureTokensOn(leaders));

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(leaders));

        scn.SkipToPhase(Phase.MANEUVER);

        //Brego cannot be used because the only 2 gondor tokens are on a hindered card
        assertFalse(scn.FreepsActionAvailable(brego));
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

        assertEquals(1, scn.GetBurdens()); //1 from the bid
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

        scn.AddBurdens(1);
        assertEquals(2, scn.GetBurdens()); //1 from the bid, +1 above
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

        scn.AddBurdens(2);
        assertEquals(3, scn.GetBurdens()); //2 from above, +1 from the bid

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

        scn.AddBurdens(3);
        assertEquals(4, scn.GetBurdens()); //3 from above, +1 from the bid

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
