package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_013_Tests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("tidings", "2_13");
                    put("gimli", "1_13");

                    put("deckcard1", "1_13");
                    put("deckcard2", "1_13");
                    put("deckcard3", "1_13");

                }}
        );
    }

    @Test
    public void TidingsofEreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 2
         * Name: Tidings of Erebor
         * Unique: False
         * Side: Free Peoples
         * Culture: Dwarven
         * Twilight Cost: 0
         * Type: Event
         * Subtype: Regroup
         * Game Text: <b>Regroup:</b> Spot a Dwarf to draw 3 cards. Any Shadow player may remove (3) to prevent this.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsCard("tidings");

        assertEquals("Tidings of Erebor", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
        assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.REGROUP));
        assertEquals(0, card.getBlueprint().getTwilightCost());
    }

    // Uncomment any @Test markers below once this is ready to be used
    //@Test
    public void TidingsofEreborTest1() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var card = scn.GetFreepsCard("card");
        scn.FreepsMoveCardToHand(card);

        scn.StartGame();
        scn.FreepsPlayCard(card);

        assertEquals(0, scn.GetTwilight());
    }

    @Test
    public void TidingsAbilityCanBePrevented() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl tidings = scn.GetFreepsCard("tidings");
        PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
        scn.FreepsMoveCharToTable(gimli);
        scn.FreepsMoveCardToHand(tidings);

        scn.StartGame();
        scn.FreepsPassCurrentPhaseAction();

        scn.SkipToPhase(Phase.REGROUP);


        scn.FreepsPlayCard(tidings);
        assertEquals(3, scn.GetTwilight());
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(3, scn.GetFreepsDeckCount());
        scn.ShadowAcceptOptionalTrigger();
        assertEquals(0, scn.GetTwilight());
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(3, scn.GetFreepsDeckCount());

    }



}