package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CostAtTest extends AbstractAtTest {
    @Test
    public void playOnCostReduction() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, LotroDeck> decks = new HashMap<>();

        LotroDeck deck = createSimplestDeck();
        // Frodo, Reluctant Adventurer
        deck.setRingBearer("2_102");
        decks.put(P1, deck);
        addPlayerDeck(P2, decks, null);

        initializeGameWithDecks(decks);

        PhysicalCardImpl hobbitSword = createCard(P1, "1_299");

        _game.getGameState().addCardToZone(_game, hobbitSword, Zone.HAND);

        skipMulligans();

        // Play Hobbit Sword (on Frodo) - should be free
        final int twilightPool = _game.getGameState().getTwilightPool();
        playerDecided(P1, "0");
        assertEquals(twilightPool, _game.getGameState().getTwilightPool());
    }
}
