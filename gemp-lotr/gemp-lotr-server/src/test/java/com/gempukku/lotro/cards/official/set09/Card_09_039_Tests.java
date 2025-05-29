package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_039_Tests
{

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("library", "9_39");
                    put("troop1", "1_143");
                    put("axe1", "1_9");
                    put("troop2", "1_143");
                    put("axe2", "1_9");
                    put("troop3", "1_143");
                    put("axe3", "1_9");

                    put("runner", "1_178");
                }}
        );
    }


    @Test
    public void LibraryOfOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 9
         * Name: Library of Orthanc
         * Unique: True
         * Side: Shadow
         * Culture: Isengard
         * Twilight Cost: 2
         * Type: Artifact
         * Subtype: Support Area
         * Game Text: Shadow: Play an [ISENGARD] minion to stack the top card of your draw deck on this card.
         *   Skirmish: Remove (1) and discard a Free Peoples card stacked here to make an [ISENGARD] minion strength +1.
         *   Regroup: Remove (1) to take an [ISENGARD] card stacked here into hand.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsCard("library");

        assertEquals("Library of Orthanc", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertTrue(card.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, card.getBlueprint().getSide());
        assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
        assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
        assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
        assertEquals(2, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void ShadowAbilityStacksTopCardOfDeckAfterPlayingMinion() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var library = scn.GetShadowCard("library");
        var troop1 = scn.GetShadowCard("troop1");
        var troop2 = scn.GetShadowCard("troop2");

        scn.MoveCardsToHand(library, troop1, troop2);

        scn.StartGame();
        scn.SetTwilight(20);

        scn.FreepsPassCurrentPhaseAction();

        scn.ShadowPlayCard(library);
        assertTrue(scn.ShadowActionAvailable(library));
        assertEquals(5, scn.GetShadowDeckCount());
        assertEquals(0, scn.GetStackedCards(library).size());

        scn.ShadowUseCardAction(library);
        assertTrue(scn.ShadowDecisionAvailable("Choose card to play from hand"));
        scn.ShadowChooseCards(troop1);
        assertEquals(4, scn.GetShadowDeckCount());
        assertEquals(1, scn.GetStackedCards(library).size());

        scn.ShadowUseCardAction(library);
        assertFalse(scn.ShadowDecisionAvailable("Choose cards from hand"));
        //scn.ShadowChooseCards(troop2);  //As there's only one, this happens automatically
        assertEquals(3, scn.GetShadowDeckCount());
        assertEquals(2, scn.GetStackedCards(library).size());

    }

    @Test
    public void SkirmishAbilityRemoves1AndDiscardsStackedFreepsCardToAdd1Strength() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var library = scn.GetShadowCard("library");
        var troop1 = scn.GetShadowCard("troop1");
        var troop2 = scn.GetShadowCard("troop2");
        var axe1 = scn.GetShadowCard("axe1");

        scn.MoveCardsToSupportArea(library);
        scn.MoveMinionsToTable(troop1);
        scn.StackCardsOn(library, axe1, troop2);

        scn.StartGame();
        scn.SetTwilight(20);

        scn.FreepsPassCurrentPhaseAction();

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(scn.GetRingBearer(), troop1);
        scn.FreepsResolveSkirmish(scn.GetRingBearer());
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.ShadowActionAvailable(library));
        // 20 initially, +2 from move, the rest were all cheated in
        assertEquals(22, scn.GetTwilight());
        assertEquals(9, scn.GetStrength(troop1));
        assertEquals(2, scn.GetStackedCards(library).size());

        scn.ShadowUseCardAction(library);
        assertFalse(scn.ShadowAnyDecisionsAvailable()); // only 1 valid choice even with 2 stacked, so should have no decision
        assertEquals(21, scn.GetTwilight());
        assertEquals(10, scn.GetStrength(troop1));
        assertEquals(1, scn.GetStackedCards(library).size());
        assertEquals(Zone.DISCARD, axe1.getZone());
    }

    @Test
    public void RegroupAbilityRemoves1AndTakesStackedIsengardCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var library = scn.GetShadowCard("library");
        var troop1 = scn.GetShadowCard("troop1");
        var runner = scn.GetShadowCard("runner");
        var axe1 = scn.GetShadowCard("axe1");

        scn.MoveCardsToSupportArea(library);
        scn.StackCardsOn(library, axe1, runner, troop1);

        scn.StartGame();
        scn.SetTwilight(20);

        scn.FreepsPassCurrentPhaseAction();

        scn.SkipToPhase(Phase.REGROUP);
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.ShadowActionAvailable(library));
        // 20 initially, +2 from move, the rest were all cheated in
        assertEquals(22, scn.GetTwilight());
        assertEquals(0, scn.GetShadowHandCount());
        assertEquals(3, scn.GetStackedCards(library).size());

        scn.ShadowUseCardAction(library);
        assertFalse(scn.ShadowAnyDecisionsAvailable()); // only 1 valid choice even with 3 stacked, so should have no decision
        assertEquals(21, scn.GetTwilight());
        assertEquals(1, scn.GetShadowHandCount());
        assertEquals(2, scn.GetStackedCards(library).size());
        assertEquals(Zone.HAND, troop1.getZone());
    }
}
