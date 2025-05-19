package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;


public class Card_01_019_Tests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("balin", "1_19");
                    put("gimli", "1_13");

                    put("runner1", "1_178");
                    put("runner2", "1_178");
                    put("scout", "1_191");

                }}
        );
    }

    @Test
    public void HereLiesBalinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1
         * Name: Here Lies Balin, Son of Fundin
         * Unique: False
         * Side: Free Peoples
         * Culture: Dwarven
         * Twilight Cost: 0
         * Type: Event
         * Subtype: Maneuver
         * Game Text: Maneuver: Exert a Dwarf to wound 2 Orcs or to wound 1 Orc twice.
         */

        //Pre-game setup
        var scn = GetScenario();

        var card = scn.GetFreepsCard("balin");

        assertEquals("Here Lies Balin, Son of Fundin", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
        assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
        assertEquals(0, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void BalinCanWound2OrcsOnce() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var gimli = scn.GetFreepsCard("gimli");
        var balin = scn.GetFreepsCard("balin");
        scn.MoveCompanionToTable(gimli);
        scn.MoveCardsToHand(balin);

        var runner1 = scn.GetShadowCard("runner1");
        var runner2 = scn.GetShadowCard("runner2");
        var scout = scn.GetShadowCard("scout");
        scn.MoveMinionsToTable(runner1, runner2);

        scn.StartGame();

        scn.SkipToPhase(Phase.MANEUVER);

        assertEquals(0, scn.GetWoundsOn(gimli));
        assertEquals(Zone.SHADOW_CHARACTERS, runner1.getZone());
        assertEquals(Zone.SHADOW_CHARACTERS, runner2.getZone());

        assertTrue(scn.FreepsPlayAvailable(balin));
        scn.FreepsPlayCard(balin);

        assertTrue(scn.FreepsDecisionAvailable("Choose action"));
        scn.FreepsChooseOption("Wound 2 Orcs");

        assertEquals(1, scn.GetWoundsOn(gimli));
        assertEquals(Zone.DISCARD, runner1.getZone());
        assertEquals(Zone.DISCARD, runner2.getZone());
    }

    @Test
    public void BalinCanWound1OrcTwice() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var gimli = scn.GetFreepsCard("gimli");
        var balin = scn.GetFreepsCard("balin");
        scn.MoveCompanionToTable(gimli);
        scn.MoveCardsToHand(balin);

        var runner1 = scn.GetShadowCard("runner1");
        var runner2 = scn.GetShadowCard("runner2");
        var scout = scn.GetShadowCard("scout");
        scn.MoveMinionsToTable(scout);

        scn.StartGame();

        scn.SkipToPhase(Phase.MANEUVER);

        assertEquals(0, scn.GetWoundsOn(gimli));
        assertEquals(Zone.SHADOW_CHARACTERS, scout.getZone());
        assertEquals(0, scn.GetWoundsOn(scout));

        assertTrue(scn.FreepsPlayAvailable(balin));
        scn.FreepsPlayCard(balin);

        assertEquals(1, scn.GetWoundsOn(gimli));
        assertEquals(Zone.DISCARD, scout.getZone());
    }

    @Test
    public void FreepsMustChooseTwoOrcOrOneOrcToWoundTwice() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var gimli = scn.GetFreepsCard("gimli");
        var balin = scn.GetFreepsCard("balin");
        scn.MoveCompanionToTable(gimli);
        scn.MoveCardsToHand(balin);

        var runner1 = scn.GetShadowCard("runner1");
        var runner2 = scn.GetShadowCard("runner2");
        var scout = scn.GetShadowCard("scout");
        scn.MoveMinionsToTable(runner1, runner2, scout);

        scn.StartGame();

        scn.SkipToPhase(Phase.MANEUVER);

        assertEquals(0, scn.GetWoundsOn(gimli));
        assertEquals(Zone.SHADOW_CHARACTERS, scout.getZone());
        assertEquals(0, scn.GetWoundsOn(scout));

        assertTrue(scn.FreepsPlayAvailable(balin));
        scn.FreepsPlayCard(balin);

        assertTrue(scn.FreepsDecisionAvailable("Choose action"));
        assertEquals(2, scn.FreepsGetMultipleChoices().size());
    }

    @Test
    public void OneOrcWith1VitalityCanBeHitByHereLiesBalin() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var gimli = scn.GetFreepsCard("gimli");
        var balin = scn.GetFreepsCard("balin");
        scn.MoveCompanionToTable(gimli);
        scn.MoveCardsToHand(balin);

        var runner1 = scn.GetShadowCard("runner1");
        var runner2 = scn.GetShadowCard("runner2");
        var scout = scn.GetShadowCard("scout");
        scn.MoveMinionsToTable(runner1);

        scn.StartGame();

        scn.SkipToPhase(Phase.MANEUVER);

        assertEquals(0, scn.GetWoundsOn(gimli));
        assertEquals(Zone.SHADOW_CHARACTERS, runner1.getZone());
        assertEquals(0, scn.GetWoundsOn(runner1));

        assertTrue(scn.FreepsPlayAvailable(balin));
        scn.FreepsPlayCard(balin);

        //https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.1#effect
        //"If the effect of a card or special ability requires you to choose one of two different actions,
        // you must choose an action that you are fully capable of performing (if possible)."
        // As a result, if you can neither "wound 2 orcs" or "wound 1 orc twice", none of the choices are possible.
        // You then back up and choose the option you are most capable of performing (I guess)

        assertEquals(Zone.DISCARD, runner1.getZone());
    }
}