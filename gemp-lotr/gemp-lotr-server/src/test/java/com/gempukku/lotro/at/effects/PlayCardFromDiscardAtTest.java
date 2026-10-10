package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayCardFromDiscardAtTest extends AbstractAtTest {
    @Test
    public void ulaireNerteaCantPlayMinionsOnGreatRiver() throws Exception {
        initializeSimplestGame();

        for (int i = 0; i < 4; i++) {
            final PhysicalCardImpl greatRiver = createCard(P1, "1_306");
            _game.getGameState().addCardToZone(_game, greatRiver, Zone.FREE_CHARACTERS);
        }

        skipMulligans();

        final PhysicalCardImpl greatRiver = createCard(P2, "3_118");
        greatRiver.setSiteNumber(2);
        _game.getGameState().addCardToZone(_game, greatRiver, Zone.ADVENTURE_PATH);

        final PhysicalCardImpl ulaireNertea = createCard(P2, "1_234");
        _game.getGameState().addCardToZone(_game, ulaireNertea, Zone.HAND);

        final PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.DISCARD);

        _game.getGameState().setTwilight(20);

        // Fellowship phase
        playerDecided(P1, "");

        assertEquals(greatRiver, _game.getGameState().getCurrentSite());

        playerDecided(P2, getCardActionId(P2, "Play Úlairë Nertëa"));

        assertFalse(_userFeedback.getAwaitingDecision(P2).getDecisionType() == AwaitingDecisionType.MULTIPLE_CHOICE);
    }
}
