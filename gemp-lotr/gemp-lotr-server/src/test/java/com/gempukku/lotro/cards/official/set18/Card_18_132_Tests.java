package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;


public class Card_18_132_Tests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("flees", "18_132");
                    put("nazgul", "1_232");
                    put("orc", "7_196");
                    put("orc2", "7_192"); //2 cost to ensure that it trips the choice
                    put("cantea", "1_230");
                    put("twk", "1_237");

                    put("follower0", "18_47");
                    put("follower1", "18_17");
                    put("follower2", "15_26");
                    put("follower3", "18_7");

                }}
        );
    }

    @Test
    public void AllLifeFleesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 18
         * Name: All Life Flees
         * Unique: False
         * Side: Shadow
         * Culture: Wraith
         * Twilight Cost: 2
         * Type: Event
         * Subtype: Shadow
         * Game Text: To play, spot a Nazgul.
         *   Remove (X) to choose one: discard a follower from play that has a twilight cost of X, or play from deck a
         *   [RINGWRAITH] minion that has a twilight cost of X.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsCard("flees");

        assertEquals("All Life Flees", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, card.getBlueprint().getSide());
        assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.SHADOW));
        assertEquals(2, card.getBlueprint().getTwilightCost());

    }

    @Test
    public void RequiresNazgulToPlay() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var flees = scn.GetShadowCard("flees");
        var nazgul = scn.GetShadowCard("nazgul");
        scn.ShadowMoveCardToHand(flees, nazgul);

        scn.StartGame();

        scn.SetTwilight(10);
        scn.FreepsPassCurrentPhaseAction();

        assertFalse(scn.ShadowPlayAvailable(flees));
        scn.ShadowPlayCard(nazgul);
        assertTrue(scn.ShadowPlayAvailable(flees));
    }

    @Test
    public void CanDiscardFollowerCostingX() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var flees = scn.GetShadowCard("flees");
        var nazgul = scn.GetShadowCard("nazgul");
        var orc = scn.GetShadowCard("orc");
        var cantea = scn.GetShadowCard("cantea");
        scn.ShadowMoveCardToHand(flees);
        scn.ShadowMoveCharToTable(nazgul);

        var follower0 = scn.GetFreepsCard("follower0");
        var follower1 = scn.GetFreepsCard("follower1");
        var follower2 = scn.GetFreepsCard("follower2");
        var follower3 = scn.GetFreepsCard("follower3");
        scn.FreepsMoveCardToSupportArea(follower0, follower1, follower2, follower3);

        scn.StartGame();

        scn.SetTwilight(10);
        scn.FreepsPassCurrentPhaseAction();

        assertEquals(Zone.SUPPORT, follower2.getZone());
        assertEquals(12, scn.GetTwilight()); // 10 initial, +2 from moving
        scn.ShadowPlayCard(flees);

        assertEquals(10, scn.GetTwilight());
        scn.ShadowChoose("2");
        assertEquals(8, scn.GetTwilight());
        assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
        assertEquals(2, scn.ShadowGetMultipleChoices().size());
        scn.ShadowChooseMultipleChoiceOption("Discard a follower");

        assertEquals(Zone.DISCARD, follower2.getZone());
    }

    @Test
    public void CanDiscardFollowerCosting0() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var flees = scn.GetShadowCard("flees");
        var nazgul = scn.GetShadowCard("nazgul");
        scn.ShadowMoveCardToHand(flees);
        scn.ShadowMoveCharToTable(nazgul);

        var follower0 = scn.GetFreepsCard("follower0");
        var follower1 = scn.GetFreepsCard("follower1");
        var follower2 = scn.GetFreepsCard("follower2");
        var follower3 = scn.GetFreepsCard("follower3");
        scn.FreepsMoveCardToSupportArea(follower0, follower1, follower2, follower3);

        scn.StartGame();

        scn.SetTwilight(10);
        scn.FreepsPassCurrentPhaseAction();

        assertEquals(Zone.SUPPORT, follower0.getZone());
        assertEquals(12, scn.GetTwilight()); // 10 initial, +2 from moving
        scn.ShadowPlayCard(flees);

        assertEquals(10, scn.GetTwilight());
        scn.ShadowChoose("0");
        assertEquals(10, scn.GetTwilight());
        scn.ShadowChoose("0");

        assertEquals(Zone.DISCARD, follower0.getZone());
    }

    @Test
    public void CanPlayWraithMinionFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var flees = scn.GetShadowCard("flees");
        var nazgul = scn.GetShadowCard("nazgul");
        var orc = scn.GetShadowCard("orc");
        var cantea = scn.GetShadowCard("cantea");
        var twk = scn.GetShadowCard("twk");
        scn.ShadowMoveCardToHand(flees);
        scn.ShadowMoveCharToTable(nazgul);

        scn.StartGame();

        scn.SetTwilight(20);
        scn.FreepsPassCurrentPhaseAction();
        assertEquals(22, scn.GetTwilight()); // 20 initial, +2 from moving
        scn.ShadowPlayCard(flees);

        assertEquals(20, scn.GetTwilight());

        assertEquals(5, orc.getBlueprint().getTwilightCost());
        assertEquals(5, cantea.getBlueprint().getTwilightCost());
        assertEquals(Zone.DECK, orc.getZone());
        assertEquals(Zone.DECK, cantea.getZone());

        scn.ShadowChoose("5");

        assertEquals(2, scn.ShadowGetSelectableCount()); // orc + cantea, but not TWK
        scn.ShadowChooseCardBPFromSelection(orc);
        scn.ShadowDismissRevealedCards();

        assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
    }
}