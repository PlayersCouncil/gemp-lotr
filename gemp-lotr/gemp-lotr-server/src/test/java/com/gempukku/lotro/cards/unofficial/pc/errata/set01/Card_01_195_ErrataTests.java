package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_195_ErrataTests
{

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>()
                {{
                    put("relics", "51_195");
                    put("runner", "1_178");
                    put("scimitar", "1_180");

                }},
                VirtualTableScenario.FellowshipSites,
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.RulingRing
        );
    }


    @Test
    public void RelicsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1
         * Title: Relics of Moria
         * Unique: true
         * Side: Shadow
         * Culture: Moria
         * Twilight Cost: 1
         * Type: Condition
         * Subtype: Support Area
         * Game Text: Plays to your support area.
         * Shadow: Remove (2) to play a [MORIA] possession from your discard pile.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl relics = scn.GetFreepsCard("relics");

        assertTrue(relics.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, relics.getBlueprint().getSide());
        assertEquals(Culture.MORIA, relics.getBlueprint().getCulture());
        assertEquals(CardType.CONDITION, relics.getBlueprint().getCardType());
        assertEquals(1, relics.getBlueprint().getTwilightCost());
        assertTrue(scn.HasKeyword(relics, Keyword.SUPPORT_AREA));
    }

    @Test
    public void ShadowAbilityRemoves2ToPlayMoriaPossessionFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var relics = scn.GetShadowCard("relics");
        var runner = scn.GetShadowCard("runner");
        var scimitar = scn.GetShadowCard("scimitar");
        scn.MoveCardsToSupportArea(relics);
        scn.MoveMinionsToTable(runner);
        scn.MoveCardsToDiscard(scimitar);

        scn.StartGame();

        scn.FreepsPassCurrentPhaseAction();

        assertEquals(3, scn.GetTwilight());
        assertEquals(Zone.DISCARD, scimitar.getZone());
        assertTrue(scn.ShadowActionAvailable(relics));
        scn.ShadowUseCardAction(relics);

        assertEquals(1, scn.GetTwilight());
        assertEquals(Zone.ATTACHED, scimitar.getZone());
        assertFalse(scn.ShadowActionAvailable(relics));
    }

    @Test
    public void ShadowAbilityNotAvailableIfNoPossessionsInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var relics = scn.GetShadowCard("relics");
        var runner = scn.GetShadowCard("runner");
        var scimitar = scn.GetShadowCard("scimitar");
        scn.MoveCardsToSupportArea(relics);
        scn.MoveMinionsToTable(runner);

        scn.StartGame();

        scn.FreepsPassCurrentPhaseAction();

        assertEquals(Zone.DECK, scimitar.getZone());
        assertEquals(0, scn.GetShadowDiscardCount());
        assertFalse(scn.ShadowActionAvailable(relics));
    }

}
