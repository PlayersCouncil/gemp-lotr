package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_248_ErrataTests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("forces1", "51_248");
                    put("forces2", "51_248");
                    put("troll", "6_103");
                    put("orc1", "6_102");
                    put("orc2", "6_102");
                    put("orc3", "6_102");
                    put("orc4", "6_102");
                }}
        );
    }

    @Test
    public void ForcesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: Forces of Mordor
         * Side: Shadow
         * Culture: Sauron
         * Twilight Cost: 0
         * Type: Event
         * Phase: Shadow
         * Errata Game Text: Shadow: Exert a [SAURON] minion to add (1) for each [SAURON] Orc you can spot (limit (3)).
         */

        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        var forces = scn.GetFreepsCard("forces1");

        assertEquals(0, forces.getBlueprint().getTwilightCost());
        assertEquals(Side.SHADOW, forces.getBlueprint().getSide());
        assertEquals(CardType.EVENT, forces.getBlueprint().getCardType());
        assertEquals(Culture.SAURON, forces.getBlueprint().getCulture());
    }

    @Test
    public void ExertsMinionWhenPlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        var forces1 = scn.GetShadowCard("forces1");
        var forces2 = scn.GetShadowCard("forces2");
        var troll = scn.GetShadowCard("troll");
        var orc1 = scn.GetShadowCard("orc1");
        var orc2 = scn.GetShadowCard("orc2");
        var orc3 = scn.GetShadowCard("orc3");
        var orc4 = scn.GetShadowCard("orc4");

        scn.ShadowMoveCardToHand(forces1,forces2);
        scn.ShadowMoveCharToTable(troll, orc1, orc2);

        scn.StartGame();

        scn.FreepsPassCurrentPhaseAction();
        assertTrue(scn.ShadowActionAvailable("Forces of Mordor"));
        assertEquals(2, scn.GetTwilight());

        scn.ShadowPlayCard(forces1);

        assertTrue(scn.ShadowDecisionAvailable("Exert"));
        assertEquals(3, scn.ShadowGetCardChoiceCount());
        scn.ShadowChoose(scn.ShadowGetCardChoices().get(1));

        //There are 3 minions, but only 2 orcs
        assertEquals(4, scn.GetTwilight());

        scn.ShadowMoveCharToTable(orc3, orc4);
        scn.ShadowPlayCard(forces2);
        scn.ShadowChoose(scn.ShadowGetCardChoices().get(1));

        //There are 5 minions, but only 4 orcs which should hit the limit of 3.
        assertEquals(7, scn.GetTwilight());

    }

}
