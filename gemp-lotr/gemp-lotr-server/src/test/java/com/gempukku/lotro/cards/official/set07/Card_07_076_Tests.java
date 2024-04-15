package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_076_Tests
{

    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>()
                {{
                    put("friends", "7_76");
                    put("smeagol", "7_71");

                    put("chaff1", "6_21");
                    put("chaff2", "6_22");
                    put("chaff3", "6_23");
                    put("chaff4", "6_24");
                    put("chaff5", "6_25");
                    put("chaff6", "6_26");
                    put("chaff7", "6_27");
                    put("chaff8", "6_28");
                    put("chaff9", "6_29");
                    put("chaff10", "6_30");
                    put("chaff11", "6_31");
                    put("chaff12", "6_32");
                    put("chaff13", "6_33");
                    put("chaff14", "6_34");
                    put("chaff15", "6_35");
                    put("chaff16", "6_36");
                }},
                GenericCardTestHelper.FellowshipSites,
                GenericCardTestHelper.FOTRFrodo,
                GenericCardTestHelper.RulingRing
        );
    }

    @Test
    public void VeryNiceFriendsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 7
         * Name: Very Nice Friends
         * Unique: False
         * Side: Free Peoples
         * Culture: Gollum
         * Twilight Cost: 0
         * Type: Event
         * Subtype: Regroup
         * Game Text: Play Sméagol from your discard pile to play the fellowship's next site
         *  (replacing an opponent's site if necessary). If you do not move again this turn, discard your hand.
         */

        //Pre-game setup
        var scn = GetScenario();

        var card = scn.GetFreepsCard("friends");

        assertEquals("Very Nice Friends", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
        assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasKeyword(card, Keyword.REGROUP));
        assertEquals(0, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void VeryNiceFriendsRequiresSmeagolInDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var friends = scn.GetFreepsCard("friends");
        var smeagol = scn.GetFreepsCard("smeagol");
        scn.FreepsMoveCardToHand(friends, smeagol);

        scn.StartGame();
        scn.SkipToPhase(Phase.REGROUP);

        assertFalse(scn.FreepsPlayAvailable(friends));
    }

    @Test
    public void VeryNiceFriendsPlaysSmeagolFromDiscardToPlayNextSite() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var site3 = scn.GetFreepsSite("Council Courtyard");
        var friends = scn.GetFreepsCard("friends");
        var smeagol = scn.GetFreepsCard("smeagol");
        scn.FreepsMoveCardToHand(friends);
        scn.FreepsMoveCardToDiscard(smeagol);

        scn.StartGame();
        scn.SkipToPhase(Phase.REGROUP);

        assertTrue(scn.FreepsPlayAvailable(friends));
        assertEquals(Zone.ADVENTURE_DECK, site3.getZone());
        assertEquals(Zone.DISCARD, smeagol.getZone());
        scn.FreepsPlayCard(friends);
        //scn.FreepsChooseAnyCard();
        assertEquals(Zone.ADVENTURE_PATH, site3.getZone());
        assertEquals(Zone.FREE_CHARACTERS, smeagol.getZone());
    }

    @Test
    public void MovingDuringRegroupKeepsHand() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var friends = scn.GetFreepsCard("friends");
        var smeagol = scn.GetFreepsCard("smeagol");
        scn.FreepsMoveCardToHand(friends);
        scn.FreepsMoveCardToDiscard(smeagol);

        scn.StartGame();
        scn.FreepsDrawCards(7);
        scn.SkipToPhase(Phase.REGROUP);

        assertTrue(scn.FreepsPlayAvailable(friends));
        assertEquals(8, scn.GetFreepsHandCount());
        assertEquals(1, scn.GetFreepsDiscardCount()); //Only the smeagol we need
        scn.FreepsPlayCard(friends);
        //scn.FreepsChooseAnyCard();
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.FreepsDecisionAvailable("Do you want to make another move?"));
        scn.FreepsChooseToMove();
        assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
        assertEquals(7, scn.GetFreepsHandCount()); // full hand minus Very Nice Friends
        assertEquals(1, scn.GetFreepsDiscardCount()); //Smeagol now in play, VNF in discard
    }

    @Test
    public void StayingDuringRegroupDiscardsHandBeforeReconcile() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var friends = scn.GetFreepsCard("friends");
        var smeagol = scn.GetFreepsCard("smeagol");
        scn.FreepsMoveCardToHand(friends);
        scn.FreepsMoveCardToDiscard(smeagol);

        scn.StartGame();
        scn.FreepsDrawCards(7);
        scn.SkipToPhase(Phase.REGROUP);

        assertTrue(scn.FreepsPlayAvailable(friends));
        assertEquals(8, scn.GetFreepsHandCount());
        assertEquals(1, scn.GetFreepsDiscardCount()); //Only the smeagol we need
        scn.FreepsPlayCard(friends);
        //scn.FreepsChooseAnyCard();
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.FreepsDecisionAvailable("Do you want to make another move?"));
        scn.FreepsChooseToStay();
        scn.FreepsDeclineReconciliation();
        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertEquals(8, scn.GetFreepsHandCount()); // full hand after reconciliation
        assertEquals(8, scn.GetFreepsDiscardCount()); //VNF in discard + entire discarded 7-card hand
    }
}
