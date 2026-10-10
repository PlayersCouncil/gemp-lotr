package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcealedExposedAtTest extends AbstractAtTest {
    //WTF why are these here lol

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
}
