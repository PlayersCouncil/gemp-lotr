package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_013_Tests
{
    // Every card a test does not place lies in the Free Peoples draw deck: the three spare Gimlis, and Grimir,
    // Lord of Moria and Gimli where they are not in play.
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("tidings", "2_13");
                    put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a skirmish ability only
                    put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, home site 3; a fellowship ability only
                    // Lord of Moria: a [dwarven] condition in the support area, and no Dwarf; "Each Dwarf is damage +1"
                    put("lord", "1_21");

                    put("deckcard1", "1_13");
                    put("deckcard2", "1_13");
                    put("deckcard3", "1_13");
                }},
                VirtualTableScenario.FellowshipSites,
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.RulingRing
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
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
        assertEquals(0, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void SpotsADwarfCompanionToDrawThreeCardsWhenTheShadowPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var tidings = scn.GetFreepsCard("tidings");
        var gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(gimli);
        scn.MoveCardsToHand(tidings);

        scn.StartGame();
        // No minion is played, so the turn goes from the move straight to the regroup phase.
        scn.SkipToPhase(Phase.REGROUP);
        scn.SetTwilight(3);

        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
        assertTrue(scn.FreepsPlayAvailable(tidings));
        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(5, scn.GetFreepsDeckCount());
        int shadowHand = scn.GetShadowHandCount();
        int shadowDeck = scn.GetShadowDeckCount();

        scn.FreepsPlayCard(tidings);
        assertTrue(scn.ShadowDecisionAvailable("prevent"));
        scn.ShadowChooseNo();

        // The event has left the hand and three cards have come into it.
        assertInZone(Zone.DISCARD, tidings);
        assertEquals(3, scn.GetFreepsHandCount());
        assertEquals(2, scn.GetFreepsDeckCount());
        // The event costs nothing, and nothing was removed.
        assertEquals(3, scn.GetTwilight());
        assertEquals(shadowHand, scn.GetShadowHandCount());
        assertEquals(shadowDeck, scn.GetShadowDeckCount());
        assertEquals(0, scn.GetWoundsOn(gimli));
        assertTrue(scn.AwaitingShadowRegroupPhaseActions());
    }

    @Test
    public void TidingsAbilityCanBePrevented() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var tidings = scn.GetFreepsCard("tidings");
        var gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(gimli);
        scn.MoveCardsToHand(tidings);

        scn.StartGame();
        scn.SkipToPhase(Phase.REGROUP);
        scn.SetTwilight(4);

        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(5, scn.GetFreepsDeckCount());

        scn.FreepsPlayCard(tidings);
        assertEquals(4, scn.GetTwilight());
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(5, scn.GetFreepsDeckCount());
        assertTrue(scn.ShadowDecisionAvailable("prevent"));

        scn.ShadowChooseYes();
        // (3) removed, and nothing drawn; the event was played all the same.
        assertEquals(1, scn.GetTwilight());
        assertEquals(0, scn.GetFreepsHandCount());
        assertEquals(5, scn.GetFreepsDeckCount());
        assertInZone(Zone.DISCARD, tidings);
        assertTrue(scn.AwaitingShadowRegroupPhaseActions());
    }

    @Test
    public void TheShadowPlayerIsNotAskedWithFewerThanThreeTwilightTokens() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var tidings = scn.GetFreepsCard("tidings");
        var gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(gimli);
        scn.MoveCardsToHand(tidings);

        scn.StartGame();
        scn.SkipToPhase(Phase.REGROUP);
        scn.SetTwilight(2);

        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(5, scn.GetFreepsDeckCount());

        scn.FreepsPlayCard(tidings);

        assertFalse(scn.ShadowDecisionAvailable("prevent"));
        assertTrue(scn.AwaitingShadowRegroupPhaseActions());
        assertEquals(3, scn.GetFreepsHandCount());
        assertEquals(2, scn.GetFreepsDeckCount());
        assertEquals(2, scn.GetTwilight());
        assertInZone(Zone.DISCARD, tidings);
    }

    @Test
    public void TheShadowPlayerIsNotAskedWhenThereIsNoCardToDraw() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var tidings = scn.GetFreepsCard("tidings");
        var gimli = scn.GetFreepsCard("gimli");
        scn.MoveCompanionsToTable(gimli);
        scn.MoveCardsToHand(tidings);

        scn.StartGame();
        scn.SkipToPhase(Phase.REGROUP);
        // The draw deck has run out.
        scn.MoveCardsToFreepsDiscard("deckcard1", "deckcard2", "deckcard3", "grimir", "lord");
        scn.SetTwilight(3);

        assertEquals(0, scn.GetFreepsDeckCount());
        assertEquals(1, scn.GetFreepsHandCount());
        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
        // An event may be played though it will do nothing.
        assertTrue(scn.FreepsPlayAvailable(tidings));

        scn.FreepsPlayCard(tidings);

        // Nothing will be drawn, so there is nothing to prevent and nothing to pay for.
        assertFalse(scn.ShadowDecisionAvailable("prevent"));
        assertTrue(scn.AwaitingShadowRegroupPhaseActions());
        assertEquals(3, scn.GetTwilight());
        assertEquals(0, scn.GetFreepsHandCount());
        assertInZone(Zone.DISCARD, tidings);
    }

    @Test
    public void ADwarvenCardThatIsNoDwarfIsNotADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var tidings = scn.GetFreepsCard("tidings");
        var lord = scn.GetFreepsCard("lord");
        scn.MoveCardsToSupportArea(lord);
        scn.MoveCardsToHand(tidings);

        scn.StartGame();
        scn.SkipToPhase(Phase.REGROUP);

        // Frodo and a [dwarven] condition: no Dwarf.
        assertInZone(Zone.SUPPORT, lord);
        assertInZone(Zone.HAND, tidings);
        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
        assertFalse(scn.FreepsPlayAvailable(tidings));
    }

    @Test
    public void ADwarfAllyIsADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var tidings = scn.GetFreepsCard("tidings");
        var grimir = scn.GetFreepsCard("grimir");
        scn.MoveAlliesToTable(grimir);
        scn.MoveCardsToHand(tidings);

        scn.StartGame();
        scn.SkipToPhase(Phase.REGROUP);
        // Too little for the Shadow player to prevent with
        scn.SetTwilight(0);

        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
        assertTrue(scn.FreepsPlayAvailable(tidings));
        assertEquals(1, scn.GetFreepsHandCount());
        assertEquals(5, scn.GetFreepsDeckCount());

        scn.FreepsPlayCard(tidings);

        assertEquals(3, scn.GetFreepsHandCount());
        assertEquals(2, scn.GetFreepsDeckCount());
        assertEquals(0, scn.GetWoundsOn(grimir));
        assertInZone(Zone.DISCARD, tidings);
        assertTrue(scn.AwaitingShadowRegroupPhaseActions());
    }
}
