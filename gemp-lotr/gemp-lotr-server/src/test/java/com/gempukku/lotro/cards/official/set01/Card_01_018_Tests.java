package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;


public class Card_01_018_Tests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("halls", "1_18");
                    put("gimli", "1_13");

                    put("scard1", "1_178");
                    put("scard2", "1_21");
                    put("scard3", "1_203");

                    put("fcard1", "2_75");
                    put("fcard2", "2_51");
                    put("fcard3", "2_96");

                }}
        );
    }

    @Test
    public void HallsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1
         * Name: Halls of My Home
         * Unique: False
         * Side: Free Peoples
         * Culture: Dwarven
         * Twilight Cost: 1
         * Type: Event
         * Subtype: Fellowship
         * Game Text: Fellowship: Exert a Dwarf to reveal the top 3 cards of any draw deck. You may discard 1 Shadow card revealed.
         * Return the rest in any order.
         */

        //Pre-game setup
        var scn = GetScenario();

        var card = scn.GetFreepsCard("halls");

        assertEquals("Halls of My Home", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
        assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
        assertEquals(1, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void HallsExertsToRevealDiscardAndRearrangeFreepsDeck() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var gimli = scn.GetFreepsCard("gimli");
        var halls = scn.GetFreepsCard("halls");
        scn.MoveCompanionsToTable(gimli);
        scn.MoveCardsToHand(halls);

        var fcard1 = scn.GetFreepsCard("fcard1");
        var fcard2 = scn.GetFreepsCard("fcard2");
        var fcard3 = scn.GetFreepsCard("fcard3");

        var scard1 = scn.GetShadowCard("scard1");
        var scard2 = scn.GetShadowCard("scard2");
        var scard3 = scn.GetShadowCard("scard3");

        scn.StartGame();

        assertTrue(scn.FreepsPlayAvailable(halls));
        assertEquals(0, scn.GetWoundsOn(gimli));

        scn.MoveCardsToTopOfDeck(fcard3, fcard2, fcard1);
        scn.MoveCardsToTopOfDeck(scard3, scard2, scard1);

        scn.FreepsPlayCard(halls);

        assertEquals(1, scn.GetWoundsOn(gimli));
        assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertEquals(2, scn.FreepsGetMultipleChoices().size());
        scn.FreepsChooseOption("your deck");
        List<String> choices = scn.FreepsGetADParamAsList("blueprintId");
        assertTrue(choices.contains(fcard1.getBlueprintId()));
        assertTrue(choices.contains(fcard2.getBlueprintId()));
        assertTrue(choices.contains(fcard3.getBlueprintId()));

        scn.DismissRevealedCards();
        assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
        scn.FreepsChooseYes();
        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));

        choices = scn.FreepsGetADParamAsList("blueprintId");
        assertTrue(choices.contains(fcard1.getBlueprintId()));
        assertTrue(choices.contains(fcard2.getBlueprintId()));
        assertFalse(choices.contains(fcard3.getBlueprintId())); // freeps card, shouldn't be able to be discarded

        scn.FreepsChooseCardBPFromSelection(fcard1);
        assertEquals(Zone.DISCARD, fcard1.getZone()); //discarded shadow card
        choices = scn.FreepsGetADParamAsList("blueprintId");
        assertFalse(choices.contains(fcard1.getBlueprintId()));
        assertTrue(choices.contains(fcard2.getBlueprintId()));
        assertTrue(choices.contains(fcard3.getBlueprintId()));

        scn.FreepsChooseCardBPFromSelection(fcard3);
        assertEquals(fcard2.getBlueprintId(), scn.GetFreepsTopOfDeck().getBlueprintId());

    }


    @Test
    public void HallsExertsToRevealDiscardAndRearrangeShadowDeck() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var gimli = scn.GetFreepsCard("gimli");
        var halls = scn.GetFreepsCard("halls");
        scn.MoveCompanionsToTable(gimli);
        scn.MoveCardsToHand(halls);

        var fcard1 = scn.GetFreepsCard("fcard1");
        var fcard2 = scn.GetFreepsCard("fcard2");
        var fcard3 = scn.GetFreepsCard("fcard3");

        var scard1 = scn.GetShadowCard("scard1");
        var scard2 = scn.GetShadowCard("scard2");
        var scard3 = scn.GetShadowCard("scard3");

        scn.StartGame();

        scn.MoveCardsToTopOfDeck(fcard3, fcard2, fcard1);
        scn.MoveCardsToTopOfDeck(scard3, scard2, scard1);

        assertTrue(scn.FreepsPlayAvailable(halls));
        assertEquals(0, scn.GetWoundsOn(gimli));

        scn.FreepsPlayCard(halls);

        assertEquals(1, scn.GetWoundsOn(gimli));
        assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertEquals(2, scn.FreepsGetMultipleChoices().size());
        scn.FreepsChooseOption("your opponent's deck");
        List<String> choices = scn.FreepsGetADParamAsList("blueprintId");
        assertTrue(choices.contains(scard1.getBlueprintId()));
        assertTrue(choices.contains(scard2.getBlueprintId()));
        assertTrue(choices.contains(scard3.getBlueprintId()));

        scn.DismissRevealedCards();
        assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
        scn.FreepsChooseYes();
        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));

        choices = scn.FreepsGetADParamAsList("blueprintId");
        assertTrue(choices.contains(scard1.getBlueprintId()));
        assertTrue(choices.contains(scard3.getBlueprintId()));
        assertFalse(choices.contains(scard2.getBlueprintId())); // freeps card, shouldn't be able to be discarded

        scn.FreepsChooseCardBPFromSelection(scard1);
        assertEquals(Zone.DISCARD, scard1.getZone()); //discarded shadow card
        choices = scn.FreepsGetADParamAsList("blueprintId");
        assertFalse(choices.contains(scard1.getBlueprintId()));
        assertTrue(choices.contains(scard2.getBlueprintId()));
        assertTrue(choices.contains(scard3.getBlueprintId()));

        scn.FreepsChooseCardBPFromSelection(scard2);
        assertEquals(scard3.getBlueprintId(), scn.GetShadowTopOfDeck().getBlueprintId());

    }
}