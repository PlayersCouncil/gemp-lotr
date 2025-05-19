package com.gempukku.lotro.at;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConcealedExposedAtTest extends AbstractAtTest {
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                    put("arwen", "1_30");
                }}
        );
    }


    @Test
    public void ConcealedDoesNothingIfNoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");

        scn.MoveCompanionToTable(aragorn);

        scn.StartGame();

        scn.ApplyAdHocModifier(new AddKeywordModifier(aragorn, aragorn, null, Keyword.CONCEALED));

        assertEquals(0, scn.GetTwilight());
        scn.FreepsPassCurrentPhaseAction();

        //1 for the ring-bearer, 1 for aragorn, 1 for the site (King's Tent)
        assertEquals(3, scn.GetTwilight());
    }

    @Test
    public void ConcealedRemovesOneIfAvailable() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl nostranger = scn.GetFreepsCard("nostranger");

        scn.MoveCardsToHand(aragorn);

        scn.StartGame();

        scn.FreepsPlayCard(aragorn);
        scn.ApplyAdHocModifier(new AddKeywordModifier(aragorn, aragorn, null, Keyword.CONCEALED));

        //4 from playing aragorn
        assertEquals(4, scn.GetTwilight());
        scn.FreepsPassCurrentPhaseAction();

        //4 from playing aragorn, 1 for the ring-bearer, 1 for aragorn, 1 for the site (King's Tent), -1 for concealed
        assertEquals(6, scn.GetTwilight());
    }

    @Test
    public void TwoConcealedRemovesTwoIfAvailable() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");

        scn.MoveCardsToHand(aragorn);
        scn.MoveCompanionToTable(arwen);

        scn.StartGame();

        scn.FreepsPlayCard(aragorn);
        scn.ApplyAdHocModifier(new AddKeywordModifier(aragorn, Keyword.RANGER, null, Keyword.CONCEALED));


        //4 from playing aragorn
        assertEquals(4, scn.GetTwilight());
        scn.FreepsPassCurrentPhaseAction();

        //4 from playing aragorn, 1+1+1 for companions, 1 for the site (King's Tent), -2 for concealed
        assertEquals(6, scn.GetTwilight());
    }

    @Test
    public void ConcealedRemovesNothingIfExposed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");

        scn.MoveCardsToHand(aragorn);

        scn.StartGame();

        scn.FreepsPlayCard(aragorn);
        scn.ApplyAdHocModifier(new AddKeywordModifier(aragorn, Keyword.RANGER, null, Keyword.CONCEALED));

        scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.EXPOSED));

        //4 from playing aragorn
        assertEquals(4, scn.GetTwilight());
        scn.FreepsPassCurrentPhaseAction();

        //4 from playing aragorn, 1 for the ring-bearer, 1 for aragorn, 1 for the site (King's Tent), 0 for exposed concealed
        assertEquals(7, scn.GetTwilight());
    }

    @Test
    public void lookAtHand() throws Exception {
        initializeSimplestGame();

        PhysicalCard goblinMan = addToZone(createCard(P2, "2_42"), Zone.SHADOW_CHARACTERS);
        PhysicalCard goblinManInHand = addToZone(createCard(P1, "2_42"), Zone.HAND);

        passUntil(Phase.SHADOW);
        selectCardAction(P2, goblinMan);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, getAwaitingDecision(P2).getDecisionType());
    }

    @Test
    public void lookAtRandomCardsFromHand() throws Exception {
        initializeSimplestGame();

        PhysicalCard goblinMan = addToZone(createCard(P2, "2_42"), Zone.SHADOW_CHARACTERS);
        PhysicalCard galadriel = addToZone(createCard(P1, "1_45"), Zone.SUPPORT);
        PhysicalCard mirrorOfGaladriel = addToZone(createCard(P1, "1_55"), Zone.SUPPORT);

        for (int i = 0; i < 7; i++) {
            addToZone(createCard(P2, "2_42"), Zone.HAND);
        }

        passUntil(Phase.MANEUVER);
        selectCardAction(P1, mirrorOfGaladriel);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, getAwaitingDecision(P1).getDecisionType());
    }

    @Test
    public void lookAtTopCardsOfDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard gandalf = addToZone(createCard(P1, "1_364"), Zone.FREE_CHARACTERS);
        PhysicalCard questionsThatNeedAnswering = addToZone(createCard(P1, "1_81"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, questionsThatNeedAnswering);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, getAwaitingDecision(P1).getDecisionType());
    }

    @Test
    public void revealBottomCardsOfDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard shepherdOfTheTrees = addToZone(createCard(P1, "15_36"), Zone.FREE_CHARACTERS);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard topCard = addToZone(createCard(P1, "1_3"), Zone.DECK);
        PhysicalCard middleCard = addToZone(createCard(P1, "1_4"), Zone.DECK);
        PhysicalCard bottomCard = addToZone(createCard(P1, "1_5"), Zone.DECK);

        passUntil(Phase.MANEUVER);
        selectCardAction(P1, shepherdOfTheTrees);
        assertNotNull(getArbitraryCardId(P1, "1_5"));
        assertNotNull(getArbitraryCardId(P2, "1_5"));
    }

    @Test
    public void revealCardsFromAdventureDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard destroyedHomestead = addToZone(createCard(P2, "15_76"), Zone.HAND);
        PhysicalCard easterlingScout1 = addToZone(createCard(P2, "15_77"), Zone.SHADOW_CHARACTERS);
        PhysicalCard easterlingScout2 = addToZone(createCard(P2, "15_77"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        setTwilightPool(10);
        passUntil(Phase.SHADOW);
        selectCardAction(P2, destroyedHomestead);
        assertEquals("Revealed card(s)", getAwaitingDecision(P1).getText());
        assertEquals(1, getAwaitingDecision(P1).getDecisionParameters().get("cardId").length);
        assertEquals("Revealed card(s)", getAwaitingDecision(P2).getText());
        assertEquals(1, getAwaitingDecision(P2).getDecisionParameters().get("cardId").length);
    }
}
