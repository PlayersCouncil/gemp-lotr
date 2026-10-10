package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ExtraCostToPlayAtTest extends AbstractAtTest {
    @Test
    public void addThreat() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl wraith= new PhysicalCardImpl(100, "10_26", P1, _cardLibrary.getLotroCardBlueprint("10_26"));

        _game.getGameState().addCardToZone(_game, wraith, Zone.HAND);

        skipMulligans();

        playerDecided(P1, "0");

        assertEquals(Zone.FREE_CHARACTERS, wraith.getZone());
        assertEquals(1, _game.getGameState().getThreats());
    }

    @Test
    public void cantPlayTooManyThreats() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl wraith= new PhysicalCardImpl(100, "10_26", P1, _cardLibrary.getLotroCardBlueprint("10_26"));

        _game.getGameState().addCardToZone(_game, wraith, Zone.HAND);
        _game.getGameState().addThreats(P1, 3);

        skipMulligans();

        String[] actionIds = (String[]) _userFeedback.getAwaitingDecision(P1).getDecisionParameters().get("actionId");
        assertEquals(0, actionIds.length);
    }
}
