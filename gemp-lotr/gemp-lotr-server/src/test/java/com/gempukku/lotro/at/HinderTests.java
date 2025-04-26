package com.gempukku.lotro.at;

import com.gempukku.lotro.cards.GenericCardTestHelper;
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCardToHand(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn, sam);
        scn.AddWoundsToChar(aragorn, 1);
        scn.AddWoundsToChar(frodo, 2);
        scn.AddWoundsToChar(sam, 2);

        var wall = scn.GetShadowCard("wall");
        var dunlending = scn.GetShadowCard("dunlending");
        scn.ShadowMoveCharToTable(dunlending);
        scn.ShadowMoveCardToHand(wall);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn, gandalf);
        scn.FreepsMoveCardToHand(flame);

        scn.ShadowMoveCharToTable("runner");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCharToTable("gandalf", "sam", "merry", "gimli");

        var enquea = scn.GetShadowCard("enquea");
        scn.ShadowMoveCharToTable(enquea);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn, boromir, gimli, gandalf);
        scn.FreepsMoveCardToHand(beacons);

        scn.ShadowMoveCharToTable("runner");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("armor", "1_92");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var armor = scn.GetFreepsCard("armor");
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(armor);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.FreepsMoveCharToTable(aragorn, gandalf);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("pathfinder", "1_110");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var pathfinder = scn.GetFreepsCard("pathfinder");
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(pathfinder);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn);
        //These are 4-cost decoys that Grond can target instead
        scn.FreepsMoveCardToSupportArea(barricade, guardian);

        var grond = scn.GetShadowCard("grond");
        scn.ShadowMoveCardToSupportArea(grond);
        scn.AddTokensToCard(grond, 4);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn);
        //These are 0-cost decoys that Grond can target instead
        scn.FreepsMoveCardToSupportArea(kingdom, stairs);

        var grond = scn.GetShadowCard("grond");
        scn.ShadowMoveCardToSupportArea(grond);
        scn.AddTokensToCard(grond, 4);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.FreepsMoveCharToTable(aragorn, gandalf);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, AbstractAtTest.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, AbstractAtTest.P1, "Can't pick aragorn", Filters.minStrength(4)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        var name = card.getCardId();
                    }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.FreepsMoveCharToTable(aragorn, gandalf);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, AbstractAtTest.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, AbstractAtTest.P1, "Can't pick aragorn", Filters.minVitality(4)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) { }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("gandalf", "1_72");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.FreepsMoveCharToTable(aragorn, gandalf);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, AbstractAtTest.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, AbstractAtTest.P1, "Can't pick aragorn", Filters.minResistance(4)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        var name = card.getCardId();
                    }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn, gandalf, gimli);

        //This version of the hinder-izing ability requires that the player choose a character with at least 4 strength.
        //After hindering Aragorn, we will attempt to use it again and see whether he is still selectable.
        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                var action = new ActivateCardAction(frodo, AbstractAtTest.P1);

                //action.setText("Hinder Aragorn");
                action.appendCost(new ChooseActiveCardEffect(null, AbstractAtTest.P1, "Can't pick aragorn", Signet.GANDALF) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        var name = card.getCardId();
                    }
                });
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("lord", "4_219");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        var lord = scn.GetShadowCard("lord");
        scn.ShadowMoveCharToTable(lord);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
    public void HinderedRingBearerCannotSpotBurdens() throws DecisionResultInvalidException, CardNotFoundException {

    }



    //@Test
    public void HinderedCompanionsCanBeSpottedAsFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
        //Courtesy of my Hall
    }

    @Test
    public void HinderedCompanionsCannotUseSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ShadowMoveCharToTable("runner");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
    public void HinderedCompanionsCannotUseModifiers() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "7_364");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "3_38");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
    public void HinderedCompanionsLoseKeywords() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "4_109");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertTrue(scn.hasKeyword(aragorn, Keyword.DEFENDER));
        scn.FreepsUseCardAction(frodo);

        assertTrue(scn.IsHindered(aragorn));
        assertFalse(scn.hasKeyword(aragorn, Keyword.DEFENDER));
    }

    //@Test
    public void HinderedCompanionsDoesNotCountAsHavingOrNotHavingKeyword() throws DecisionResultInvalidException, CardNotFoundException {
        //Remember Your Old Strength
    }

    @Test
    public void HinderedCompanionsCannotBeAssignedToSkirmishByAssignmentAbilities() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("troll", "15_117");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        var troll = scn.GetShadowCard("troll");
        scn.ShadowMoveCharToTable(troll);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        scn.FreepsUseCardAction(frodo);
        assertTrue(scn.IsHindered(aragorn));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowUseCardAction(troll);
        assertEquals(2, scn.GetWoundsOn(troll));
        assertFalse(scn.IsCharAssigned(troll));
        assertFalse(scn.IsCharAssigned(aragorn));

        assertTrue(scn.FreepsDecisionAvailable("Assignment action"));
    }

    @Test
    public void HinderedCompanionsCannotBeAssignedToSkirmishesNormally() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("runner", "1_178");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ShadowMoveCharToTable("runner");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");

                    put("enquea", "1_231");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.ShadowMoveCharToTable("enquea");

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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

        scn.FreepsMoveCharToTable("legolas", "gimli", "boromir", "sam", "merry", "pippin");
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(gandalf, arwen);

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
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("httwc", "3_38");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var httwc = scn.GetFreepsCard("httwc");
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(httwc);

        scn.HinderCard(aragorn);

        scn.StartGame();

        assertTrue(scn.IsHindered(aragorn));
        assertFalse(scn.FreepsPlayAvailable(httwc));
    }

    @Test
    public void HinderedCompanionStillCountsTowardsThreatLimit() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("deadman", "10_27");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var deadman = scn.GetFreepsCard("deadman");
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(deadman);

        scn.AddThreats(1);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(aragorn);
        scn.AttachCardsTo(aragorn, anduril);
        scn.FreepsMoveCardToHand(sword);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, anduril));
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
    public void ItemsCannotBePlayedOntoHinderedCompanions() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("anduril", "7_79");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var anduril = scn.GetFreepsCard("anduril");
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(anduril);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
        var scn = new GenericCardTestHelper(
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
        scn.FreepsMoveCharToTable(arwen, aragorn);
        scn.AttachCardsTo(arwen, anduril);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, aragorn));
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
    public void HinderedSiteDoesNotCountAsControlled() throws DecisionResultInvalidException, CardNotFoundException {

    }

    //@Test
    public void BearerCannotBeUsedIfBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {

    }
}
